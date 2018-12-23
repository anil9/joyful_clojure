(defproject joyful-clojure-05 "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [cheshire "5.8.1"]
                 [ring "1.7.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger "1.0.1"]
                 [compojure "1.6.1"]
                 [korma "0.4.3"]
                 [log4j "1.2.15"
                  :exclusions [javax.mail/mail
                               javax.jms/jms
                               com.sun.jdmk/jmxtools
                               com.sun.jmx/jmxri]]
                 [org.postgresql/postgresql "42.2.5"]
                 [ragtime "0.7.2"]
                 [environ "1.1.0"]
                 [clj-time "0.15.0"]
                 [com.taoensso/timbre "4.10.0"]]

  :plugins [[lein-environ "1.1.0"]]

  :source-paths ["src"]
  :test-paths ["tests"]
  :target-path "target/%s"

  :profiles
  {:dev {:env {:environment "development"}
         :source-paths ["src" "dev"]
         :repl-options {:port 8081}}

   :test {:env {:environment "test"}}

   :prod {:env {:environment "production"}
          :uberjar-name "app-standalone.jar"
          :main com.shortify.api.main
          :aot :all}})