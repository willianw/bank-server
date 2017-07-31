(ns bank-server.core
  (:use compojure.core)
  (:use ring.adapter.jetty)
  (:use bank-server.views))

(def transactions (ref []))

(defn insert [lst x]
  (println "insert" x "in" lst)
  (cond
    (empty? lst) (list x)
    (> (Integer/parseInt (get (first lst) "date")) (Integer/parseInt (get x "date"))) (conj lst x)
    :else (conj (insert (rest lst) x) (first lst))))

(defroutes app
  (GET "/operation" []
    (view-operation-input))

  (POST "/operation" req
  ;Verificação da entrada-> lança erro se formato incorreto
    (let [transaction (get req :params)]
      (println (get transaction "description"))
      (dosync
        (alter transactions insert transaction))
      (view-operation-output transaction @transactions)))

  (GET "/balance" []
    (view-balance-input))

  ; (POST "/balance" [account p q r s]
  ;   (let [account (get (get req :params) "account")]
  ;     (view-balance-output (reduce (fn [transaction, x]
  ;                                     (if (contains? {"Deposit", "Salary", "Credit"} (transaction "operation")
  ;                                       (concat x ", " (transaction "value"))
  ;                                       (concat "-" x ", " (transaction "value"))
  ;                                     )))
  ;                           0 (filter #(= account (get % "account")) @transactions))))))

  (POST "/balance" req
    (let [account (get (get req :params) "account")]
    (view-balance-output (map (fn [%]
                            (get % "operation")) (filter #(= account (get % "account")) @transactions))))))

(defn -main [& args]
  (println "Hello, World!\n")
  (run-jetty app {:port 8080}))
