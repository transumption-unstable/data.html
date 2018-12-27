(ns yegortimoshenko.data.html
  (:refer-clojure :exclude [comment read read-string])
  (:require #?(:clj [clojure.core.match :refer [match]]
               :cljs [cljs.core.match :refer-macros [match]])
            [yegortimoshenko.data.html.types :as t]
            #?(:clj [yegortimoshenko.data.html.reader :as r])
            #?(:clj [yegortimoshenko.data.html.writer :as w])))

(defn element
  ([tag] (t/->Element tag {} ()))
  ([tag attrs] (t/->Element tag attrs ()))
  ([tag attrs & content] (t/->Element tag attrs content)))

(defn comment [content]
  (t/->Comment content))

(defn html
  "Build HTML DOM tree from Hiccup-like data structure."
  [data]
  (match data
    [[& _] & _] (mapcat html data)
    [tag (attrs :guard map?) & children] [(t/->Element tag attrs (mapcat html children))]
    [tag & children] [(t/->Element tag {} (mapcat html children))]
    :else [data]))

#?(:clj (def read r/read))
#?(:clj (def read-string r/read-string))

#?(:clj (def write w/write))
#?(:clj (def write-string w/write-string))
