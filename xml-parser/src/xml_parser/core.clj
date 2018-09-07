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
          [honeysql.helpers :refer :all :as helpers])
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

(defn format-date [varrchar] (str/join (str/split varrchar #"-")))

(def str-date (format-date "1935-06-05"))
(sql/raw ["CAST ('#sql/param str-date AS DATE)'"])
(sql/format (sql/raw ["CAST ('#sql/param str-date AS DATE)'"]))
(sql/raw ["CAST ('#sql/param str-date AS DATE)'"])

WHERE NOT EXISTS (SELECT * FROM person
                   WHERE
                     fname='JIARA'
                     AND lname='HERTZEL'
                     AND dob='1935-06-05')

(sql/format {:exists })

(where [:= :exists {:select *
                      :from :person
                      :where
                      :fname "shakrah"
                      :lname "yves"
                    }
              false])
}
;works but returns no result ?!
(jdbc/query db-spec
    (sql/format {:union [(-> (helpers/update :person)
                             (sset {:phone "1112225554"})
                             (where [:and
                                       [:= :fname "shakrah"]
                                       [:= :lname "yves"]
                                       [:<> :phone "1234567899"]
                                       ]))
                            ;if update fails then insert
                         (-> (insert-into :person)
                             (values [{:fname "shakrah"
                                       :lname "yves"
                                       :phone "11122244444"}]))]
                }))
    ;(lock :mode :update)

(jdbc/query db-spec
  (-> (select :fname :lname :dob :phone)
      (from :person)
      (where [:and
                [:= :fname "JIARA"]
                [:= :lname "HERTZEL"]
                [:= :dob (sql/raw ["CAST ('str-date AS DATE)'"])]
                [:<> :phone "5859012188"]])
      sql/format))
;--> ({:fname "JIARA", :lname "HERTZEL", :dob #inst "1935-06-04T23:00:00.000-00:00", :phone "5859012134"})


(def testSQLstr
  "UPDATE person
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
                            AND dob='1935-06-05');")

;inserts are rare in this example case but they emit selects which could slow down the process.
;UPDATE tableName SET col1 = value WHERE colX = arg1 and colY = arg2;
   ;IF NOT FOUND THEN
   ;INSERT INTO tableName values (value, arg1, arg2);

(take 3 list-map)

;output query to file?
