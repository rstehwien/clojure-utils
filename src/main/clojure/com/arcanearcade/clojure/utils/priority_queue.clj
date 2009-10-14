;; Originally written by John Harrop
;; http://groups.google.com/group/clojure/msg/b2c0b2e2ca175440?hl=en

(ns com.arcanearcade.clojure.utils.priority-queue
  (:require [clojure.contrib.core :as cc-core]))

(defn- third [s]
  (nth s 2))

(defn- heapify [pri i v]
  (let [i-left (inc (* i 2))
        i-right (inc i-left)
        e (get v i)
        e-left (get v i-left)
        e-right (get v i-right)
        p (pri e)
        p-left (if e-left (pri e-left))
        p-right (if e-right (pri e-right))
        pp (if e-left [p-left p] [p])
        ppp (if e-right (cons p-right pp) pp)
        m (apply max ppp)]
    (if (= m p)
      v
      (if (= m p-left)
        (assoc
            (heapify pri i-left
                     (assoc v i-left e))
          i e-left)
        (assoc
            (heapify pri i-right
                     (assoc v i-right e))
          i e-right)))))

(defn heap?
  "Returns logical true if and only if obj is a heap."
  [obj]
  (if (cc-core/seqable? obj)
    (let [s (seq obj)]
      (and
       (= (first s) :heap)
       (= (count s) 3)
       (ifn? (second s))
       (cc-core/seqable? (third s))))))

(defn- throw-iae [& msg-fragments]
  (throw (IllegalArgumentException. (apply str msg-fragments))))

(defn empty-heap
  "Given a function to compute the priority of an element, returns an empty heap
that will use that function."
  [pri]
  (if (ifn? pri)
    [:heap pri []]
    (throw-iae pri " is not invokable.")))

(defn- validate-heap [heap]
  (if-not (heap? heap)
    (throw-iae heap " is not a heap.")))

(defn heap-max
  "Returns a heap's maximum element, if there is one, else nil."
  [heap]
  (validate-heap heap)
  (first (third heap)))

(defn heap-size
  "Returns a heap's size."
  [heap]
  (validate-heap heap)
  (count (third heap)))

(defn heap-empty?
  "Returns logical true if and only if the heap is empty."
  [heap]
  (zero? (heap-size heap)))

(defn heap-remove-max
  "Removes a heap's maximum element. If the heap is empty, does nothing."
  [heap]
  (validate-heap heap)
  (let [pri (second heap)
        v (third heap)
        lst (last v)]
    (if (= 1 (count v))
      [:heap pri []]
      [:heap pri (heapify pri 0 (assoc (pop v) 0 lst))])))

(defn- heap-insert* [pri i v]
  (if (zero? i)
    v
    (let [parent (quot (dec i) 2)
          p-elt (get v parent)
          i-elt (get v i)
          pri-p (pri p-elt)
          pri-i (pri i-elt)]
      (if (> pri-p pri-i)
        v
        (recur pri parent (assoc (assoc v i p-elt) parent i-elt))))))

(defn heap-insert
  "Adds obj to heap. It cannot be nil."
  [heap obj]
  (validate-heap heap)
  (if (nil? obj)
    (throw-iae "Cannot add nil to a heap.")
    (let [pri (second heap)
          v (conj (third heap) obj)]
      [:heap pri (heap-insert* pri (dec (count v)) v)])))

(defn heap-seq
  "Returns a lazy sequence of the contents of the heap, in descending priority
order."
  [heap]
  (lazy-seq
    (if-not (heap-empty? heap)
      (cons (heap-max heap) (heap-seq (heap-remove-max heap))))))

