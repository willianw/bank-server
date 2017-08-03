(ns bank-server.core
  (:use compojure.core)
  (:use ring.adapter.jetty)
  (:use bank-server.views)
)

(def transactions (ref []))

(defn insert [lst x]
  (do
    (println "insert" x "into" lst)
    (cond
      (empty? lst) (list x)
      (.after (get (first lst) "date") (get x "date")) (conj lst x)
      :else (conj (insert (rest lst) x) (first lst)))
  ))

(defn check [field_name type transaction]
  (println "check" field_name type transaction)
  (try
    (let [value (transaction field_name)]
      (case type
        "integer" (Integer/parseInt value)
        "float" (Float/parseFloat value)
        ;"date" (Integer/parseInt value)
        "date" (.parse (java.text.SimpleDateFormat. "dd/MM/yyyy") value)
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

(defn tomorrow []
  (let [today (java.util.Calendar/getInstance)]
    (.add today java.util.Calendar/DAY_OF_YEAR 1)
  (.getTime today)))


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
                (if (.before date (.getTime (java.util.Calendar/getInstance)))
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
    (println req)
    (let [params (get req :params)
          account (check "account" "integer" params)
          initial (check "initial" "date" params)
          final   (check "final" "date" params)]
          (println "account" account "initial" initial "final" final)
          (if (and account initial final)
            (let [days    (distinct (map #(% "date") @transactions))
                  filter1 (filter #(and (.after (% "date") initial) (.before (% "date") final)) @transactions)
                  statements (reduce (fn [list, day]
                                    (do
                                      (conj list (hash-map "date" day "transactions" (filter #(= 0 (.compareTo (% "date") day)) filter1)))))
                                  [] days)]
              (do
                (view-statement-output (str statements))))
            (view-statement-input "Houve um erro")
          )))
)

(defn -main [& args]
  (println "Hello, World!\n")
  (run-jetty app {:port 8080}))
