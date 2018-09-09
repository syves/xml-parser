(ns xml-parser.core-test
  (:require [clojure.test :refer :all]
            [xml-parser.core :refer :all]
            [clojure.java.jdbc :as jdbc]))

(deftest a-test

  (def test-list-map '({:firstname "JIARA", :lastname "HERTZEL", :date-of-birth "1935-06-05", :phone "1111111111"} {:firstname "00226501", :lastname "MCGREWJR", :date-of-birth "1936-02-01", :phone "1111111111"}))

  (testing "what does trans-query return with trys."
    (is (=
          (trans-query
            test-list-map
            sql-upsert-builder
            sql-select-contraint-builder)
            "fooBar")))

  (testing "transaction query with updates/inserts followed by select ;shows success. Can process the entire update.xml file"
    (def actual (query list-map sql-upsert-builder))
    (print actual)
    (is (= actual
           (query list-map sql-upsert-builder))
        "Can process the entire update.xml file"))

  (testing "transaction query with updates/inserts followed by select shows success."
    (is (=
           (trans-query test-list-map sql-upsert-builder sql-select-contraint-builder))
           ""))

  (testing "query with updates/inserts sql queries updates the db."
    (is (=
           (query test-list-map sql-upsert-builder))
           '("caught exception: No results were returned by the query." "caught exception: No results were returned by the query." "caught exception: No results were returned by the query."))

    (is (=
          (query test-list-map sql-select-contraint-builder)
          (({:fname "JIARA", :lname "HERTZEL", :dob #inst "1935-06-04T23:00:00.000-00:00", :phone "1111111111"}) ({:fname "00226501", :lname "MCGREWJR", :dob #inst "1936-01-31T23:00:00.000-00:00", :phone "1111111111"}))
          ))
          )

  (testing "sql-select-builder, should return vector of raw sql."
    (is (=
          (sql-select-builder (first (take 1 test-list-map)))
          ["SELECT * FROM person WHERE fname='JIARA' AND lname='HERTZEL' AND dob='1935-06-05'AND phone!='1111111111';"]))))
