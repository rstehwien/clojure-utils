(ns com.arcanearcade.clojure.utils.file-utils
  (:gen-class)
  (:import (java.io File)))

(comment

(defmulti cd class)
(defmethod cd String [s])
(defmethod cd File [f])

(defn pwd)

(defmulti mkdir class)
(defmethod mkdir String [s])
(defmethod mkdir File [f])

(defmulti mkdirs class)
(defmethod mkdirs String [s])
(defmethod mkdirs File [f])

(defmulti rmdir class)
(defmethod rmdir String [s])
(defmethod rmdir File [f])

(defmulti mv (fn [from to] [(class from) (class to)]))
(defmethod mv [String String] [from to] (println "Both strings"))
(defmethod mv [File File] [from to] (println "Both files"))
(defmethod mv [File String] [from to] (println "File String"))
(defmethod mv [String File] [from to] (println "String File"))

)

(defn mv [from to]
  (let [f (if (= (class from) File) from (File. from))
        t (if (= (class to) File) from (File. to))]
    (println "transformed to File")))

(def file-seq-cache (ref {}))

(defn clear-cached-files
  ([]
     (dosync (alter file-seq-cache empty)))
  ([& ks]
     (dosync (doseq [key ks] (alter file-seq-cache dissoc key)))
     @file-seq-cache))

(defn get-cached-files [path]
  (dosync
    (when-not (contains? @file-seq-cache path)
      (alter file-seq-cache conj {path (file-seq (File. path))}))
    (@file-seq-cache path)))

(defn filter-files-ends-with [files suffix]
  (filter #(.. % toString (endsWith suffix)) files))

(defn files-ending-with [path suffix]
  (filter-files-ends-with (get-cached-files path) suffix))

