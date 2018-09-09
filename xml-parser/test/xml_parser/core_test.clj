(ns xml-parser.core-test
  (:require [clojure.test :refer :all]
            [xml-parser.core :refer :all]
            [clojure.java.jdbc :as jdbc]))

(deftest test-sql-select-builder
  (testing "sql-select-builder, should return vector of raw sql."
    (is (=
          (sql-select-builder (first (take 1 test-list-map)))
          ["SELECT * FROM person WHERE fname='JIARA' AND lname='HERTZEL' AND dob='1935-06-05'AND phone!='9999999999';"]))))

;This test is mutating the actual db, shoudl learn how to stub
(deftest test-query!
  (testing "query with small map updates/inserts sql queries updates the db."
    (is (=
          (query-runner test-list-map sql-upsert-builder)
          '("caught exception: No results were returned by the query." "caught exception: No results were returned by the query.")))

    (is (=
          (query-runner test-list-map sql-select-contraint-builder)
          (quote (({:fname "JIARA", :lname "HERTZEL", :dob #inst "1935-06-04T23:00:00.000-00:00", :phone "9999999999"}) ({:fname "00226501", :lname "MCGREWJR", :dob #inst "1936-01-31T23:00:00.000-00:00", :phone "9999999999"})))
          )))

    ;Restore state back to value before upate on two people
    ;revert state; TODO look up original value
    (def orig-phone-Jiara "foo")
    (def orig-phone-MCGREWJR "foo")

    (try
      (jdbc/query db-spec (sql-upsert-builder {:firstname "JIARA", :lastname "HERTZEL", :date-of-birth "1935-06-05", :phone "1111111111"}))
    (catch Exception e (str "caught exception: "                     (.getMessage e))))
    (try
      (jdbc/query db-spec (sql-upsert-builder {:firstname "00226501", :lastname "MCGREWJR", :date-of-birth "1936-02-01", :phone "1111111111"}))
    (catch Exception e (str "caught exception: "                     (.getMessage e)))))

;If this completes without GC then I could filter filter and count the result set...print it?
;Did not complete after two hours!
(deftest test-query-large
 (testing "transaction query with updates/inserts followed by select ;shows success.Can process the entire update.xml file"
  (def actual (query-runner list-map sql-upsert-builder))
 ;(print actual)
  (is (= actual
         "Can process the entire update.xml file"))))

;I dont think this will ever work because jdbc throws an exception on Successful updates with error :"caught exception: No results were returned by the query."
;
;(deftest trans-query-large
;  (testing "transaction query with updates/inserts followed by select shows success."
;    (is (=
;           (trans-query test-list-map sql-upsert-builder s;ql-select-contraint-builder)
;           ""))))
