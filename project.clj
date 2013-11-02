(defproject com.thelastcitadel/itinerary "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[com.thelastcitadel/lein-itinerary "0.1.0-SNAPSHOT"]]
  :test-selectors {:default (complement :regression)
                   :regression :regression}
  :dependencies [[org.clojure/clojure "1.5.1"]])
