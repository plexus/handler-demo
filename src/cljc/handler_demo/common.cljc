(ns handler-demo.common)

(defn shared-fn
  "A function that is shared between clj and cljs"
  []
  (println "cljc!"))

