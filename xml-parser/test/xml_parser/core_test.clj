(ns xml-parser.core-test
  (:require [clojure.test :refer :all]
            [xml-parser.core :refer :all]
            [clojure.java.jdbc :as jdbc]))

(deftest a-test

  ;(testing "Can process the entire update.xml file"
  ;(is (=
  ;        (query list-map sql-upsert-builder)
  ;         ?)))

  ;(testing "Can deduce how many upserts/updates should occur from update.xml file"
  ;  (is (=
  ;         (query list-map sql-select-builder)
  ;         ?)))
  ;TODO synch db and test data
  (def test-list-map '({:firstname "JIARA", :lastname "HERTZEL", :date-of-birth "1935-06-05", :phone "1111111111"} {:firstname "00226501", :lastname "MCGREWJR", :date-of-birth "1936-02-01", :phone "1111111111"} {:firstname "shakrah", :lastname "yves", :date-of-birth "1936-02-01", :phone "1111111111"}))

  (testing "query with updates/inserts sql queries do not return from jdbc, but do update the db."
    (is (=
           (query test-list-map sql-upsert-builder)
           '("caught exception: No results were returned by the query." "caught exception: No results were returned by the query." "caught exception: No results were returned by the query."))))

  (testing "query with select sql queries returns results from jdbc."
    (is (=
           (query test-list-map sql-select-builder)
           (() ({:fname "00226501", :lname "MCGREWJR", :dob #inst "1936-01-31T23:00:00.000-00:00", :phone "9796740198"}) ()))))

  (testing "sql-select-builder, should return vector of raw sql."
    (is (=
          (sql-select-builder (first (take 1 test-list-map)))
          ["SELECT * FROM person WHERE fname='JIARA' AND lname='HERTZEL' AND dob='1935-06-05'AND phone!='2222222222';"])))

  (testing "test contraint for update, raw query works."
    (is (=
          (jdbc/query db-spec ["SELECT * FROM person WHERE fname='JIARA' AND lname='HERTZEL' AND dob='1935-06-05'AND phone!='1111111111';"])
          ({:fname "JIARA", :lname "HERTZEL", :dob #inst "1935-06-04T23:00:00.000-00:00", :phone "2222222222"})))))
