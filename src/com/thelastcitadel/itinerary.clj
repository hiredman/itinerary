(ns com.thelastcitadel.itinerary
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [com.thelastcitadel.itinerary.sort :as sort]
            [com.thelastcitadel.itinerary.run :as run]))

(def deps sort/deps)

(defn depend-fn
  "add dependency information to var"
  [v m]
  (alter-meta! v assoc :itinerary/deps m)
  v)

(defmacro depend
  "add dependency information to var"
  [name & kvs]
  (let [ns (namespace name)
        name (symbol (clojure.core/name name))
        v (intern (if ns
                    (create-ns ns)
                    *ns*)
                  name)
        m (into {} (for [[k v] (partition 2 kvs)]
                     [k (resolve ns v)]))]
    `(depend-fn ~v ~m)))

(defn all-tests
  "return all vars with :test metadata"
  []
  (for [namespace (all-ns)
        [n v] (ns-publics namespace)
        :when (:test (meta v))]
    v))

(defn dep-graph
  "return a dependency graph of tests and fixtures"
  ([]
     (dep-graph (all-tests)))
  ([tests]
     (assoc (apply merge-with into
                   (for [v tests
                         [nk fixture] (:itinerary/deps (meta v))]
                     (if (contains? (:flags @fixture) :once)
                       {v #{[:after fixture] :all-tests}
                        [:before fixture] #{v}
                        [:after fixture] #{}}
                       {v #{[:after fixture v nk] :all-tests}
                        [:before fixture v nk] #{v}
                        [:after fixture v nk] #{}})))
       :all-tests #{})))

(defn plumbing
  "given an empty env {} and a list of things to do, do them"
  [env itinerary]
  (reduce run/x-run-tests env itinerary))

(defn env
  "return the test environment"
  []
  run/*env*)
