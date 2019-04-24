(ns nsign.unstable.data.html
  (:refer-clojure :exclude [comment read read-string])
  (:require [clojure.spec.alpha :refer [valid?]]
            [nsign.unstable.data.html.node :as node]
            [nsign.unstable.data.html.spec :as spec]
            #?(:clj [nsign.unstable.data.html.reader :as r])
            #?(:clj [nsign.unstable.data.html.writer :as w]))
  (:import (clojure.lang IPersistentVector ISeq)))

(defn comment [text]
  {:pre [(valid? ::spec/text text)]}
  (node/->Comment text))

(defprotocol HTML
  (html [this]))

(extend-protocol HTML
  IPersistentVector
  (html [[tag ?attrs & content]]
    [(if (valid? ::spec/attrs ?attrs)
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
