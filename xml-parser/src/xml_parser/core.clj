(ns xml-parser.core
  (:require [clojure.java.io :as io]
          [clojure.java.jdbc :as jdbc]
          [clojure.data.xml :as c-d-xml :refer [parse]]
          [clojure.data.zip.xml :as c-d-z-xml
                :refer [xml-> xml1-> attr attr= text]]
          [clojure.string :as str]
          [clj-time.core :as t]
          [clj-time.format :as f]
          [clj-time.coerce :as c])
          (:import org.apache.commons.io.input.BOMInputStream
                   org.apache.commons.io.ByteOrderMark
                   java.util.zip.GZIPInputStream
                   java.sql.SQLException))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//localhost:5432/testdb2"})

(def gzip-filepath "./resources/update-file.xml.gz")

(def bom-array
  (into-array [ByteOrderMark/UTF_16LE
               ByteOrderMark/UTF_16BE
               ByteOrderMark/UTF_8
               ByteOrderMark/UTF_32BE
               ByteOrderMark/UTF_32LE]))

;Note does not strip whitespace
(defn bom-reader
  "removes BOMs"
  [file]
    (io/reader
      (BOMInputStream.
        (GZIPInputStream.
          (io/input-stream
            (io/file
               file)))
        false
        bom-array)))

;op takes 75 sec once?
(def list-map
  (with-open [rdr (bom-reader gzip-filepath)]
    (doall
      (take 1500000
        (->> rdr
             ;;Returns a lazy tree of the xml/element struct-map
             parse
             :content
             (map (fn [member]
                      (reduce (fn [acc elem]
                                  (assoc acc
                                         (:tag elem)
                                         (first (:content elem))))
                              {}
                              (:content member)))))))))

(def custom-formatter (f/formatter "yyyy-MM-dd"))

;removed phone number
(defn to-where-clause [rec] ["fname = ? AND lname = ? AND dob = CAST (? AS DATE)"
(get rec :firstname "")
(get rec :lastname "")
(get rec :date-of-birth "")
])

(defn update-or-insert!
  [db table rec where-clause]
  (jdbc/with-db-transaction [t-con db]
    (let [result
      (try
          (jdbc/update!
                   db-spec
                   :person
                   {:phone (get rec :phone "")}
                   where-clause)
        (catch SQLException e (jdbc/print-sql-exception-chain e)))]
      (if (zero? (first result))
      (try
        (jdbc/insert!
          db-spec
          :person
          [:fname :lname :dob :phone]
          [(get rec :firstname "")
           (get rec :lastname "")
           (java.sql.Date. (c/to-long (f/parse custom-formatter (get rec :date-of-birth ""))))
           (get rec :phone "")])
        (catch SQLException e (jdbc/print-sql-exception-chain e)))
          result)
      )))

(defn batch-transaction [records db-con table gen-where-clause]
    ;(jdbc/with-db-connection [db-con db-spec]
        (map
          (fn [rec]
            (update-or-insert!
              db-con
              table
              rec
              (gen-where-clause rec)))
         records))
