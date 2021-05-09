(defproject vip-8 "1.0"
  :description "A Chip 8 emulator with Vim philosophy"
  :url "https://github.com/psa-anddev/vip-8"
  :license {:name "GPL v 3"
            :url "https://www.gnu.org/licenses/gpl-3.0.html"}
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
                       :jvm-opts ["-Dcljfx.skip-javafx-initialization=true"]}
             :test {:jvm-opts ["-Dcljfx.skip-javafx-initialization=true"]}})
