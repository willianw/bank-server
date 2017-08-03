(ns bank-server.core-test
  (:require [clojure.test :refer :all]
            [bank-server.core :refer :all]))

(def simple-operation []
  ;(testing "simple-operation"
    (println (bank-server.core/app/POST "/operation" hash-map "account" "1000" "date" "12/10/2016" "operation" "Deposit" "value" "12.50" "description" "desc"))
    (is (= 0 1))
  ;)
  )
