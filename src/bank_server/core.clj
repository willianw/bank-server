(ns bank-server.core
	(:use compojure.core)
	(:use ring.adapter.jetty)
	(:use bank-server.views)
	(:require [clj-time.core :as time])
	(:require [clj-time.format :as time-format]))

(def transactions (ref []))

(defn insert [lst x]
	(do
		;(println "insert" x "into" lst)
		(cond	(empty? lst) (list x)
				(time/after? (get (first lst) "date") (get x "date")) (conj lst x)
				:else (conj (insert (rest lst) x) (first lst)))))

(defn check [field_name type transaction]
	;(println "check" field_name type transaction)
	(try
		(let [value (transaction field_name)]
		(case type
			"integer" (Integer/parseInt value)
			"float" (Float/parseFloat value)
			;"date" (Integer/parseInt value)
			"date" (time-format/parse (time-format/formatter "dd/MM/yyyy") value)
			"text" value
			))
			(catch Exception e
				(do (.printStackTrace e)
				false))))

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
			(list (reduce #(and %1 (not (empty? %2))) true all_fields) all_fields))))

(defn balance [account time transactions]
	(reduce (fn [sum transaction]
				(let	[value (transaction "value")
						delta	(if (contains? (set '("Deposit" "Salary" "Credit")) (transaction "operation")) value (* -1 value))]
						(+ sum delta)))
			0 (filter #(and (= account (get % "account")) (time/before? (get % "date") time)) transactions)))

(defn statements [transactions account & [initial final]]
	(let	[days	(distinct (map #(% "date") transactions))
			filter1	(if (not (or initial final))
						transactions
						(filter #(time/within? (time/interval initial final)) transactions))]
			(reduce (fn	[list day]
				(conj list (hash-map	"date" (time-format/unparse (time-format/formatter :date) day)
										"transactions" (filter #(time/equal? (get % "date") day) filter1)
										"balance" (balance account day transactions))))
				(list) days)
	)
)

(defroutes app
	(GET "/operation" []
		(view-operation-input))

	(POST "/operation" req
		(println "POST requisition:" (req :body))
		(let [transaction_check (check_operation (get req :params))]
			(if (first transaction_check)
				(let [transaction (first (rest transaction_check))]
					(do (dosync
						(alter transactions insert transaction))
						;(println "@transactions:" @transactions)
						(view-operation-output transaction @transactions)))
				(view-operation-input (str "Erro nos dados fornecidos: " (rest transaction_check))))))

	(GET "/balance" []
		(view-balance-input))

	(POST "/balance" req
		(let [account (check "account" "integer" (get req :params))
				value (balance account (time/now) @transactions)]
			(if account
				(view-balance-output (hash-map "account" account "value" value))
				(view-balance-input "Erro nos dados fornecidos."))))

	(GET "/statement" []
		(view-statement-input))

	(POST "/statement" req
		(let	[params (get req :params)
				account (check "account" "integer" params)
				initial (check "initial" "date" params)
				final   (check "final" "date" params)]
		(if (and account initial final)
			(do (map #(println %) @transactions)
			(view-statement-output (statements @transactions account initial final)))
			(view-statement-input "Houve um erro"))
		))

	(GET "/debt" []
		(view-debt-input))

	(POST "/debt" req
		(let [account (check "account" "integer" (get req :params))]
			(if account
				(view-debt-output (reduce (fn [list statement]
						(let	[value (get statement "balance")
								date (get statement "date")
								last (last statement)
								rest (drop-last statement)
								period {:principal value :start date}]
							(do
								(println "value" value)
								(println "date" date)
							(cond
								(and (< value 0) (contains? (last list) :end)) (conj statement period)
								(and (< value 0) (not (empty? list)) (not (contains? (last list) :end)) (conj (conj rest (assoc last :end date)) period))
								(and (> value 0) (not (empty? list)) (not (contains? (last list) :end)) (conj rest (assoc last :end date)))
							))
						)
					) (list) (statements @transactions account)))
				(view-debt-input "Houve um erro")
			)
		)
	)
)

(defn -main [& args]
	(println "Hello, World!\n")
	(run-jetty app {:port 8080}))
