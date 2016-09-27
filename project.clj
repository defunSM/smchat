(defproject smchat "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [seesaw "1.4.2" :exclusions [org.clojure/clojure]]
                 [com.github.insubstantial/substance "7.1"]
                 [org.clojure/tools.nrepl "0.2.11"]
                 [clj-time "0.12.0"]]
  :plugins [[cider/cider-nrepl "0.9.1"]]
  :profiles {:uberjar {:aot [smchat.core]}} ;; add this
  :jar-name "smchat.jar" ;; add this
  :uberjar-name "smchat-uber.jar" ;; add this
  :main smchat.core) ;; add this
