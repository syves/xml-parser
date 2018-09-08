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

(defn format-date [varrchar] (str/join (str/split varrchar #"-")))
(def str-date (format-date "1935-06-05"))
(sql/raw ["CAST ('str-date AS DATE)'"])

;this will not work because there is no support for where clause on constraint.
;(jdbc/query db-spec
;  (-> (insert-into :person)
;      (values [{:fname "shakrah"
;                :lname "yves"
;                :phone "1234567891"}])
;      (upsert (-> (on-conflict [:fname :lname :dob]
;                  (do-update-set :phone))))
;      (returning :*)
;      sql/format))

; I'm not sure how to do an udate or insert with this library
;(jdbc/query db-spec
  ;these operations do npt happen in order?
  ;(-> (insert-into :person)
  ;    (values [{:fname "shakrah"
  ;              :lname "yves"
  ;              :phone "1234567891"}])
  ;    ;if update fails then insert
  ;    (helpers/update :person)
  ;    (sset {:phone "1112225554"})
  ;    (where [:and
  ;              [:= :fname "shakrah"]
  ;              [:= :lname "yves"]
  ;              [:<> :phone "1234567899"]])
      ;(lock :mode :update)
  ;    sql/format))

; this does not work because of the cast cannot be inserted here?
;(jdbc/query db-spec
;  (-> (select :fname :lname :dob :phone)
;      (from :person)
;      (where [:and
;                [:= :fname "JIARA"]
;                [:= :lname "HERTZEL"]
;                ;syntax error at or near ")"
;                [:= :dob (sql/raw ["CAST ('str-date AS DATE)'"])]
;                [:<> :phone "5859012188"]])
;      sql/format))
;'5859012134'

(defn sql-str-builder [rec]
              [(format "UPDATE person SET phone=%s WHERE fname=%s AND lname=%s AND dob=%s AND phone!=%s ;INSERT INTO person(fname, lname, dob, phone) SELECT %s,%s,%s,%s WHERE NOT EXISTS (SELECT * FROM person WHERE fname=%s AND lname=%s AND dob=%s);"
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

(def raw3 "UPDATE person SET phone='5859012666' WHERE fname='JIARA' AND lname='HERTZEL' AND dob='1935-06-05' AND phone!='5859012666';INSERT INTO person(fname, lname, dob, phone) SELECT 'JIARA','HERTZEL','1935-06-05','5859012666' WHERE NOT EXISTS (SELECT * FROM person WHERE fname='JIARA' AND lname='HERTZEL' AND dob='1935-06-05');")

(jdbc/query db-spec [raw3]) ;works

;(map (fn [rec]
;         (jdbc/query db-spec (sql-str-builder rec)))
;         list-map)

(map (fn [rec]
          (jdbc/query db-spec (sql-str-builder rec)))
          (take 3 list-map)

;inserts are rare in this example case but they emit selects which could slow down the process.
