(defproject com.yegortimoshenko/data.html "20170427.001236"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [net.htmlparser.jericho/jericho-html "3.4"]
                 [org.jsoup/jsoup "1.10.2"]]
  :deploy-repositories {"sonatype" {:creds :gpg :url "https://oss.sonatype.org/service/local/staging/deploy/maven2"}}
  :description "Lazy zipper-compatible HTML reader/writer based on jsoup and amazing Jericho HTML Parser"
  :license {:name "ISC License" :url "https://opensource.org/licenses/ISC"}
  :plugins [[lein-stamp "20170312.223701"]]
  :pom-addition [:developers
                 [:developer
                  [:id "yegortimoshenko"]
                  [:name "Yegor Timoshenko"]
                  [:email "yegortimoshenko@gmail.com"]]]
  :repl-options {:init-ns yegortimoshenko.data.html}
  :scm {:url "git@github.com:yegortimoshenko/data.html.git"}
  :url "https://github.com/yegortimoshenko/data.html")
