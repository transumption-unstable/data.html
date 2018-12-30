(ns yegortimoshenko.unstable.data.html.writer
  "Minimizing HTML writer. Produces the most compact HTML representation of
  the given tree while still being conformant to the living standard.

  There are some errors that don't trigger quirks mode, like <!doctypehtml>
  doctype. Leveraging these would help shave off a few more bytes, but results
  in technically invalid HTML. This implementation ignores such optimization
  opportunities.

  TODO: skip optional tags.

  References:
  - html-minifier <https://git.io/vf3AC>
  - Html5Printer <https://git.io/fhkNT>"
  (:require [clojure.string :as str]
            [yegortimoshenko.unstable.data.html.node]
            [yegortimoshenko.unstable.data.html.spec :as spec])
  (:import (clojure.lang Keyword Sequential)
           (java.io Writer StringWriter)
           (java.util Set)
           (yegortimoshenko.unstable.data.html.node Comment Element)))

(set! *warn-on-reflection* true)

(defprotocol AttrValue
  (attr-value ^String [this]))

(extend-protocol AttrValue
  Keyword
  (attr-value [this] (name this))
  nil
  (attr-value [_] "")
  Set
  (attr-value [this]
    (str/join " " (map attr-value (sort this))))
  String
  (attr-value [this] this))

(defn ^:private must-be-quoted?
  "https://mathiasbynens.be/notes/unquoted-attribute-values"
  [s]
  (or (empty? s)
      (re-find #"[\s\"'`=<>]" s)))

(def ^:private quote->escape-sequence
  {\" "&#34;"
   \' "&#39;"})

(def ^:private quote->regex
  {\" #"\""
   \' #"\'"})

(defn ^:private optimal-quote ^String [s]
  (let [freq (frequencies s)]
    (if (> (get freq \" 0)
           (get freq \' 0))
      \'
      \")))

(defn ^:private write-attr-value
  [x ^Writer w]
  (let [s (attr-value x)]
    (if (must-be-quoted? s)
      (let [q (optimal-quote s)]
        (.write w (int q))
        (.write w (str/replace s
                    (quote->regex q)
                    (quote->escape-sequence q)))
        (.write w (int q)))
      (.write w s))))

(def ^:private comment?
  (partial instance? Comment))

(def children-that-make-body-open-mandatory
  #{:meta :link :script :style :template})

(def siblings-that-make-p-close-optional
  #{:address :article :aside :blockquote :div :dl :fieldset :footer :form
    :h1 :h2 :h3 :h4 :h5 :h6 :header :hgroup :hr :main :nav :ol :p :pre
    :section :table :ul})

(defn escape-text ^String [s]
  (str/replace s #"<" "&lt;"))

(defprotocol Node
  (write-node [this next ^Writer w]))

(extend-protocol Node
  Comment
  (write-node [{:keys [text]} _ ^Writer w]
    (doseq [^String chunk ["<!--" text "-->"]]
      (.write w chunk)))
  Element
  (write-node [{:keys [attrs content tag] :as elt} next ^Writer w]
    (let [tag-name (name tag)]
      (doseq [chunk ["<" tag-name]]
        (.write w ^String chunk))
      (doseq [[k v] (sort attrs)]
        (.write w " ")
        (.write w (name k))
        (.write w "=")
        (write-attr-value v w))
      (.write w ">")
      (write-node content nil w)
      (when-not (spec/void-element? elt)
        (doseq [chunk ["</" tag-name ">"]]
          (.write w ^String chunk)))))
  Sequential
  (write-node [those _ w]
    (loop [[this & rest] those]
      (when this
        (write-node this (first rest) w)
        (recur rest))))
  String
  (write-node [this _ ^Writer w]
    (.write w (escape-text this))))

(def doctype "<!doctype html>")

(defn write [tree ^Writer w]
  (.write w ^String doctype)
  (write-node tree nil w))

(defn write-string [tree]
  (with-open [out (StringWriter.)]
    (write tree out)
    (str out)))
