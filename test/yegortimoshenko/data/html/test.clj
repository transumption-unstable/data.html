(ns yegortimoshenko.data.html.test
  (:require [clojure.test :refer [deftest is testing]]
            [yegortimoshenko.data.html :as html :refer [html]]))

(deftest html5-writer
  (testing "omit attribute quotes where possible"
    (is (= (html/write-string (html [:div {:class "a"}])) "<div class=a></div>")))
  (testing "omit end tags where possible"
    (is (= (html/write-string (html [:div [:p "Sic."]])) "<div><p>Sic.</div>")))
  (testing "omit html/head/body when possible"
    (is (= (html/write-string (html [:html [:head [:title "Foo"]] [:body [:div "Sic."]]]))
           "<!DOCTYPE html><title>Foo</title><div>Sic.</div>"))
    (is (= (html/write-string (html [:html [:-comment "Hi?"] [:head [:title "Foo"]] [:body [:div "Sic."]]]))
           "<!DOCTYPE html><html><!-- Hi? --><title>Foo</title><div>Sic.</div></html>"))
    (is (= (html/write-string (html [:html {:lang "en"} [:head [:title "Foo"]] [:body [:div "Sic."]]]))
           "<!DOCTYPE html><html lang=en><title>Foo</title><div>Sic.</div></html>"))))
