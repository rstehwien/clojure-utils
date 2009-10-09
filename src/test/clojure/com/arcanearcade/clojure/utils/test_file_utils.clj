(ns com.arcanearcade.clojure.utils.test-file-utils
  (:require [com.arcanearcade.clojure.utils.file-utils :as file-utils])
  (:use clojure.contrib.test-is))

(deftest test-empty-files-ending-with
  (is (= (count (file-utils/files-ending-with "should_not_exist1" ".zip")) 0)))

(deftest test-keys-file-seq-cache
  (file-utils/clear-cached-files)
  (is (= (count (keys @file-utils/file-seq-cache)) 0))
  (file-utils/get-cached-files "should_not_exist1")
  (is (some #{"should_not_exist1"} (keys @file-utils/file-seq-cache)))
  )

(run-tests)
