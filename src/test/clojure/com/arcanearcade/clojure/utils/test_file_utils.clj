(ns com.arcanearcade.clojure.utils.test-file-utils
  (:require [com.arcanearcade.clojure.utils.file-utils :as file-utils])
  (:use clojure.contrib.test-is))

(deftest test-empty-files-ending-with
  (is (= (count (file-utils/filter-files-ends-with (file-utils/ls "should_not_exist1") ".zip")) 0)))

(deftest test-keys-file-seq-cache
  (file-utils/clear-file-seq-cache)
  (is (= (count (keys @file-utils/file-seq-cache)) 0))
  (file-utils/get-file-seq-cache "should_not_exist1")
  (is (some #{"should_not_exist1"} (keys @file-utils/file-seq-cache)))
  )
