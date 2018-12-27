(ns yegortimoshenko.data.html
  "Lazy zipper-compatible HTML reader/writer built on top of Jericho HTML Parser"
  (:refer-clojure :exclude [comment read read-string])
  (:import (java.io Reader Writer StringReader StringWriter)
           (java.util Iterator)
           (net.htmlparser.jericho Attribute StreamedSource StartTag
                                   StartTagType EndTag EndTagType
                                   StartTagTypeGenericImplementation)))

(set! *warn-on-reflection* true)

(defn ^{:author "Chris Houser"
        :copyright "Rich Hickey"
        :license "Eclipse Public License"
        :origin "https://git.io/v93Od"
        :private true}
  seq-tree
  "Takes a seq of events that logically represents a tree by each event being
  one of: enter-sub-tree event, exit-sub-tree event, or node event.

  Returns a lazy sequence whose first element is a sequence of sub-trees and
  whose remaining elements are events that are not siblings or descendants of
  the initial event.

  The given exit? function must return true for any exit-sub-tree event.

  parent must be a function of two arguments: the first is an event, the second
  a sequence of nodes or subtrees that are children of the event. parent must
  return nil or false if the event is not an enter-sub-tree event. Any other
  return value will become a sub-tree of the output tree and should normally
  contain in some way the children passed as the second arg.

  The node function is called with a single event arg on every event that is
  neither parent nor exit, and its return value will become a node of the
  output tree."
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

(defrecord Element [tag attrs content])

(defn ^:private void? [^StartTag tag]
  (or (.isEndTagForbidden tag) (= "track" (.getName tag))))

(defn ^:private read-element [^StartTag tag]
  (let [f (partial ->Element
                   (keyword (.getName tag))
                   (into {} (for [^Attribute a (.getAttributes tag)]
                              [(keyword (.getKey a)) (.getValue a)])))]
    (if (void? tag) (f nil) ^{:tag (keyword (.getName tag))} f)))

(defrecord Comment [content])

(defn ^:private read-comment [^StartTag tag]
  (Comment. (str (.getTagContent tag))))

(defrecord Raw [content])

(defn ^:private read-raw [^StartTag tag]
  (Raw. (str tag)))

(defn ^:private read-event [^Iterator iterator]
  (if (.hasNext iterator)
    (let [segment (.next iterator)]
      (condp instance? segment
        StartTag (condp = (.getStartTagType ^StartTag segment)
                   StartTagType/NORMAL (read-element segment)
                   StartTagType/COMMENT (read-comment segment)
                   StartTagType/DOCTYPE_DECLARATION ::doctype
                   (read-raw segment))
        EndTag ::exit
        (str segment)))))

(defn ^:private event-seq [^Reader in]
  (->> (StreamedSource. in)
       (.iterator)
       (partial read-event)
       (repeatedly)
       (remove (partial = ::doctype))
       (take-while some?)))

(defn ^:private parent [event children]
  (when (fn? event) (event children)))

(defn ^:private infer
  "TODO: lazily infers optional tags, see: http://w3.org/TR/html51/syntax.html#optional-tags"
  [seq]
  (let [f (fn [[elt & rest :as seq] seen]
            (lazy-seq
             (if (= :html (:tag (meta elt)))
               seq
               (cons (partial ->Element :html {}) seq))))]
    (f seq #{})))

(defn ^:private infer [seq] seq)

(defn ^:private register []
  (.register (proxy [StartTagTypeGenericImplementation] ["" "<" ">" EndTagType/UNREGISTERED false]))
  (.register StartTagType/NORMAL))

(def ^:private register-memo (memoize register))

(defn read
  "Reads an HTML document from Reader and returns a clojure.xml compatible lazy element tree"
  [^Reader in]
  (register-memo)
  (->> (event-seq in)
       (drop-while (complement fn?))
       (infer)
       (seq-tree parent (partial = ::exit) identity)
       (ffirst)))

(defn read-string
  "See yegortimoshenko.data.html/read"
  [s]
  (read (StringReader. s)))

(defn comment [content]
  (Comment. content))

(defn element
  ([tag] (Element. tag {} ()))
  ([tag attrs] (Element. tag attrs ()))
  ([tag attrs & content] (Element. tag attrs content)))

(defn raw [content]
  (Raw. content))

(defn html
  ([content]
   (if (vector? content)
     (let [[tag ?attrs & children] content]
       (case tag
         :-comment (Comment. ?attrs)
         :-raw (Raw. ?attrs)
         (let [[attrs children] (if (map? ?attrs) [?attrs children] [{} (cons ?attrs children)])]
           (Element. tag attrs (remove nil? (map html children))))))
     (if (seq? content) (map html content) content)))
  ([content & more]
   (html (cons content more))))

(defmacro ^:private codepoint [c] (int c))

(defn ^:private emit-doctype [^Writer out]
  (.write out "<!doctype html>"))

(defn emit [elt ^Writer out]
  (.write out "TODO"))

(defn write [elt ^Writer out]
  (emit-doctype out)
  (emit elt out))

(defn write-string
  "See yegortimoshenko.data.html/write"
  [elt]
  (with-open [out (StringWriter.)]
    (write elt out)
    (str out)))
