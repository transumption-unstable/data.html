(ns yegortimoshenko.data.html.node
  (:refer-clojure :exclude [comment])
  (:require [clojure.string :as str]))

(defrecord Element [tag attrs content])
(defrecord Comment [text])

(defn element
  ([tag] (->Element tag {} ()))
  ([tag attrs] (->Element tag attrs ()))
  ([tag attrs & content] (->Element tag attrs content)))

(defn element? [x]
  (instance? Element x))

(defn comment
  "Creates a Comment node, validating its text against the standard:
  https://html.spec.whatwg.org/#comments

  Returns nil if validation fails."
  [text]
  (when-not (or (str/starts-with? text ">")
                (str/starts-with? text "->")
                (str/includes? text "<!--")
                (str/includes? text "-->")
                (str/includes? text "--!>")
                (str/ends-with? text "<!-"))
    (->Comment text)))

(defn comment? [x]
  (instance? Comment x))

(defn node? [x]
  (or (element? x)
      (comment? x)))
