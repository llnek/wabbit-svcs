;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defproject io.czlab/wabbit-shared "1.1.0"

  :license {:url "http://www.eclipse.org/legal/epl-v10.html"
            :name "Eclipse Public License"}
  :url "https://github.com/llnek/wabbit-shared"

  :description "Sharable parts of wabbit."

  :dependencies [[commons-io/commons-io "2.6"]]

  :plugins [[cider/cider-nrepl "0.22.2"]
            [lein-codox "0.10.7"]
            [lein-cprint "1.3.1"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]]}
             :uberjar {:aot :all}}

  :global-vars {*warn-on-reflection* true}
  :coordinate! "czlab/wabbit/shared"
  :target-path "out/%s"
  :omit-source true
  :aot :all


  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  :resource-paths ["src/main/resources"])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF

