(ns com.thelastcitadel.itinerary
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]))

(defn g> [g a b]
  (letfn [(ann [item]
            (for [dep (get g item)
                  i (cons dep (ann dep))]
              i))]
    (not (contains? (set (ann a)) b))))

(defn merge-sorted [gt [x & xs] [y & ys]]
  (lazy-seq
   (cond
    (and (nil? x) (nil? y)) nil
    (nil? x) (cons y ys)
    (nil? y) (cons x xs)
    (= y x) (cons x (merge-sorted gt xs ys))
    (gt x y) (cons x (merge-sorted gt xs (cons y ys)))
    (gt y x) (cons y (merge-sorted gt (cons x xs) ys))
    :else (list* x y (merge-sorted gt xs ys)))))

(defn deps* [start g]
  (letfn [(visit [n already-seen]
            (assert (not (contains? already-seen n)) "has no cycles")
            (let [already-seen (conj already-seen n)]
              (concat
               (for [[node deps] g
                     :when (contains? (get g node) n)
                     i (visit node already-seen)]
                 i)
               [n])))]
    (reverse (distinct (visit start #{})))))

(defn deps [g]
  (reduce (partial merge-sorted (partial g> g))
          (for [[node deps] g]
            (deps* node g))))

(defn depend-fn [v m]
  (alter-meta! v assoc ::deps m)
  v)

(defmacro depend [name & kvs]
  (let [ns (namespace name)
        name (symbol (clojure.core/name name))
        v (intern (if ns
                    (create-ns ns)
                    *ns*)
                  name)
        m (into {} (for [[k v] (partition 2 kvs)]
                     [k (resolve ns v)]))]
    `(depend-fn ~v ~m)))

(defn all-tests []
  (for [namespace (all-ns)
        [n v] (ns-publics namespace)
        :when (:test (meta v))]
    v))

(defn filter-tests [selector tests]
  (filter (comp selector meta) tests))

(defn dep-graph
  ([]
     (dep-graph (all-tests)))
  ([tests]
     (assoc (apply merge-with into
                   (for [v tests
                         [nk fixture] (::deps (meta v))]
                     (if (contains? (:flags @fixture) :once)
                       {v #{[:after fixture] :all-tests}
                        [:before fixture] #{v}
                        [:after fixture] #{}}
                       {v #{[:after fixture v nk] :all-tests}
                        [:before fixture v nk] #{v}
                        [:after fixture v nk] #{}})))
       :all-tests #{})))

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
                                  (::deps (meta step)))]
                         (binding [*env* (select-keys env (keys (::deps (meta step))))]
                           (test-var step))))))

(defn plumbing [env itinerary]
  (reduce x-run-tests env itinerary))
