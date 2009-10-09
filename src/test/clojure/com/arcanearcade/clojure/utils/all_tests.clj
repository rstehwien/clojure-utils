(ns com.arcanearcade.clojure.utils.all-tests
  (:use clojure.contrib.test-is)
  (:require com.arcanearcade.clojure.utils.test-file-utils)
  ) 

(run-tests
 'com.arcanearcade.clojure.utils.test-file-utils
 )