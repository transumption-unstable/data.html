(ns yegortimoshenko.data.html.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [yegortimoshenko.data.html.node :as node]))

(def void-elements
  "https://html.spec.whatwg.org/#elements-2"
  #{:area :base :br :col :embed :hr :img :input
    :link :meta :param :source :track :wbr})

(defn void-element? [{:keys [tag]}]
  (void-elements tag))

(defn empty-element? [{:keys [content]}]
  (empty? content))

(s/def ::tag keyword?)
(s/def ::attrs (s/map-of keyword? string?))
(s/def ::content (s/coll-of ::node))

(s/def ::element
  (s/and node/Element?
         (s/keys :req-un [::tag ::attrs ::content])
         (s/or :normal (complement void-element?)
               :void (s/and void-element? empty-element?))))

(defn valid-comment?
  "https://html.spec.whatwg.org/#comments"
  [{:keys [text]}]
  (not (or (str/starts-with? text ">")
           (str/starts-with? text "->")
           (str/includes? text "<!--")
           (str/includes? text "-->")
           (str/includes? text "--!>")
           (str/ends-with? text "<!-"))))

(s/def ::comment
  (s/and node/Comment? valid-comment?))

(s/def ::node
  (s/or ::element
        ::comment
        ::string string?))
