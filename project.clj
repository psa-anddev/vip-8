(defproject vip-8 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-auto "0.1.3"]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.3.610"]
                 [cljfx "1.7.13"]
                 [org.openjfx/javafx-base "16-ea+6"]
                 [org.openjfx/javafx-graphics "16-ea+6"]
                 [org.openjfx/javafx-controls "16-ea+6"]]
  :repl-options {:init-ns vip-8.core}
  :main vip-8.core
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dcljfx.skip-javafx-initialization=true"]}})
