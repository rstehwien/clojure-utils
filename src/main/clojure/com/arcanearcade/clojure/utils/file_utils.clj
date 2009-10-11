(ns com.arcanearcade.clojure.utils.file-utils
  (:require [clojure.contrib.duck-streams :as duck-streams])
  (:require [clojure.contrib.java-utils :as java-utils])
  (:import (java.io File)))

(defn re-match? [re s] (not (nil? (re-matches re s))))

(comment "cd" isn't a valid command in Java as there is no reliable way to change the working directory)

(def file java-utils/file)

(defn make-parents [f] (duck-streams/make-parents (file f)))

(def pwd duck-streams/pwd)

(defn touch [f] (duck-streams/append-spit (file f) ""))

(defn mv [from to] (.renameTo (file from) (file to)))

(defn cp [from to] (str "TODO copy " from " to " to))

(defn ls
  ([] (ls "."))
  ([f] (seq (.listFiles (file f)))))

(defn ls_r
  ([] (ls_r "."))
  ([f] (file-seq (file f))))

(defn mkdir [f] (.mkdir (file f)))

(defn mkdirs [f] (.mkdirs (file f)))

(defn rm [f] (.delete (file f)))

(defn rm_rf [path]
  (let [p (file path)]
    (if (.isDirectory p) (doseq [cur (ls p)] (rm_rf cur)))
    (rm p)))

(defn filter-files [files re]
  (filter #(re-match? re (.toString %)) files))

(defn filter-files-ends-with [files suffix]
  (filter #(.. % toString (endsWith suffix)) files))

(def file-seq-cache (ref {}))

(defn file-seq-cache-key [f] (.toString (file f)))

(defn clear-file-seq-cache
  ([]
     (dosync (alter file-seq-cache empty)))
  ([& paths]
     (dosync (doseq [key (map file-seq-cache-key paths)] (alter file-seq-cache dissoc key)))
     @file-seq-cache))

(defn get-file-seq-cache [path]
  (let [f (file path) key (file-seq-cache-key f)]
    (dosync
     (when-not (contains? @file-seq-cache key)
       (alter file-seq-cache conj {path (file-seq (file key))}))
     (@file-seq-cache key))))


