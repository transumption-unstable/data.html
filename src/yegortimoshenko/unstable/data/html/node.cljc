(ns yegortimoshenko.unstable.data.html.node
  (:refer-clojure :exclude [comment]))

(defrecord Element [tag attrs content])
(defrecord Comment [text])

(defn Element? [x]
  (instance? Element x))

(defn Comment? [x]
  (instance? Comment x))
