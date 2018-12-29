(ns yegortimoshenko.data.html.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [yegortimoshenko.data.html.node :as node]))

(defn ^:private valid-comment-text?
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
   (s/and node/Comment? valid-comment-text?)
   #(gen/fmap node/->Comment (gen/string))))
