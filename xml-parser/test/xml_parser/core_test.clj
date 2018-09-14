(ns xml-parser.core-test
  (:require [clojure.test :refer :all]
            [xml-parser.core :refer :all]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str])
            (:import java.sql.SQLException))

(comment (deftest batch-conditional-transaction!
  (testing "test conditional transaction batch query"
    (is (= (batch-query-with-db-con-2 (take 10 list-map) db-spec :person)
    "foo"))))
)

(deftest single-conditional-transaction!
  (def rec (first '({:firstname "JIARA",
           :lastname "HERTZEL",
           :date-of-birth "1935-06-05",
           :phone "9999999999"})))

  (testing "test conditional update transaction "
      (is (= (update-or-insert! db-spec :person
               rec
               ["fname = ? AND lname = ? AND dob = CAST (? AS DATE) AND phone <> ?"
                (get rec :firstname "")
                (get rec :lastname "")
                (get rec :date-of-birth "")
                (get rec :phone "")])
                "foo"))))
