(ns yegortimoshenko.data.html.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [yegortimoshenko.data.html.node :as node]))

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
 (s/with-gen
   (s/and node/Comment? valid-comment?)
   #(gen/fmap node/->Comment (gen/string))))

(def void-elements
  "https://html.spec.whatwg.org/#elements-2"
  #{:area :base :br :col :embed :hr :img :input
    :link :meta :param :source :track :wbr})
