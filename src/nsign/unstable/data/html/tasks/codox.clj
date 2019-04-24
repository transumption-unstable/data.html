(ns nsign.unstable.data.html.tasks.codox
  (:require [codox.main :refer [generate-docs]]))

(defn -main []
  (generate-docs {:output-path "public"}))
