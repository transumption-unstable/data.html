(ns yegortimoshenko.data.html
  (:refer-clojure :exclude [comment read read-string])
  (:require #?(:clj [clojure.core.match :refer [match]]
               :cljs [cljs.core.match :refer-macros [match]])
            [clojure.string :as str]
            [yegortimoshenko.data.html.nodes :as n]
            #?(:clj [yegortimoshenko.data.html.reader :as r])
            #?(:clj [yegortimoshenko.data.html.writer :as w])))

(defn element
  ([tag] (n/->Element tag {} ()))
  ([tag attrs] (n/->Element tag attrs ()))
  ([tag attrs & content] (n/->Element tag attrs content)))

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
    (n/->Comment text)))

(defn html
  "Builds HTML tree from Hiccup-like data structure."
  [data]
  (match data
    [[& _] & _] (mapcat html data)
    [tag (attrs :guard map?) & children] [(n/->Element tag attrs (mapcat html children))]
    [tag & children] [(n/->Element tag {} (mapcat html children))]
    :else [data]))

#?(:clj (def read r/read))
#?(:clj (def read-string r/read-string))

#?(:clj (def write w/write))
#?(:clj (def write-string w/write-string))
