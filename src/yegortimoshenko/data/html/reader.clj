(ns yegortimoshenko.data.html.reader
  "Lazy HTML reader built on top of Jericho HTML Parser."
  (:import (java.io Reader StringReader)
           (java.util Iterator)
           (net.htmlparser.jericho Attribute StreamedSource StartTag
                                   StartTagType EndTag EndTagType
                                   StartTagTypeGenericImplementation))
  (:refer-clojure :exclude [read read-string])
  (:require [yegortimoshenko.data.html.types :as t]))

(set! *warn-on-reflection* true)

(defn ^:private lazy-tree [[leaf & leaves]]
  (lazy-seq
    (cond
      (fn? leaf) [(leaf (lazy-tree leaves))]
      (= :up leaf) (lazy-tree leaves)
      (some? leaf) (cons leaf (lazy-tree leaves)))))

(defn ^:private read-element [^StartTag segment]
  (let [tag (keyword (.getName segment))
        attrs (into {} (for [^Attribute a (.getAttributes segment)]
                         [(keyword (.getKey a)) (.getValue a)]))]
    (partial t/->Element tag attrs)))

(defn ^:private read-comment [^StartTag segment]
  (-> segment .getTagContent str t/->Comment))

(def readers
  {StartTagType/NORMAL read-element
   StartTagType/COMMENT read-comment})

(defprotocol Segment
  (read-segment [x]))

(extend-protocol Segment
  StartTag
  (read-segment [x]
    (if-let [reader (readers (.getStartTagType x))]
      (reader x)))
  EndTag
  (read-segment [_] :up)
  java.lang.Object
  (read-segment [x] (str x)))

(defn lazy-iterator [^Iterator iter]
  (lazy-seq
    (when (.hasNext iter)
      (cons (.next iter) (lazy-iterator iter)))))

(defn ^:private event-seq [^Reader in]
  (map read-segment (-> in StreamedSource. .iterator lazy-iterator)))

(defn read
  "Reads an HTML document from Reader and returns a seq of clojure.xml
  compatible lazy element trees."
  [^Reader in]
  (-> in event-seq lazy-tree))

(defn read-string
  "See yegortimoshenko.data.html/read"
  [s]
  (read (StringReader. s)))
