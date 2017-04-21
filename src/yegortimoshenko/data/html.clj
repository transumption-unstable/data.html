(ns yegortimoshenko.data.html
  (:refer-clojure :exclude [comment read read-string])
  (:import (java.io InputStream)
           (org.jsoup Jsoup)
           (org.jsoup.nodes Attribute Attributes DataNode Document Document$OutputSettings TextNode)
           (org.jsoup.parser Tag)))

(set! *warn-on-reflection* true)

(defrecord Comment [content])
(defrecord Element [tag attrs content])

(defn comment [content]
  (Comment. content))

(defn element
  ([tag] (Element. tag {} ()))
  ([tag attrs] (Element. tag attrs ()))
  ([tag attrs & content] (Element. tag attrs content)))

(defn ^:private parse-element [^org.jsoup.nodes.Element elt]
  (Element.
   (keyword (.tagName elt))
   (into {} (map (fn [^Attribute attr] [(.getKey attr) (.getValue attr)]) (.attributes elt)))
   (for [node (.childNodes elt)]
     (condp instance? node
       org.jsoup.nodes.Comment (Comment. (.getData ^org.jsoup.nodes.Comment node))
       org.jsoup.nodes.Element (parse-element node)
       DataNode (.getWholeData ^DataNode node)
       TextNode (.text ^TextNode node)))))

(defn ^:private parse-document [^Document doc]
  (parse-element (.child doc 0)))

(def ^:dynamic ^String *encoding* "UTF-8")

(defn read
  ([^InputStream in] (parse-document (Jsoup/parse in *encoding* nil)))
  ([{:keys [encoding url] :or {encoding *encoding*}} in]
   (parse-document (Jsoup/parse ^InputStream in ^String encoding ^String url))))

(defn read-string [^String s]
  (parse-document (Jsoup/parse s)))

(defn ->fragment [sexp]
  (if (string? sexp)
    sexp
    (let [[tag & content] sexp]
      (if (= tag :-comment)
        (Comment. (first content))
        (let [?attrs (first content)
              [attrs content] (if (map? ?attrs) [?attrs (rest content)] [{} content])]
          (Element. tag attrs (map ->fragment content)))))))

(def ^:private base-uri (str))
(def ^:private data-elements #{"script" "style"})

(defn ^:private emit-element ^org.jsoup.nodes.Element [{:keys [tag attrs content]}]
  (let [attrs2 (Attributes.)]
    (doseq [[k v] attrs]
      (.put attrs2 (name k) (str v)))
    (let [elt (org.jsoup.nodes.Element. (Tag/valueOf (name tag)) base-uri attrs2)]
      (doseq [node content]
        (.appendChild elt
         (condp instance? node
           Comment (org.jsoup.nodes.Comment. (:content node) base-uri)
           Element (emit-element node)
           String (if (data-elements tag)
                    (DataNode. node base-uri)
                    (TextNode. node base-uri))))) elt)))

(defn ^:private emit-document [{:keys [encoding] :or {encoding *encoding*}} elt]
  (doto (Document. base-uri)
    (.outputSettings (doto (Document$OutputSettings.)
                       (.charset *encoding*)
                       (.prettyPrint false)))
    (.appendChild (emit-element elt))))

(defn write-string
  ([elt] (write-string {} elt))
  ([{:keys [doctype] :or {doctype "<!DOCTYPE html>"} :as params} elt]
   (str doctype (.toString (emit-document params elt)))))
