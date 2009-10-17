;; Copyright (c) Robert Stehwien, Oct 2009. All rights reserved.
;; Originally written by John Harrop in:
;; http://groups.google.com/group/clojure/msg/1ead77e1e15b3aaa
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns
    #^{:author "John Harrop, Robert Stehwien",
       :doc "Clojure actor that can be controlled from outside"}
  com.arcanearcade.clojure.utils.actor
  )

(defn make-actor [f period-in-ms & initial-state]
  (agent (into [f period-in-ms false] initial-state)))

(defmacro actor
  "Creates and returns a new, initially-sleeping actor with the specified period, initial parameter values, and code to execute."
  [period-in-ms initial-bindings & body]
  `(let [p# ~period-in-ms]
     (make-actor
      (fn [~@(take-nth 2 initial-bindings)] ~@body)
      p#
      ~@(take-nth 2 (rest initial-bindings)))))

(defn- actor-act [state]
  (when (nth state 2)
    (apply (first state) (drop 3 state))
    (Thread/sleep (second state))
    (send-off *agent* actor-act))
  state)

(defn- actor-start [state]
  (send-off *agent* actor-act)
  (into [(first state) (second state) true] (drop 3 state)))

(defn- actor-stop [state]
  (into [(first state) (second state) false] (drop 3 state)))

(defn- actor-change-state [state new-state]
  (into [(first state) (second state) (nth state 2)] new-state))

(defn start-actor
  "Wakes up an actor -- starts it periodically executing its body."
  [actor]
  (send-off actor actor-start))

(defn stop-actor
  "Puts an actor to sleep again."
  [actor]
  (send-off actor actor-stop))

(defn change-actor-state
  "Changes an actor's parameter list."
  [actor & new-state]
  (send-off actor actor-change-state new-state))

;; Test at the REPL with these:
;; => (def y (actor 1000 [x 1 y 1] (println (+ (* x 10) y))))
;; => (start-actor y)
;; 11 should start repeating to stdout.
;; => (change-actor-state y 2 2)
;; It should stop repeating 11 and start repeating 22.
;; => (stop-actor y)
;; Output should halt.
;; => (start-actor y)
;; Output should resume.
;; => (stop-actor y)
;; Output should halt.
;; => (change-actor-state y 4 7)
;; No immediate effect.
;; => (start-actor y)
;; Output should resume, but printing 47 instead of 22.
;; => (stop-actor y)
;; Output should stop.

;; Note that the (actor ...) macro resembles a fn form in
;; use (though destructuring won't work).  In the body, the
;; parameters can be referred to by name to use them. When
;; it is called, it is with an ordered list of values to
;; bind to those parameters. The (change-actor-state ...)
;; function is followed by the actor and then such a
;; parameter list, so in the example above since x was the
;; first parameter (change-actor-state y 4 7) binds 4 to x
;; on subsequent executions of the actor body.

;; Under the hood, it's exactly as described: the actor body
;; is wrapped in a fn with those parameter names and the
;; actor is invoked periodically with an argument list,
;; which is replaced by change-actor-state. The actor itself
;; is an agent wrapping a vector with the function, period,
;; awake flag, and current parameters.

;; Trivial additions: creating a (defactor name ...) macro
;; that functions like (def name (actor ...)); adding a
;; change-actor-period function.

;; More interesting, but fairly easy: have the function take
;; one extra parameter, the previous invocation's return
;; value, and specify an initial value for it in (actor
;; ...). This enables the function to maintain a mutable
;; cell of sorts.

;; Nontrivial: add destructuring.