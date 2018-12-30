(ns yegortimoshenko.unstable.data.html.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [yegortimoshenko.unstable.data.html.node :as node]))

(s/def ::tag keyword?)
(s/def ::attrs (s/and (s/map-of keyword? string?)
                      (complement node/Element?)
                      (complement node/Comment?)))
(s/def ::content (s/coll-of ::node))

(def void-elements
  "https://html.spec.whatwg.org/#elements-2"
  #{:area :base :br :col :embed :hr :img :input
    :link :meta :param :source :track :wbr})

(defn ^:private void-element? [{:keys [tag]}]
  (void-elements tag))

(defn ^:private empty-element? [{:keys [content]}]
  (empty? content))

(s/def ::element
  (s/and node/Element?
         (s/keys :req-un [::tag ::attrs ::content])
         (s/or :normal (complement void-element?)
               :void (s/and void-element? empty-element?))))

(s/def ::text
  (s/and #(not (str/starts-with? % ">"))
         #(not (str/starts-with? % "->"))
         #(not (str/includes? % "<!--"))
         #(not (str/includes? % "-->"))
         #(not (str/includes? % "--!>"))
         #(not (str/ends-with? % "<!-"))))

(s/def ::comment
  (s/and node/Comment?
         (s/keys :req-un [::text])))

(s/def ::node
  (s/or ::element
        ::comment
        ::string string?))
