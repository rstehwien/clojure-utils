(ns rstehwien.clojure.fileutils
  (:gen-class)
  (:import (java.io File)))

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

(count (files-ending-with "should_not_exist1" ".zip"))
(count (files-ending-with "should_not_exist2" ".zip"))
(count (files-ending-with "should_not_exist3" ".zip"))


(defn -main
  ([greetee]
    (println (str "fileutils2 Hello " greetee "!")))
  ([] (-main "world")))
