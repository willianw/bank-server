(ns bank-server.core
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.adapter.jetty))

(def transactions (ref []))

(defn view-layout [& content]
  (html
    (doctype :xhtml-strict)
    (xhtml-tag "en"
      [:head
        [:meta {:http-equiv "Content-type"
                :content "text/html; charset=utf-8"}]
        [:title "Bank"]]
      [:body content])))

(defn view-operation-input []
  (view-layout
    [:h2 "Bank operation"]
    [:form {:method "post" :action "/operation"}
      [:span "Account:"]      [:input {:type "text" :name "account"}][:br]
      [:span "Date"]          [:input {:type "text" :name "date"}][:br]
      [:span "Operation:"]    [:select {:name "operation"}
                                [:option {:value "Deposit"} "Deposit"]
                                [:option {:value "Purchase"} "Purchase"]
                                [:option {:value "Withdrawal"} "Withdrawal"]]
      [:span "Value:"]        [:input {:type "text" :name "value"}][:br]
      [:span "Description:"]  [:input {:type "text" :name "description"}][:br]
      [:input.action {:type "submit" :value "OK"}]]))

(defn html-transaction [{:keys [account date operation value description]}]
  [:p
    [:span "account" account]
    [:span "date" date]
    [:span "operation" operation]
    [:span "value" value]
    [:span "description" description]]
  )

(defn insert [lst x]
  (println "insert" x "in" lst)
  (cond
    (empty? lst) (list x)
    (> (Integer/parseInt (get (first lst) :date)) (Integer/parseInt (get x :date))) (conj lst x)
    :else (conj (insert (rest lst) x) (first lst))))


(defn view-operation-output [transaction]
  ;(let [[account operation value description] (parse-input account operation value description)]
  ;Verificação da entrada-> lança erro se formato incorreto
  (println "before: " transactions)
  (dosync
    (alter transactions insert transaction))
  (println "after: " transactions)

  (view-layout
    [:h2 "Operation successful!"]
    [:p "Account: " (get transaction :account)]
    [:p "Date:    " (get transaction :date)]
    (map #(html-transaction %) @transactions)
    ;[{:account account :operation operation :value value :description description}])
    [:a.action {:href "/operation"} "New operation"]))

(defroutes app
  (GET "/operation" []
    (view-operation-input))

  (POST "/operation" [account date operation value description]
    (view-operation-output {:account account :date date :operation operation :value value :description description})))

(defn -main [& args]
  (println "Hello, World!\n")
  (run-jetty app {:port 8080}))
