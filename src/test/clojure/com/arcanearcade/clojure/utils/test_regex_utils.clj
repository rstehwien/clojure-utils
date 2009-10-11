(ns com.arcanearcade.clojure.utils.test-regex-utils
  (:require [com.arcanearcade.clojure.utils.regex-utils :as regex-utils])
  (:use clojure.test))

(deftest test-re-match?-pass
  (is (regex-utils/re-match? #".*foo.*" "a fooa")))

(deftest test-re-match?-fail
  (is (not (regex-utils/re-match? #".*foo.*" "something without f o o"))))

(deftest test-re-filter-pass
  (is (= 2 (count (regex-utils/re-filter #".*bar.*" ["bar" "abar" "smeggle"])))))

(deftest test-re-filter-pass-one
  (is (= ["bar"] (regex-utils/re-filter #"bar" ["bar" "abar" "smeggle"]))))

(deftest test-re-filter-fail
  (is (= 0 (count (regex-utils/re-filter #"br" ["bar" "abar" "smeggle"])))))
