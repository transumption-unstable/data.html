(ns yegortimoshenko.data.html
  "Lazy zipper-compatible HTML reader/writer built on top of jsoup and amazing Jericho HTML Parser"
  (:refer-clojure :exclude [comment read read-string])
  (:import (java.io InputStream Reader StringReader)
           (java.util Iterator)
           (net.htmlparser.jericho Attribute Renderer StreamedSource StartTag StartTagType EndTag Segment)))

(set! *warn-on-reflection* true)

(defn ^{:author "Chris Houser"
        :copyright "Rich Hickey"
        :example `(seq-tree #(when (= %1 :<) (vector %2)) #{:>} str
                            [1 2 :< 3 :< 4 :> :> 5 :> 6])
        :license "Eclipse Public License"
        :origin "https://git.io/v93Od"
        :private true}
  seq-tree
  "Takes a seq of events that logically represents
  a tree by each event being one of: enter-sub-tree event,
  exit-sub-tree event, or node event.
  Returns a lazy sequence whose first element is a sequence of
  sub-trees and whose remaining elements are events that are not
  siblings or descendants of the initial event.
  The given exit? function must return true for any exit-sub-tree
  event.  parent must be a function of two arguments: the first is an
  event, the second a sequence of nodes or subtrees that are children
  of the event.  parent must return nil or false if the event is not
  an enter-sub-tree event.  Any other return value will become
  a sub-tree of the output tree and should normally contain in some
  way the children passed as the second arg.  The node function is
  called with a single event arg on every event that is neither parent
  nor exit, and its return value will become a node of the output tree."
  [parent exit? node coll]
  (lazy-seq
   (when-let [[event] (seq coll)]
     (let [more (rest coll)]
       (if (exit? event)
         (cons nil more)
         (let [tree (seq-tree parent exit? node more)]
           (if-let [p (parent event (lazy-seq (first tree)))]
             (let [subtree (seq-tree parent exit? node (lazy-seq (rest tree)))]
               (cons (cons p (lazy-seq (first subtree)))
                     (lazy-seq (rest subtree))))
             (cons (cons (node event) (lazy-seq (first tree)))
                   (lazy-seq (rest tree))))))))))

(defrecord Comment [content])
(defrecord Element [tag attrs content])

(defn ^:private read-comment [^StartTag tag]
  (Comment. (str (.getTagContent tag))))

(defn ^:private read-element [^StartTag tag]
  (partial ->Element
           (keyword (.getName tag))
           (into {} (for [^Attribute a (.getAttributes tag)]
                      [(keyword (.getKey a)) (.getValue a)]))))

(defn ^:private read-event [^Iterator iterator]
  (if (.hasNext iterator)
    (let [segment (.next iterator)]
      (condp instance? segment
        StartTag (condp = (.getStartTagType ^StartTag segment)
                   StartTagType/NORMAL (read-element segment)
                   StartTagType/COMMENT (read-comment segment)
                   ::ignored)
        EndTag ::exit
        (str segment)))))

(defn ^:private event-seq [^Reader in]
  (take-while some? (remove #{::ignored} (repeatedly (partial read-event (.iterator (StreamedSource. in)))))))

(defn ^:private parent [event children]
  (when (fn? event) (event children)))

(defn read [^Reader in]
  (ffirst (seq-tree parent #{::exit} identity (event-seq in))))

(defn read-string [s]
  (read (StringReader. s)))

(defn comment [content]
  (Comment. content))

(defn element
  ([tag] (Element. tag {} ()))
  ([tag attrs] (Element. tag attrs ()))
  ([tag attrs & content] (Element. tag attrs content)))

(defn html [content]
  (if (vector? content)
    (let [[tag ?attrs & children] content]
      (if (= tag :-comment)
        (Comment. ?attrs)
        (let [[attrs children] (if (map? ?attrs) [?attrs children] [{} (cons ?attrs children)])]
          (Element. tag attrs (map html children)))))
    (if (seq? content) (map html content) content)))

(def ^:private base-uri (str))
(def ^:private data-tags #{"script" "style"})

(defn ^:private jsoup-element ^org.jsoup.nodes.Element [{:keys [tag attrs content]}]
  (let [attrs2 (org.jsoup.nodes.Attributes.)]
    (doseq [[k v] attrs]
      (.put attrs2 (name k) (str v)))
    (let [elt (org.jsoup.nodes.Element. (org.jsoup.parser.Tag/valueOf (name tag)) base-uri attrs2)]
      (doseq [node content]
        (.appendChild elt
         (condp instance? node
           Comment (org.jsoup.nodes.Comment. (:content node) base-uri)
           Element (jsoup-element node)
           String (if (data-tags tag)
                    (org.jsoup.nodes.DataNode. node base-uri)
                    (org.jsoup.nodes.TextNode. node base-uri))))) elt)))

(defn ^:private jsoup-document ^org.jsoup.nodes.Document [elt]
  (doto (org.jsoup.nodes.Document. base-uri)
    (.outputSettings (doto (org.jsoup.nodes.Document$OutputSettings.) (.prettyPrint false)))
    (.appendChild (jsoup-element elt))))

(defn write-string [elt]
  (str (jsoup-document elt)))

(def doctype "<!DOCTYPE html>")
