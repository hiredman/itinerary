(ns com.thelastcitadel.itinerary.sort)

;; topo sort of graph as a map of nodes to sets of vertices

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
