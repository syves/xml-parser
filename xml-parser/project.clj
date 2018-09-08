(defproject xml-parser "0.1.0-SNAPSHOT"
  :description "Efficient db insertion from xml"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"] ;from 1.8
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [proto-repl "0.3.1"]
                 [commons-io/commons-io "2.5"]
                 [honeysql "0.9.3"]
                 [nilenso/honeysql-postgres "0.2.4"]]
  :main xml-parser.core/test-lein-time)
