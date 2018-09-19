(ns xml-parser.core-test
  (:require [clojure.test :refer :all]
            [xml-parser.core :refer :all]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str])
            (:import java.sql.SQLException))

(comment (deftest batch-conditional-transaction!
  (testing "test conditional transaction batch query"

    ;(is (= (rec-count db-spec) 10000000))

    (is (=))


    ;(is (= (rec-count db-spec) 10000002)); two records inserted?
)))

;we are updating each time which is extremely wasteful!
(deftest single-conditional-transaction!
  (def rec (first '({:firstname "JIARA",
           :lastname "HERTZEL",
           :date-of-birth "1935-06-05",
           :phone "9999999999"})))

  (is (= (rec-count db-spec) 10000000))

  (testing "test conditional update transaction "
      (is (= (update-or-insert! db-spec :person
               rec
               (to-where-clause rec))
               ; we should only insert once. '(1)
                '(1))))
    (is (= (rec-count db-spec) 10000000))
    (is (= (update-or-insert! db-spec :person
             rec
             (to-where-clause rec))
             ; we should only insert once. '(1)
              '(1))))
    (is (= (rec-count db-spec) 10000000))
                ))
