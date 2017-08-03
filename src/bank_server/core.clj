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

(defn check [field_name type transaction]
  (try
    (let [value (transaction field_name)]
      (case type
        "integer" (Integer/parseInt value)
        "float" (Float/parseFloat value)
        "date" (Integer/parseInt value)
        ;"date" (parse (formatters :date) (transaction "date"))
        "text" value
      ))
  (catch Exception e
    (do (.printStackTrace e)
    false)))
)

(defn check_operation [transaction]
  (let [all_fields (hash-map
                        "account"       (check "account" "integer" transaction)
                        "date"          (check "date" "date" transaction)
                        "operation"     (check "operation" "text" transaction)
                        "value"         (check "value" "float" transaction)
                        "description"   (check "description" "text" transaction)
                        )]
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

;TO-DO: contar apenas transações anteriores a hoje
  (POST "/balance" req
    (let [account (check "account" "integer" (get req :params))
         value (reduce + 0 (map
           (fn [%]
             (let [value (get % "value")
                  date (get % "date")]
                ;TO-DO: trocar a comparação abaixo usando date
                (if (<= date 13)
                  (if (contains? (set '("Deposit" "Salary" "Credit")) (get % "operation"))
                  value
                  (* -1 value))
                  0)
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
    (let [params (get req :params)
          account (check "account" "integer" params)
          initial (check "initial" "date" params)
          final   (check "final" "date" params)
          days    (distinct (map #(% "date") @transactions))
          filter1 (filter #(and (> (% "date") initial) (< (% "date") final)) @transactions)
          list    (reduce (fn [list, day]
                            (do
                              (println "filter2 in " day (filter #(= (% "date") day) filter1))
                              (conj list (hash-map "date" day "transactions" (filter #(= (% "date") day) filter1)))))
                          [] days)]
      (do
        (println "days" days)
        (println "filter1" filter1)
        (view-statement-output (str list)))))
)

(defn -main [& args]
  (println "Hello, World!\n")
  (run-jetty app {:port 8080}))
