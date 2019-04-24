(ns nsign.unstable.data.html.node)

(defrecord Element [tag attrs content])
(defrecord Comment [text])

(defn Element? [x]
  (instance? Element x))

(defn Comment? [x]
  (instance? Comment x))
