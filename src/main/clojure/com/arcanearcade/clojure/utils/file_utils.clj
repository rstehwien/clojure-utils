;; Copyright (c) Robert Stehwien, Oct 2009. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns
    #^{:author "Robert Stehwien",
       :doc "A Clojure wrapper for java.io.File plus useful utilities"}
  com.arcanearcade.clojure.utils.file-utils
  (:require [clojure.contrib.duck-streams :as du])
  (:require [clojure.contrib.java-utils :as ju])
  (:require [clojure.contrib.seq-utils :as su])
  (:require [com.arcanearcade.clojure.utils.regex-utils :as ru])
  (:import (java.io File BufferedReader FileReader))
  (:import (java.util.zip CRC32)))

;; "cd" isn't a valid command in Java as there is no reliable way to change the working directory

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

(defn file-name [f] (.getName (file f)))

(def pwd du/pwd)

(defn touch [f] (du/append-spit (file f) ""))

(defn mv [from to] (.renameTo (file from) (file to)))

(defn mkdir [f] (.mkdir (file f)))

(defn mkdirs [f] (.mkdirs (file f)))

(defn ls
  ([] (ls "."))
  ([f] (seq (.listFiles (file f)))))

(defn ls_r
  ([] (ls_r "."))
  ([f] (file-seq (file f))))

(defn cp [from to]
  (let [dest (if (directory? to) (file to (file-name from)) to)]
    (if (directory? from)
      (do (mkdir dest)
          (doseq [cur (ls from)] (cp cur dest)))
      (du/copy from dest))))

(defn rm [f] (.delete (file f)))
(defn delete-on-exit [f] (.deleteOnExit (file f)))

(defn rm_rf [path]
  (let [p (file path)]
    (if (directory? p) (doseq [cur (ls p)] (rm_rf cur)))
    (rm p)))

(defn re-filter-files [re files] (ru/re-filter re files))
(defn re-exists? [re files] (not-empty (re-filter-files re files)))

(defn byte-seq [rdr]
  (let [result (. rdr read)]
    (if (= result -1)
      (do (. rdr close) nil)
      (lazy-seq (cons (byte result) (byte-seq rdr))))))

(defn crc32 [f]
  (let [rdr (BufferedReader. (FileReader. (file f)))
        bytes (byte-seq rdr)
        crc (CRC32.)]
    (doseq [chunk (su/partition-all 256 bytes)] (.update crc (into-array Byte/TYPE chunk)))
    (.getValue crc)))


(let [file-seq-cache (ref {})]

  (defn file-seq-cache-key [f] (str (file f)))

  (defn file-seq-cache-keys [] (keys @file-seq-cache))

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

  )
