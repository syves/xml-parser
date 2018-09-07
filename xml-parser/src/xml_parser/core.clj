(ns xml-parser.core

(:require [clojure.java.io :as io]
          [clojure.java.jdbc :as jdbc]
          [clojure.xml :as xml]
          [clojure.zip :as zip]
          ;[clojure.data.xml :refer :all]
          [clojure.data.xml :as c-d-xml :refer [parse]]
          [clojure.data.zip.xml :as c-d-z-xml
                :refer [xml-> xml1-> attr attr= text]]
          [clojure.pprint :refer [pprint]])
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

;TODO for each map create a sql query

;({:firstname "00226501",
;:lastname "MCGREWJR",
;:date-of-birth "1936-02-01",
;:phone "9796740198"}...)

"""UPDATE person
      SET phone='5859012134'
      WHERE
        fname='JIARA'
        AND lname='HERTZEL'
        AND dob='1935-06-05'
        AND phone!='5859012134';
    INSERT INTO
      person(fname, lname, dob, phone)
    SELECT 'JIARA','HERTZEL','1935-06-05','5859012134'
       WHERE NOT EXISTS (SELECT * FROM person
                          WHERE
                            fname='JIARA'
                            AND lname='HERTZEL'
                            AND dob='1935-06-05');"""

;inserts are rare in this example case but they emit selects which could slow down the process.
;UPDATE tableName SET col1 = value WHERE colX = arg1 and colY = arg2;
   ;IF NOT FOUND THEN
   ;INSERT INTO tableName values (value, arg1, arg2);

(map (fn [rec]
          (str
            (format "CASE WHEN fname = %s;" (get rec :firstname ""))
            (format "AND lname = %s;" (get rec :lastname ""))
            (format "AND dob = %s;"(get rec :date-of-birth ""))
            (format "AND phone != %s THEN UPDATE foo;" (get rec :phone ""))

            ))
      (take 3 list-map))

;output query to file?
