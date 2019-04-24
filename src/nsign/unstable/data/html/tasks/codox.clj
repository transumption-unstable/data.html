(ns nsign.unstable.data.html.tasks.codox
  (:require [codox.main :refer [generate-docs]]))

(defn -main []
  (generate-docs
    {:name "unstable.data.html"
     :version "" ; https://github.com/weavejester/codox/pull/183
     :source-uri "https://gitlab.com/nsign/unstable.data.html/blob/{git-commit}/{filepath}#L{line}"
     :output-path "public"}))
