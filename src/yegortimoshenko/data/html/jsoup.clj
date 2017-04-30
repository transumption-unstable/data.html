(ns yegortimoshenko.data.html.jsoup
  (:require [yegortimoshenko.data.html :as html])
  (:import (java.io Writer)
           (org.jsoup.nodes Document Document$OutputSettings Document$OutputSettings$Syntax
                            Attributes Comment Element DataNode TextNode)
           (org.jsoup.parser Tag)))

(def ^:private base-uri (str))
(def ^:private data-tags #{"script" "style"})

(defn ^:private jsoup-element ^Element [{:keys [tag attrs content]}]
  (let [attrs2 (Attributes.)]
    (doseq [[k v] attrs]
      (.put attrs2 (name k) (str v)))
    (let [elt (Element. (Tag/valueOf (name tag)) base-uri attrs2)]
      (doseq [node content]
        (.appendChild elt
         (condp instance? node
           yegortimoshenko.data.html.Comment (Comment. (:content node) base-uri)
           yegortimoshenko.data.html.Element (jsoup-element node)
           yegortimoshenko.data.html.Raw (DataNode. (:content node) base-uri)
           (if (data-tags tag)
             (DataNode. node base-uri)
             (TextNode. node base-uri))))) elt)))

(defn ^:private jsoup-document ^Document [syntax elt]
  (doto (Document. base-uri)
    (.outputSettings
     (doto (Document$OutputSettings.)
       (.syntax syntax)
       (.prettyPrint false)))
    (.appendChild (jsoup-element elt))))

(defmethod html/emit ::html [_ elt ^Writer out]
  (.write out (str (jsoup-document Document$OutputSettings$Syntax/html elt))))

(defmethod html/emit ::xml [_ elt ^Writer out]
  (.write out (str (jsoup-document Document$OutputSettings$Syntax/xml elt))))
