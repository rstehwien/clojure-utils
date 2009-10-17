(ns com.arcanearcade.clojure.utils.test-timer-agent
  (:require [com.arcanearcade.clojure.utils.timer-agent :as ta])
  (:use clojure.test))

(deftest test-timer-agent-add-five
  (let [timer (ta/create-timer-agent #(+ % 5) :data 8 :run-once? true :timer-ms 500)]
    (is (= 8 (:data @timer)))
    (ta/start-timer-agent timer)
    (await-for 1000 timer)
    (is (= 13 (:data @timer)))))

