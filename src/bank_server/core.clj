(ns bank-server.core
  (:use compojure.core)
  (:use ring.adapter.jetty)
  ;(:use clj-time.format)
  (:use bank-server.views)
)

(def transactions (ref []))

(defn insert [lst x]
  (cond
    (empty? lst) (list x)
    (> (get (first lst) "date") (get x "date")) (conj lst x)
    :else (conj (insert (rest lst) x) (first lst))))

(defn check_operation [transaction]
  (try
    [true (hash-map
      "account" (Integer/parseInt (transaction "account"))
      ;"date" (parse (formatters :date) (transaction "date"))
      "date" (Integer/parseInt (transaction "date"))
      "operation" (transaction "operation")
      "value" (Float/parseFloat (transaction "value"))
      "description" (transaction "description")
    )]
  (catch Exception e
    (do (.printStackTrace e)
    [false (str (.toString e))])))
)

(defn check_balance [transaction]
  (let [account (get transaction "account")]
    (try
      (Integer/parseInt account)
    (catch Exception e
      false))
  )
)

(defroutes app
  (GET "/operation" []
    (view-operation-input))

  (POST "/operation" req
  ;Verificação da entrada-> lança erro se formato incorreto
    (let [transaction_check (check_operation (get req :params))]
      (if (first transaction_check)
        (let [transaction (first (rest transaction_check))]
          (do (dosync
            (alter transactions insert transaction))
            (println "@transactions:" @transactions)
            (view-operation-output transaction @transactions)))
        (view-operation-input (str "Erro nos dados fornecidos: " (rest transaction_check)))
      )
    )
  )

  (GET "/balance" []
    (view-balance-input))

  (POST "/balance" req
    (let [account (check_balance (get req :params))
         value (reduce + 0 (map
           (fn [%]
             ;Temporary Integer/parseInt
             (let [value (get % "value")]
               (if (contains? (set '("Deposit" "Salary" "Credit")) (get % "operation"))
               value
               (* -1 value))))
            (filter #(= account (get % "account")) @transactions)))]
      (if account
        (view-balance-output (hash-map "account" account "value" value))
        (view-balance-input "Erro nos dados fornecidos.")
      )
    )
  )

  (GET "/statement" []
    (view-statement-input))

  (POST "/statement" req
    (let [params (get req :params)]
      (view-statement-output params)))
)

(defn -main [& args]
  (println "Hello, World!\n")
  (run-jetty app {:port 8080}))