(defn- heap-test-1 []
  (let [rand-ints (take 50 (repeatedly #(rand-int 50)))
        heap (reduce heap-insert (empty-heap identity) rand-ints)]
    (heap-seq heap)))

(defn priority-queue?
  "Returns logical true if and only if the object is a priority queue."
  [obj]
  (heap? obj))

(defn pq-empty
  "Given a function to compute the priority of an element, returns an empty
priority queue that will use that function."
  [pri]
  (empty-heap pri))

(defn pq-peek
  "Get the highest-priority element of a priority queue."
  [pq]
  (heap-max pq))

(defn pq-pop
  "Remove the highest-priority element of a priority queue."
  [pq]
  (heap-remove-max pq))

(defn pq-empty?
  "Returns logical true if and only if the priority queue is empty."
  [pq]
  (heap-empty? pq))

(defn pq-do
  "Remove the highest priority element of a priority queue and call a function
with that element as argument. If the queue is empty, does nothing. Returns the
new queue. The function's return value is discarded."
  [pq f]
  (if-not (pq-empty? pq)
    (f (pq-peek pq)))
  (pq-pop pq))

(defn pq-offer
  "Offer an element to a priority queue. It must not be nil."
  [pq obj]
  (heap-insert pq obj))

(defn pq-seq
  "Returns a lazy sequence of the contents of the priority queue, in descending
priority order."
  [pq]
  (heap-seq pq))

(defn- pq-test-1 []
  (let [pq0 (pq-empty count)
        names ["Jennifer" "Hayley" "Gary" "Isabelle" "Alexander"
               "John" "Susan" "Mildred" "Karen" "Kenneth"]
        pq (reduce pq-offer pq0 names)
        longest (pq-peek pq)
        pq-1 (pq-pop pq)
        next-longest (pq-peek pq-1)
        pq-2 (pq-offer (pq-pop pq-1) "Angelique")
        l3 (pq-peek pq-2)
        pq-3 (pq-pop pq-2)
        l4 (pq-peek pq-3)]
    [longest next-longest l3 l4 (pq-seq (pq-pop pq-3))]))

(defn- pq-execute [pq job]
  (job)
  pq)

(defn- pq-agent-do [pq]
  (let [this *agent*]
    (send pq pq-do (fn [job]
                     (send this pq-execute (second job))
                     (send this pq-agent-do)))
    pq))

(defn pq-agent-jobs
  "Returns an agent that wraps an initially-empty priority queue of jobs, which
are combinations of a priority and a zero-argument function. The job at the head
of the queue is executed automatically on one of the agent thread-pool threads,
then the next, and so on, any time the queue is nonempty. The job function
return values are discarded, so jobs should have side effects."
  []
  (agent (agent (pq-empty first))))

(defn agent?
  "Returns logical true if and only if the object is an agent."
  [obj]
  (instance? clojure.lang.Agent obj))

(defn- validate-pq-agent [pqa]
  (if-not
      (and
       (agent? pqa)
       (agent? @pqa)
       (priority-queue? @@pqa))
    (throw-iae pqa " is not a pq-agent.")))

(defn pq-agent-add-job
  "Schedules a job with a pq-agent."
  [pq-agent priority job]
  (validate-pq-agent pq-agent)
  (if-not (integer? priority)
    (throw-iae "priority was " priority " but should have been an integer."))
  (if-not (ifn? job)
    (throw-iae job "is not invokable."))
  (let [was-empty (pq-empty? @@pq-agent)]
    (send @pq-agent pq-offer [priority job])
    (if was-empty ; Restart the auto-doing of jobs.
      (send pq-agent pq-agent-do)))
  nil)

(defn- pq-agent-test-1 []
  (let [pqa (pq-agent-jobs)
        pqaaj (fn [n] (pq-agent-add-job pqa n #(do
                                                 (println n)
                                                 (Thread/sleep 3000))))]
    (pqaaj 4)   ; Should generate 11 9 7 4 4 3, or possibly 4 11 9 7 4 3 if the
    (pqaaj 11)  ; first 4 starts executing before the 11 job gets added.
    (pqaaj 7)
    (pqaaj 9)
    (pqaaj 3)
    (pqaaj 4)))

(defn ppq-agent-jobs
  "Like pq-agent-jobs, but parallel. Jobs will be parceled out to all cores."
  []
  (let [pqa (agent (pq-empty first))
        cores (.availableProcessors (Runtime/getRuntime))]
    (take cores (repeatedly #(agent pqa)))))

(defn- validate-ppq-agent [ppqa]
  (if-not
      (cc-core/seqable? ppqa)
    (throw-iae ppqa " is not a ppq-agent."))
  (doseq [x ppqa] (validate-pq-agent x)))

(defn ppq-agent-add-job
  "Schedules a job with a ppq-agent."
  [ppq-agent priority job]
  (validate-ppq-agent ppq-agent)
  (if-not (integer? priority)
    (throw-iae "priority was " priority " but should have been an integer."))
  (if-not (ifn? job)
    (throw-iae job "is not invokable."))
  (let [was-empty (pq-empty? @@(first ppq-agent))]
    (send @(first ppq-agent) pq-offer [priority job])
    (if was-empty ; Restart the auto-doing of jobs.
      (doseq [pq-agent ppq-agent] (send pq-agent pq-agent-do))))
  nil)

(defn- ppq-agent-test-1 []
  (let [ppqa (ppq-agent-jobs)
        ppqaaj (fn [n] (ppq-agent-add-job ppqa n #(do
                                                    (println n)
                                                    (Thread/sleep 3000))))]
    (ppqaaj 4)   ; Should give the same results as pq-agent-test-1 but with
    (ppqaaj 11)  ; the output numbers appearing two at a time on dual-core
    (ppqaaj 7)   ; machines, etc.
    (ppqaaj 9)
    (ppqaaj 3)
    (ppqaaj 4)
    ppqa))

;; I've implemented a Clojure persistent, immutable priority queue
;; data structure (built on a heap, in turn built on a Clojure
;; vector). The namespace below exports the heap operations as well as
;; the priority queue operations in case that's useful. These
;; operations return a new data structure instead of altering the
;; old. The pq-do function executes a specified one-argument function
;; on the top item of the queue for side effects and returns a queue
;; with that item removed. Alternatively, pq-peek can be used to get
;; the item and pq-pop to get the reduced queue.
;;
;; There are a few private test functions as demonstration. Also,
;; there is a job-performing API implemented using the priority
;; queue. It uses agents under the hood, exposing two functions:
;; pq-agent-jobs which returns an empty job queue agent and
;; pq-agent-add-job which takes a job queue agent, priority number,
;; and job (zero-argument fn executed for its side effects) and adds
;; it to the queue. The jobs on the queue will automatically pop and
;; execute one by one whenever the queue is nonempty. When the queue
;; is empty no busy-loop or polling takes place.
;;
;; (You might recognize some of the agent tricks in the pq-agent-jobs
;; code from the earlier actor code I posted. This time I have an
;; agent wrapping another agent, and telling that agent to tell the
;; first agent to repeat itself. It looks crazy, but it works. So does
;; the double-deref in pq-agent-add-job.)
;;
;; The heap API does some argument validation checks. The priority
;; queue API is a bit lazier, relying on the heap API's to keep nil
;; elements out and allowing any heap to be treated as a priority
;; queue. It's really just a set of synonyms for heaps and their
;; operations. The pq-agent-jobs API doesn't do as much validation;
;; pq-agent-add-job checks if the first argument looks like a pq-agent
;; in structure, the second is an integer, and the third is an IFn,
;; but that's it. In particular I couldn't find an easy way to check
;; the IFn for arity 0, short of invoking it in a try block and seeing
;; if an exception is thrown whose ultimate cause's detail message
;; contains "Wrong number of args:". Since the function is presumably
;; going to have side effects, and probably is long-running and the
;; caller wants to run it on some OTHER thread, running it is a bad
;; idea.
;;
;; The ppq-agent-jobs and ppq-agent-add-job functions behave
;; identically except that for each job queue thus created, there is
;; one outer agent per core and the jobs get done in parallel if the
;; host hardware multiprocesses.
;;
;; This code is offered into the public domain. I certify that I am
;; its author and that I hereby relinquish the copyright into the
;; public domain. Put it in clojure.contrib, use it in your projects,
;; polish or improve it, etc. as you see fit. I expect you'll want to
;; at least change the namespace name. :)