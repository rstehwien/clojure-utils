(ns com.arcanearcade.clojure.utils.all-tests
  (:use clojure.contrib.test-is)
  (:require com.arcanearcade.clojure.utils.test-file-utils)
  (:require com.arcanearcade.clojure.utils.test-regex-utils)
  (:require com.arcanearcade.clojure.utils.test-timer-agent)
  ) 

(run-tests
 'com.arcanearcade.clojure.utils.test-file-utils
 'com.arcanearcade.clojure.utils.test-regex-utils
 ;; make sure test-timer-agent runs last as it calls (shutdown-agents)
 'com.arcanearcade.clojure.utils.test-timer-agent
 )