(ns yegortimoshenko.unstable.data.html.reader
  "Lazy HTML reader built on top of Jericho HTML Parser."
  (:refer-clojure :exclude [read read-string])
  (:require [yegortimoshenko.unstable.data.html.node :as node]
            [yegortimoshenko.unstable.data.html.spec :as spec])
  (:import (java.io Reader StringReader)
           (java.util Iterator)
           (net.htmlparser.jericho StreamedSource Attribute Segment
                                   StartTag EndTag)))

(set! *warn-on-reflection* true)

(defn tag->Element [^StartTag tag]
  (node/->Element
    (keyword (.getName tag))
    (into {} (for [^Attribute attr (.getAttributes tag)]
               [(keyword (.getKey attr)) (.getValue attr)]))
    ()))

(defn tag->Comment [^StartTag tag]
  (node/->Comment (.toString (.getTagContent tag))))

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
      (let [elt (tag->Element head)]
        (if (spec/void-element? elt)
          (lazy-leaf elt tail)
          (lazy-branch #(assoc elt :content %) tail)))
      "comment"
      (lazy-leaf (tag->Comment head) tail)))
  EndTag
  (tree [_ tail]
    (lazy-leaf nil tail))
  nil
  (tree [_ _])
  Segment
  (tree [head tail]
    (lazy-leaf (str head) tail)))

(defn lazy-iterator-seq [^Iterator iter]
  (lazy-seq
    (when (.hasNext iter)
      (cons (.next iter) (lazy-iterator-seq iter)))))

(defn lazy-iterator [^Iterable i]
  (lazy-iterator-seq (.iterator i)))

(defn read
  "Reads an HTML document from Reader and returns a seq of clojure.xml
  compatible lazy element trees."
  [^Reader in]
  (-> in StreamedSource. lazy-iterator lazy-tree))

(defn read-string [s]
  (read (StringReader. s)))
