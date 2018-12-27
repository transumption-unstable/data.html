(ns yegortimoshenko.data.html.writer
  (:require [yegortimoshenko.data.html.types :as t])
  (:import (java.io Writer StringWriter)
           (org.jsoup.nodes Attributes Comment Element DataNode TextNode
                            Document Document$OutputSettings
                            Document$OutputSettings$Syntax)
           (org.jsoup.parser Tag)))

(def doctype "<!doctype html>")

(def ^:private base-uri (str))
(def ^:private data-tags #{:script :style})

(defn ^:private jsoup-element ^Element [{:keys [tag attrs content]}]
  (let [attrs2 (Attributes.)]
    (doseq [[k v] attrs]
      (.put attrs2 (name k) (str v)))
    (let [elt (Element. (Tag/valueOf (name tag)) base-uri attrs2)]
      (doseq [node content]
        (.appendChild elt
         (condp instance? node
           yegortimoshenko.data.html.types.Comment (Comment. (:content node) base-uri)
           yegortimoshenko.data.html.types.Element (jsoup-element node)
           (if (data-tags tag)
             (DataNode. node base-uri)
             (TextNode. node base-uri))))) elt)))

(defn ^:private jsoup-document ^Document [elt]
  (doto (Document. base-uri)
    (.outputSettings
     (doto (Document$OutputSettings.)
       (.syntax Document$OutputSettings$Syntax/html)
       (.prettyPrint false)))
    (.appendChild (jsoup-element elt))))

(defn write [elt ^Writer out]
  (.write out doctype)
  (.write out (-> elt first jsoup-document str)))

(defn write-string
  "See yegortimoshenko.data.html/write"
  [elt]
  (with-open [out (StringWriter.)]
    (write elt out)
    (str out)))
