;; Copyright (c) Robert Stehwien, Oct 2009. All rights reserved.
;; Originally written by Rasmus Svensson in:
;; http://github.com/raek
;; http://gist.github.com/208575
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.


(ns
  #^{:author "Rasmus Svensson, Robert Stehwien",
     :doc "Event library"}
    com.arcanearcade.clojure.utils.events
    (:use [clojure.contrib.core :only [dissoc-in]]))
 
(defn add-event-handler [state event-type key-or-agent f]
  (assoc-in state [::handlers event-type key-or-agent] f))
 
(defn remove-event-handler [state event-type key-or-agent]
  (dissoc-in state [::handlers event-type key-or-agent]))
 
(defn dispatch-event [state event-type & args]
  (doseq [[key-or-agent f] (-> state ::handlers event-type)]
      (if (keyword? key-or-agent)
	(apply f args)
	(send-off key-or-agent f args)))
  state)
 
;; (def a (agent {}))
;; (send a add-event-handler :on-foo :x #(println "foo occured:" %))
;; (send a dispatch-event :on-foo "bar")
;; (send a remove-event-handler  :on-foo :x)
;; (send a dispatch-event :on-foo "baz")
