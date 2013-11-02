(ns com.thelastcitadel.itinerary-test
  (:require [clojure.test :refer :all]
            [com.thelastcitadel.itinerary :refer :all]))

;; fixtures
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

;; tests
(deftest ^:regression t-foo
  (prn (env))
  (is false))
;; has to go after because the def in deftest clears the metadata,
;; very sad
(depend t-foo :foo foo :bar bar)

(deftest t-bar
  (prn (env))
  (is true))
(depend t-bar :foo foo :bar bar)
