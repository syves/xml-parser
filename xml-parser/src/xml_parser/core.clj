(ns xml-parser.core

(:require [clojure.java.io :as io]
          [clojure.java.jdbc :as jdbc]
          [clojure.data.xml :as c-d-xml :refer [parse]]
          [clojure.data.zip.xml :as c-d-z-xml
                :refer [xml-> xml1-> attr attr= text]]
          [clojure.string :as str])
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

(defn batch-query [big-map query-builder]
  (map (fn [rec]
    (query-builder rec))
        big-map))

(defn singleton-sql-upsert-builder [rec]
              [(format "UPDATE person SET phone='%s' WHERE fname='%s' AND lname='%s' AND dob='%s' AND phone!='%s';INSERT INTO person(fname, lname, dob, phone) SELECT '%s','%s','%s','%s' WHERE NOT EXISTS (SELECT * FROM person WHERE fname='%s' AND lname='%s' AND dob='%s');"
              (get rec :phone "")
              (get rec :firstname "")
              (get rec :lastname "")
              (get rec :date-of-birth "")
              (get rec :phone "")
              (get rec :firstname "")
              (get rec :lastname "")
              (get rec :date-of-birth "")
              (get rec :phone "")
              (get rec :firstname "")
              (get rec :lastname "")
              (get rec :date-of-birth ""))])

;TODO no idea how this should be formated.
(defn sql-upsert-builder [rec]
              (format "UPDATE person SET phone='%s' WHERE fname='%s' AND lname='%s' AND dob='%s' AND phone!='%s';INSERT INTO person(fname, lname, dob, phone) SELECT '%s','%s','%s','%s' WHERE NOT EXISTS (SELECT * FROM person WHERE fname='%s' AND lname='%s' AND dob='%s');"
              (get rec :phone "")
              (get rec :firstname "")
              (get rec :lastname "")
              (get rec :date-of-birth "")
              (get rec :phone "")
              (get rec :firstname "")
              (get rec :lastname "")
              (get rec :date-of-birth "")
              (get rec :phone "")
              (get rec :firstname "")
              (get rec :lastname "")
              (get rec :date-of-birth ""))
              )

(defn sql-updat-then-insert-builder [rec]
  [(format "UPDATE person SET phone='%s' WHERE fname='%s' AND lname='%s' AND dob='%s' AND phone!='%s';"
          (get rec :phone "")
          (get rec :firstname "")
          (get rec :lastname "")
          (get rec :date-of-birth "")
          (get rec :phone ""))

  (format "INSERT INTO person(fname, lname, dob, phone) SELECT '%s','%s','%s','%s' WHERE NOT EXISTS (SELECT * FROM person WHERE fname='%s' AND lname='%s' AND dob='%s');"
        (get rec :firstname "")
        (get rec :lastname "")
        (get rec :date-of-birth "")
        (get rec :phone "")
        (get rec :firstname "")
        (get rec :lastname "")
        (get rec :date-of-birth ""))])

(defn singleton-sql-select-builder [rec]
  [(format "SELECT * FROM person WHERE fname='%s' AND lname='%s' AND dob='%s'AND phone!='%s';"
            (get rec :firstname "")
            (get rec :lastname "")
            (get rec :date-of-birth "")
            (get rec :phone ""))])

(defn sql-select-builder [rec]
  (format "SELECT * FROM person WHERE fname='%s' AND lname='%s' AND dob='%s'AND phone!='%s';"
          (get rec :firstname "")
          (get rec :lastname "")
          (get rec :date-of-birth "")
          (get rec :phone "")))

;TODO to check if records were updated from dict, move to test?
(defn sql-select-contraint-builder [rec]
  [(format "SELECT * FROM person WHERE fname='%s' AND lname='%s' AND dob='%s'AND phone='%s';"
            (get rec :firstname "")
            (get rec :lastname "")
            (get rec :date-of-birth "")
            (get rec :phone ""))])

;TODO trying to emmit results, but syntax is wrong
(defn query-runner [records string-builder]
  (map (fn [rec]
    ;(try
      ;try return result sert true
      (jdbc/query db-spec
                  (string-builder rec)
                  #{:as-arrays? true}))
    ;(catch Exception e (str "caught exception: " (.getMessage e)))))
      records))

;not a transaction!
(defn batch-query-runner [queries]
    (try
      (jdbc/db-do-commands db-spec false queries)
    (catch Exception e
      (str "caught exception: " (.getMessage e))))
  )

(defn update-or-insert!
  [db table rec where-clause]
  (jdbc/with-db-transaction [t-con db]
    (let [result
      (try
          (jdbc/update!
                   db-spec
                   :person
                   {:phone (get rec :phone "")}
                   ["fname = ? AND lname = ? AND dob = CAST (? AS DATE) AND phone <> ?"
                    (get rec :firstname "")
                    (get rec :lastname "")
                    (get rec :date-of-birth "")
                    (get rec :phone "")])
        (catch SQLException e (jdbc/print-sql-exception-chain e)))]
      (if (zero? (first result))
        (try
          (jdbc/insert! t-con
                      table
                      [:fname :lname :dob :phone]
                      [(get rec :firstname "")
                      (get rec :lastname "")
                      (get rec :date-of-birth "")
                      (get rec :phone "")])
          (catch SQLException e (jdbc/print-sql-exception-chain e)))
          result)
      )))

(defn batch-query-with-db-con-2 [records db-con table where-clause]
    (jdbc/with-db-connection [db-con db-spec]
        (map
          (fn [rec]
            (update-or-insert!
              db-con
              table
              rec
              where-clause))
         records)))
