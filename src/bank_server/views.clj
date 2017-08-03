(ns bank-server.views
  (:use hiccup.core)
  (:use hiccup.page-helpers))

(defn view-layout [& content]
  (html
    (doctype :xhtml-strict)
    (xhtml-tag "en"
      [:head
        [:meta {:http-equiv "Content-type"
                :content "text/html; charset=utf-8"}]
        [:title "Bank"]]
      [:body content])))

(defn view-operation-input [& warning]
  (view-layout
    [:h2 "Bank operation"]
    [:form {:method "post" :action "/operation"}
    ; Trocar :value por :placeholder
      [:span "Account:"]      [:input {:type "text" :name "account" :value "1000" :pattern "[0-9]{4}" :title "Formato correto: [0-9]{4}"}][:br]
      [:span "Date"]          [:input {:type "text" :name "date"    :value "12/10/2016" :pattern "[0-9]{2}/[0-9]{2}/[0-9]{4}" :title "Formato correto: [0-9]{2}/[0-9]{2}/[0-9]{4}"}][:br]
      ;[:span "Date"]          [:input {:type "text" :name "date"    :value "12" :pattern "[0-9]+" :title "Formato correto: [0-9]+"}][:br]
      [:span "Operation:"]    [:select {:name "operation"}
                                [:option {:value "Deposit"} "Deposit"]
                                [:option {:value "Salary"} "Salary"]
                                [:option {:value "Credit"} "Credit"]
                                [:option {:value "Purchase"} "Purchase"]
                                [:option {:value "Withdrawal"} "Withdrawal"]
                                [:option {:value "Debit"} "Debit"]]
      [:span "Value:"]        [:input {:type "text" :name "value" :value "10.00" :pattern "[0-9]+\\.[0-9]{2}" :title "Formato correto: [0-9]+\\.[0-9]{2}"}][:br]
      [:span "Description:"]  [:input {:type "textarea" :name "description" :placeholder "Description"}][:br]
      [:input.action {:type "submit" :value "OK"}]]
    [:p {:color "#AA0000"} warning]
  )
)

(defn view-operation-output [transaction transactions]
  (view-layout
    [:h2 "Operation successful!"]
    [:p "Account: " (get transaction "account")]
    [:p "Date:    " (get transaction "date")]
    [:h2 "Previous operations"]
    [:p (str transactions)]
    [:a.action {:href "/operation"} "New operation"]))

(defn view-balance-input [& warning]
  (view-layout
    [:form {:method "post" :action "/balance"}
      [:span "Account:"]      [:input {:type "text" :name "account" :value "1000"}][:br]
      [:input.action {:type "submit" :value "OK"}]]
    [:p warning]))

(defn view-balance-output [text]
  (html text))

(defn view-statement-input [& warning]
  (view-layout
    [:form {:method "post" :action "/statement"}
      [:span "Account:      "]      [:input {:type "text" :name "account" :value "1000"}][:br]
      [:span "Initial date: "]      [:input {:type "text" :name "initial"    :value "12/10/2016" :pattern "[0-9]{2}/[0-9]{2}/[0-9]{4}" :title "Formato correto: [0-9]{2}/[0-9]{2}/[0-9]{4}"}][:br]
      [:span "Final date:   "]      [:input {:type "text" :name "final"    :value "15/10/2016" :pattern "[0-9]{2}/[0-9]{2}/[0-9]{4}" :title "Formato correto: [0-9]{2}/[0-9]{2}/[0-9]{4}"}][:br]
      [:input.action {:type "submit" :value "OK"}]]
    [:p warning]))

(defn view-statement-output [text]
  (html text))
