(ns xml-parser.core-test
  (:require [clojure.test :refer :all]
            [xml-parser.core :refer :all]
            [clojure.java.jdbc :as jdbc]))

;builder for a single query
(comment (deftest test-sql-select-builder
  (testing "sql-select-builder, should return vector of raw sql."
    (is (=
          (singleton-sql-select-builder (first (take 1 test-list-map)))
          ["SELECT * FROM person WHERE fname='JIARA' AND lname='HERTZEL' AND dob='1935-06-05'AND phone!='9999999999';"])))))

;This test is mutating the actual db, shoudl learn how to stub
(comment (deftest test-query!
  (testing "query with small map updates/inserts sql queries updates the db."

    ;record to be updated exists
    (is (= (jdbc/query db-spec ["SELECT * FROM person WHERE fname='JIARA' and lname='HERTZEL';"])
           (quote ({:fname "JIARA", :lname "HERTZEL", :dob #inst "1935-06-04T23:00:00.000-00:00", :phone "5859012134"}))
           ))

    ;record to be created does not exist
    (is (= (jdbc/query db-spec ["SELECT * FROM person WHERE fname='00226501' and lname='MCGREWJR';"])
            ()))

    ;updating and inserting returns only exceptions
    (is (=
          (query-runner test-list-map sql-upsert-builder)
          '("caught exception: No results were returned by the query." "caught exception: No results were returned by the query.")))

    ;existing person is updated
    ;non existant person is created
    (is (= (query-runner test-list-map sql-select-contraint-builder)
            (quote (({:fname "JIARA", :lname "HERTZEL", :dob #inst "1935-06-04T23:00:00.000-00:00", :phone "9999999999"}) ({:fname "00226501", :lname "MCGREWJR", :dob #inst "1936-01-31T23:00:00.000-00:00", :phone "9999999999"})))))

    ;restore the state db to state before insert and update
    (try
      (jdbc/query db-spec (sql-upsert-builder {:firstname "JIARA", :lastname "HERTZEL", :date-of-birth "1935-06-05", :phone "5859012134"}))
    (catch Exception e (str "caught exception: "                     (.getMessage e))))
    (try
      (jdbc/query db-spec ["delete FROM person WHERE fname='00226501' and lname='MCGREWJR';"])
    (catch Exception e (str "caught exception: "                     (.getMessage e))))
    )))

    ; I may need to run the three tests with a database state cleanup in between? but I think the first 100 are garbage could try with a select?

(comment (deftest query-upsert-runtime!
      (testing "query with small map updates/inserts time for each query."
        ;updating and inserting returns only exceptions
        (is (= (time (query-runner (take 1 list-map) singleton-sql-upsert-builder)) "Elapsed time: 0.0152 msecs"))

        ;this is alot faster than I would have expected, faster than it took for a single update?
        ;(is (= (time (query-runner (take 100 list-map) sql-upsert-builder))   "Elapsed time: 0.013707 msecs"))

        ;4 times slower than I would expect
        ;(is (= (time (query-runner (take 10 list-map) sql-upsert-builder)) "Elapsed time: 0.571054 msecs"))
        (is (= 2 2)))))

(deftest with-db-conn-runtime!
  (def batch10 (batch-query (take 10 list-map) sql-upsert-builder))
  (def batch100 (batch-query (take 100 list-map) sql-upsert-builder))

  (testing "test time batch query with ddl do commands."
    (is (= (time (batch-query-runner batch10)) "Elapsed time: 9.801417 msecs"))
    ;(is (= (time (batch-query-runner batch100)) "Elapsed time: 0.244202 msecs"))
    (is (= 2 2))))


(comment (deftest with-db-conn-runtime!
    (def batch10 (batch-query (take 10 list-map) sql-upsert-builder))
    (def batch100 (batch-query (take 100 list-map) sql-upsert-builder))

    (testing "test time batch query with db-connection."
      (is (= (time (batch-query-with-db-con batch10)) "Elapsed time: 9.801417 msecs"))
      (is (= (time (batch-query-with-db-con batch100)) "Elapsed time: 0.244202 msecs"))
      (is (= 2 2)))))
