(ns com.arcanearcade.clojure.utils.test-file-utils
  (:require [com.arcanearcade.clojure.utils.file-utils :as file-utils])
  (:use clojure.test))

(deftest test-empty-files-ending-with
  (is (= 0 (count (file-utils/filter-files (file-utils/ls "should_not_exist1") #".*zip")))))

(deftest test-keys-file-seq-cache
  (file-utils/clear-file-seq-cache)
  (is (= 0 (count (keys @file-utils/file-seq-cache))))
  (file-utils/get-file-seq-cache "should_not_exist1")
  (is (some #{"should_not_exist1"} (keys @file-utils/file-seq-cache)))
  )
