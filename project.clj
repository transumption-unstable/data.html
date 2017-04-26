(defproject com.yegortimoshenko/data.html "20170421.124711"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.jsoup/jsoup "1.10.2"]]
  :deploy-repositories {"sonatype" {:creds :gpg :url "https://oss.sonatype.org/service/local/staging/deploy/maven2"}}
  :description "Zipper-compatible HTML reader/writer based on jsoup"
  :license {:name "Internet Systems Consortium License"
            :url "https://github.com/yegortimoshenko/data.html/blob/master/LICENSE"}
  :plugins [[lein-stamp "20170312.223701"]]
  :pom-addition [:developers
                 [:developer
                  [:name "Yegor Timoshenko"]
                  [:email "yegortimoshenko@gmail.com"]]]
  :repl-options {:init-ns yegortimoshenko.data.html}
  :scm {:url "git@github.com:yegortimoshenko/data.html.git"}
  :url "https://github.com/yegortimoshenko/data.html")
