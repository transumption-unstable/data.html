(ns yegortimoshenko.data.html.reader
  "Lazy HTML reader built on top of Jericho HTML Parser."
  (:refer-clojure :exclude [read read-string])
  (:require [yegortimoshenko.data.html.node :as node])
  (:import (java.io Reader StringReader)
           (java.util Iterator)
           (net.htmlparser.jericho StreamedSource Attribute Segment
                                   StartTag StartTagType EndTag)))

(set! *warn-on-reflection* true)

(defprotocol Tree
  (tree [this rest]))

(defn lazy-tree [[this & rest]]
  (lazy-seq (tree this rest)))

(defn lazy-leaf [leaf rest]
  (cons leaf (lazy-tree rest)))

(extend-protocol Tree
  StartTag
  (tree [this rest]
    (condp = (.getStartTagType this)
      StartTagType/NORMAL
      [(node/->Element
        (keyword (.getName this))
        (into {} (for [^Attribute a (.getAttributes this)]
                   [(keyword (.getKey a)) (.getValue a)]))
        (lazy-tree rest))]
      StartTagType/COMMENT
      (lazy-leaf (node/->Comment (str (.getTagContent this))) rest)))
  EndTag
  (tree [this rest]
    (lazy-tree rest))
  nil
  (tree [this rest])
  Segment
  (tree [this rest]
    (lazy-leaf (str this) rest)))

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
