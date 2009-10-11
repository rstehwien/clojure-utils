(ns com.arcanearcade.clojure.utils.file-utils
  #^{:author "Robert Stehwien",
     :doc "A Clojure wrapper for java.io.File plus useful utilities"}
  (:require [clojure.contrib.duck-streams :as du])
  (:require [clojure.contrib.java-utils :as ju])
  (:require [com.arcanearcade.clojure.utils.regex-utils :as ru])
  (:import (java.io File)))

(comment "cd" isn't a valid command in Java as there is no reliable way to change the working directory)

(def file ju/file)

(def separator (File/separator))
(def path-separator (File/pathSeparator))
(defn read? [f] (.canRead (file f)))
(defn write? [f] (.canWrite (file f)))

(defmulti equals? (fn[f o] (class o)))
(defmethod equals? String [f #^String s] (.equals (file f) (file s)))
(defmethod equals? File [f #^File o] (.equals f o))
(defmethod equals? :default [f o] (.equals (file f) o))

(defn absolute? [f] (.isAbsolute (file f)))
(defn #^File absolute-file [f] (.getAbsoluteFile (file f)))
(defn #^String absolute-path [f] (.getAbsolutePath (file f)))

(defn absolute-path-equals? [f1 f2] (= (absolute-path f1) (absolute-path f2)))

(defn exists?
  ([f] (.exists (file f)))
  ([f col] (some #(absolute-path-equals? % f) col)))

(defn not-exists?
  ([f] (not (exists? f)))
  ([f col] (not (exists? f col))))

(defn directory? [f] (.isDirectory (file f)))
(defn file? [f] (.isFile (file f)))
(defn hidden? [f] (.isHidden (file f)))

(defn make-parents [f] (du/make-parents (file f)))
(defn #^String parent [f] (.getParent (file f)))
(defn #^File parent-file [f] (.getParentFile (file f)))

(def pwd du/pwd)

(defn touch [f] (du/append-spit (file f) ""))

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
    (if (directory? p) (doseq [cur (ls p)] (rm_rf cur)))
    (rm p)))

(defn re-filter-files [re files] (ru/re-filter re files))

(def file-seq-cache (ref {}))

(defn file-seq-cache-key [f] (str (file f)))

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


