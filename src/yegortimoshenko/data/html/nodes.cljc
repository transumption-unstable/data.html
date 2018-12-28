(ns yegortimoshenko.data.html.nodes)

(defrecord Element [tag attrs content])
(defrecord Comment [text])
