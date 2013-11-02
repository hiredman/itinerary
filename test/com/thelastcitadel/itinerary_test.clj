(ns com.thelastcitadel.itinerary-test
  (:require [clojure.test :refer :all]
            [com.thelastcitadel.itinerary :refer :all]))

(def foo {:before (fn []
                    (println "foo created")
                    (reify
                      java.io.Closeable
                      (close [_]
                        (println "foo closed"))))
          :after (fn [x]
                   (.close x))
          :flags #{:each}})

(def bar {:before (fn []
                    (println "bar created")
                    (reify
                      java.io.Closeable
                      (close [_]
                        (println "bar closed"))))
          :after (fn [x]
                   (.close x))
          :flags #{:once}})

(deftest t-foo
  (is true))
(depend t-foo :foo foo :bar bar)

(deftest t-bar
  (is true))
(depend t-bar :foo foo :bar bar)

(comment



  (doseq [x (reverse (rest (deps (dep-graph))))] (prn x))


  {[:after #'com.thelastcitadel.itinerary-test/foo #'com.thelastcitadel.itinerary-test/t-foo] #{#'com.thelastcitadel.itinerary-test/t-foo},
   #'com.thelastcitadel.itinerary-test/t-foo #{[:before #'com.thelastcitadel.itinerary-test/foo #'com.thelastcitadel.itinerary-test/t-foo]},
   #'com.thelastcitadel.itinerary-test/t-bar #{[:before #'com.thelastcitadel.itinerary-test/foo #'com.thelastcitadel.itinerary-test/t-bar]},
   [:after #'com.thelastcitadel.itinerary-test/foo #'com.thelastcitadel.itinerary-test/t-bar] #{#'com.thelastcitadel.itinerary-test/t-bar}}


  {[:after #'com.thelastcitadel.itinerary-test/foo #'com.thelastcitadel.itinerary-test/t-foo] #{#'com.thelastcitadel.itinerary-test/t-foo},
   #'com.thelastcitadel.itinerary-test/t-foo #{[:before #'com.thelastcitadel.itinerary-test/foo #'com.thelastcitadel.itinerary-test/t-foo]},
   [:after #'com.thelastcitadel.itinerary-test/bar] #{#'com.thelastcitadel.itinerary-test/t-bar},
   #'com.thelastcitadel.itinerary-test/t-bar #{[:before #'com.thelastcitadel.itinerary-test/bar] [:before #'com.thelastcitadel.itinerary-test/foo #'com.thelastcitadel.itinerary-test/t-bar]},
   [:after #'com.thelastcitadel.itinerary-test/foo #'com.thelastcitadel.itinerary-test/t-bar] #{#'com.thelastcitadel.itinerary-test/t-bar}}

  )
