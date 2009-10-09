(ns com.arcanearcade.clojure.utils.test-file-utils
  (:require [com.arcanearcade.clojure.file-utils :as file-utils])
  (:use clojure.test))

(deftest test-failure
  (is (= 20 21)))

(run-tests)

(blarg