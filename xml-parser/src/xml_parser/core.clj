(ns xml-parser.core

(:require [clojure.java.io :as io]
          [clojure.java.jdbc :as jdbc]
          [clojure.xml :as xml]
          [clojure.zip :as zip]
          [clojure.data.xml :as c-d-xml :refer [parse]]
          [clojure.data.zip.xml :as c-d-z-xml
                :refer [xml-> xml1-> attr attr= text]]
          [clojure.pprint :refer [pprint]]
          [clojure.string :as str]
          [honeysql.core :as sql]
          [honeysql.helpers :refer :all :as helpers]
          [honeysql-postgres.format :refer :all]
          [honeysql-postgres.helpers :refer :all])
          (:import org.apache.commons.io.input.BOMInputStream
                   org.apache.commons.io.ByteOrderMark
                   java.util.zip.GZIPInputStream))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//localhost:5432/testdb"
              ;; Not needed for a non-secure local database...
              ;; :user "username"
              ;; :password "secret"
              })

(def base "/Users/syves/github.com/syves/lambdawerk-backend-test/xml-parser/resources/")
(def gzip-filepath (str base "update-file.xml.gz"))

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

;op takes 75 sec
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

(defn sql-upsert-builder [rec]
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

(defn sql-select-builder [rec]
                            [(format "SELECT * FROM person WHERE fname='%s' AND lname='%s' AND dob='%s'AND phone!='%s';"
                            (get rec :firstname "")
                            (get rec :lastname "")
                            (get rec :date-of-birth "")
                            (get rec :phone "")
                            )])

(defn query [records string-builder]
           (map (fn [rec]
                    (try
                      (jdbc/query db-spec (string-builder rec))
                      (catch Exception e (str "caught exception: "                        (.getMessage e)))))
                records))

;TODO remove after testing from lein
(defn test-lein-time [] (query (take 5 list-map) sql-upsert-builder))
