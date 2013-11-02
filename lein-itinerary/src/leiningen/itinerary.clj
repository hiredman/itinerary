(ns leiningen.itinerary
  (:refer-clojure :exclude [test])
  (:require [leiningen.test :refer [test form-for-testing-namespaces]]
            [clojure.pprint :refer [pprint]]))

(defn itinerary
  "I don't do a lot."
  [project & args]
  (let [o form-for-testing-namespaces]
    (try
      (alter-var-root #'form-for-testing-namespaces
                      (fn [original]
                        (fn [& args]
                          `(let [_# (require '~'clojure.test)
                                 o# @(resolve '~'clojure.test/run-tests)]
                             (require '~'com.thelastcitadel.itinerary)
                             (try
                               (alter-var-root #'clojure.test/run-tests
                                               (fn [o#]
                                                 (fn [& nses#]
                                                   (binding [clojure.test/*report-counters*
                                                             (ref clojure.test/*initial-report-counters*)]
                                                     ((~'resolve '~'com.thelastcitadel.itinerary/plumbing)
                                                      {}
                                                      (reverse
                                                       (rest
                                                        ((~'resolve '~'com.thelastcitadel.itinerary/deps)
                                                         ((~'resolve '~'com.thelastcitadel.itinerary/dep-graph))))))
                                                     @~'clojure.test/*report-counters*))))
                               ~(apply original args)
                               (finally
                                 (alter-var-root (resolve '~'clojure.test/run-tests)
                                                 (constantly o#))))))))
      (apply test project args)
      (finally
        (alter-var-root #'form-for-testing-namespaces
                        (constantly o))))))
