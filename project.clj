(defproject bank-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.8.0"]
                  [org.clojure/clojure "1.2.0"]
                  [org.clojure/clojure-contrib "1.2.0"]
                  [ring/ring-core "0.2.5"]
                  [ring/ring-devel "0.2.5"]
                  [ring/ring-jetty-adapter "0.2.5"]
                  [compojure "0.4.0"]
                  [hiccup "0.2.6"]
                  ;[clj-time "0.14.0"]
                ]
  :main ^:skip-aot bank-server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
