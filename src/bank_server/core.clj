(ns bank-server.core
  (:use compojure.core)
  (:use ring.adapter.jetty)
  ;(:use clj-time.format)
  (:use bank-server.views)
)

(def transactions (ref []))

(defn insert [lst x]
  (do
    (println "insert" x "into" lst)
    (cond
      (empty? lst) (list x)
      (> (get (first lst) "date") (get x "date")) (conj lst x)
      :else (conj (insert (rest lst) x) (first lst)))
  ))

(defn check [field transaction]
  (try
    (case field
      "account" (Integer/parseInt (transaction "account"))
      ;"date" (parse (formatters :date) (transaction "date"))
      "date" (Integer/parseInt (transaction "date"))
      "operation" (transaction "operation")
      "value" (Float/parseFloat (transaction "value"))
      "description" (transaction "description")
    )
  (catch Exception e
    (do (.printStackTrace e)
    false)))
)

(defn check_operation [transaction]
  (let [all_fields (reduce #(assoc %1 %2 (check %2 transaction))
                    {} (list "account" "date" "operation" "value" "description"))]
    (do
      ;(println "all_fields" all_fields)
      (list (reduce #(and %1 (not (empty? %2))) true all_fields) all_fields)
    )
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
    (let [account (check "account" (get req :params))
         value (reduce + 0 (map
           (fn [%]
             (let [value (get % "value")
                  date (get % "date")]
               (if (contains? (set '("Deposit" "Salary" "Credit")) (get % "operation"))
               value
               (* -1 value))
            ))
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
