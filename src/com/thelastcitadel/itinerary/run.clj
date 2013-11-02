(ns com.thelastcitadel.itinerary.run
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [com.thelastcitadel.itinerary.sort :as sort]))

(declare ^:dynamic *env*)

(def x-run-tests nil)
(def x-run-tests-fixture nil)

(defmulti x-run-tests (fn [_  x] (type x)))
(defmulti x-run-tests-fixture (fn [_ [tag]] tag))

(defmethod x-run-tests clojure.lang.IPersistentVector [{:keys [env] :as m} step]
  (assoc m
    :env (x-run-tests-fixture env step)))

(defmethod x-run-tests-fixture :before [env step]
  (try
    (case (count step)
      2 (assoc env (second step) ((:before @(second step))))
      4 (assoc env (last step) ((:before @(second step)))))
    (catch Throwable t
      (binding [*testing-vars* (conj *testing-vars* (second step))]
        (do-report {:type :error, :message "Uncaught exception, not in assertion."
                    :expected nil, :actual t}))
      env)))

(defmethod x-run-tests-fixture :after [env step]
  (case (count step)
    2 (do
        (try
          ((:after @(second step)) (get env (second step)))
          (dissoc env (second step))
          (catch Throwable t
            (binding [binding [*testing-vars* (conj *testing-vars* (second step))]]
              (do-report {:type :error, :message "Uncaught exception, not in assertion."
                          :expected nil, :actual t}))
            env)))
    4 (do
        (try
          ((:after @(second step)) (get env (last step)))
          (dissoc env (last step))
          (catch Throwable t
            (binding [*testing-vars* (conj *testing-vars* (second step))]
              (do-report {:type :error, :message "Uncaught exception, not in assertion."
                          :expected nil, :actual t}))
            env)))))

(defmethod x-run-tests clojure.lang.Var [{:keys [env results] :as m} step]
  (assoc m :results
         (conj results (let [env (reduce
                                  (fn [env [k v]]
                                    (assoc env k (get env v (get env k))))
                                  env
                                  (:itinerary/deps (meta step)))]
                         (binding [*env* (select-keys env (keys (:itinerary/deps (meta step))))]
                           (test-var step))))))

