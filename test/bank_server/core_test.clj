(ns bank-server.core-test
  (:require [clojure.test :refer :all]
            [bank-server.core :refer :all]
            [bank-server.views :refer :all])
  (:require [clojure.data.json :as json])
  (:require [clj-http.client :as http])
  (:require [ring.mock.request :as mock]))

(deftest a-test
  (testing "GET Operation input"
            ;(println (http/get "http://localhost:8080/operation" {:query-params {"value1" "input"}}))
            (let [resp (app (-> (mock/request :get  "/operation")))]
            (println "resp" resp)
            (is (= (:status resp) 200))))
  (testing "POST Operation input"
            ;(println (http/get "http://localhost:8080/operation" {:query-params {"value1" "input"}}))
              (let [pizza {:name "Turtle Pizza"
                           :description "Pepperoni pizza"
                           :size :L
                           :origin {
                             :country :FI
                             :city "MyCity"}}
                    params {
                      "account" "1000"
                      "date" "12/10/2016"
                      "operation" "Deposit"
                      "value" "10.00"
                      "description" ""}

                    resp (app (-> (mock/request :post "/operation")
                                  (mock/content-type "application/json")
                                  (mock/body  (json/write-str params))))]
            (println "Response:" resp)
            (is (= (:status resp) 200)))
  )
)
