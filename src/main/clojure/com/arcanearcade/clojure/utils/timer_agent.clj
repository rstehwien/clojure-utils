;; Copyright (c) Robert Stehwien, Oct 2009. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns
    #^{:author "Robert Stehwien",
       :doc "An agent that calls a :function at an :interval-ms with optional :data"}
  com.arcanearcade.clojure.utils.timer-agent
  (:use [clojure.contrib.def :as ccd])
  )

(def *default-timer-ms* 1000)

(defstruct timer-agent-struct :function :timer-ms :data :run-once? :running?)

(defn- do-stop-timer-agent [ta]
  (assoc ta :running? false))

(defn- do-configure-timer-agent [ta args]
  (reduce conj ta args))

(defn- do-timer-agent [ta]
  (if (and (:running? ta) (:function ta))
    (do
      (Thread/sleep (:timer-ms ta))
      (if (not (:run-once? ta))
        (send-off *agent* do-timer-agent)
        (send *agent* do-stop-timer-agent))
      (assoc ta :data ((:function ta) (:data ta))))
    ta))

(defn- do-start-timer-agent [ta]
  (send-off *agent* do-timer-agent)
  (assoc ta :running? true))

(ccd/defnk create-timer-agent
  [f
   :timer-ms *default-timer-ms*
   :data nil
   :run-once? false]
  (agent (struct timer-agent-struct f timer-ms data run-once? false)))

(defn start-timer-agent [ta] (send ta do-start-timer-agent))

(defn stop-timer-agent [ta] (send ta do-stop-timer-agent))

(ccd/defnk configure-timer-agent!
  [ta
   :function (:function @ta)
   :timer-ms (:timer-ms @ta)
   :data (:data @ta)
   :run-once? (:run-once? @ta)]
  (send ta do-configure-timer-agent
        {:function function :timer-ms timer-ms :data data :run-once? run-once?}))

