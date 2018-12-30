(ns yegortimoshenko.unstable.data.html
  (:import (clojure.lang IPersistentVector ISeq))
  (:refer-clojure :exclude [comment read read-string])
  (:require [yegortimoshenko.unstable.data.html.node :as node]
            #?(:clj [yegortimoshenko.unstable.data.html.reader :as r])
            #?(:clj [yegortimoshenko.unstable.data.html.writer :as w])))

(defprotocol HTML
  (html [this]))

(defn ^:private attrs? [x]
  (and (map? x)
       (not (node/Element? x))
       (not (node/Comment? x))))

(extend-protocol HTML
  IPersistentVector
  (html [[tag ?attrs & content]]
    [(if (attrs? ?attrs)
      (node/->Element tag ?attrs (html content))
      (node/->Element tag {} (html (cons ?attrs content))))])
  ISeq
  (html [this] (mapcat html this))
  nil
  (html [this] [])
  String
  (html [this] [this]))

#?(:clj (def read r/read))
#?(:clj (def read-string r/read-string))

#?(:clj (def write w/write))
#?(:clj (def write-string w/write-string))
