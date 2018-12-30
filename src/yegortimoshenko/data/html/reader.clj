(ns yegortimoshenko.data.html.reader
  "Lazy HTML reader built on top of Jericho HTML Parser."
  (:refer-clojure :exclude [read read-string])
  (:require [yegortimoshenko.data.html.node :as node]
            [yegortimoshenko.data.html.spec :as spec])
  (:import (java.io Reader StringReader)
           (java.util Iterator)
           (net.htmlparser.jericho StreamedSource Attribute Segment
                                   StartTag EndTag)))

(set! *warn-on-reflection* true)

(defprotocol Tree
  (tree [head tail]))

(defn lazy-tree [[head & tail]]
  (lazy-seq (tree head tail)))

(defn lazy-leaf [head tail]
  (cons head (lazy-tree tail)))

(defn lazy-branch [parent events]
  (let [[children [_ & siblings]]
        (split-with some? (lazy-tree events))]
    (cons (parent children) siblings)))

(extend-protocol Tree
  StartTag
  (tree [head tail]
    (case (-> head .getStartTagType .getDescription)
      "normal"
      (let [tag (keyword (.getName head))
            attrs (into {} (for [^Attribute a (.getAttributes head)]
                             [(keyword (.getKey a)) (.getValue a)]))]
        (if (spec/void-elements tag)
          (lazy-leaf (node/->Element tag attrs ()) tail)
          (lazy-branch #(node/->Element tag attrs %) tail)))
      "comment"
      (lazy-leaf (node/->Comment (str (.getTagContent head))) tail)))
  EndTag
  (tree [_ tail]
    (lazy-leaf nil tail))
  nil
  (tree [_ _])
  Segment
  (tree [head tail]
    (lazy-leaf (str head) tail)))

(defn lazy-iterator [^Iterator iter]
  (lazy-seq
    (when (.hasNext iter)
      (cons (.next iter) (lazy-iterator iter)))))

(defn read
  "Reads an HTML document from Reader and returns a seq of clojure.xml
  compatible lazy element trees."
  [^Reader in]
  (-> in StreamedSource. .iterator lazy-iterator lazy-tree))

(defn read-string
  "See yegortimoshenko.data.html/read"
  [s]
  (read (StringReader. s)))
