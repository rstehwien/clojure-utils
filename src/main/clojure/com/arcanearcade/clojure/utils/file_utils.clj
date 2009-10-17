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
  (:require [clojure.contrib.def :as ccd])
  (:require [com.arcanearcade.clojure.utils.regex-utils :as ru])
  (:import (java.io File BufferedReader FileReader))
  (:import (java.util.zip CRC32)))

;; "cd" isn't a valid command in Java as there is no reliable way to change the working directory

(def file ju/file)

(def separator (File/separator))
(def path-separator (File/pathSeparator))
(defn read? [file-or-dir] (.canRead (file file-or-dir)))
(defn write? [file-or-dir] (.canWrite (file file-or-dir)))

(defmulti equals? (fn[file-or-dir o] (class o)))
(defmethod equals? String [file-or-dir #^String s] (.equals (file file-or-dir) (file s)))
(defmethod equals? File [file-or-dir #^File o] (.equals file-or-dir o))
(defmethod equals? :default [file-or-dir o] (.equals (file file-or-dir) o))

(defn absolute? [file-or-dir] (.isAbsolute (file file-or-dir)))
(defn #^File absolute-file [file-or-dir] (.getAbsoluteFile (file file-or-dir)))
(defn #^String absolute-path [file-or-dir] (.getAbsolutePath (file file-or-dir)))

(defn absolute-path-equals? [file-or-dir1 file-or-dir2]
  (= (absolute-path file-or-dir1) (absolute-path file-or-dir2)))

(defn exists?
  ([file-or-dir] (.exists (file file-or-dir)))
  ([file-or-dir coll] (some #(absolute-path-equals? % file-or-dir) coll)))

(defn not-exists?
  ([file-or-dir] (not (exists? file-or-dir)))
  ([file-or-dir coll] (not (exists? file-or-dir coll))))

(defn directory? [file-or-dir] (.isDirectory (file file-or-dir)))

(defn file? [file-or-dir] (.isFile (file file-or-dir)))

(defn hidden? [file-or-dir] (.isHidden (file file-or-dir)))

(defn make-parents [file-or-dir] (du/make-parents (file file-or-dir)))

(defn #^String parent [file-or-dir] (.getParent (file file-or-dir)))

(defn #^File parent-file [file-or-dir] (.getParentFile (file file-or-dir)))

(defn file-name [file-or-dir] (.getName (file file-or-dir)))

(def pwd du/pwd)

(defn touch [file-or-dir] (du/append-spit (file file-or-dir) ""))

(defn mv [from to] (.renameTo (file from) (file to)))

(defn mkdir [file-or-dir] (.mkdir (file file-or-dir)))

(defn mkdirs [file-or-dir] (.mkdirs (file file-or-dir)))

(defn ls
  ([] (ls "."))
  ([file-or-dir] (seq (.listFiles (file file-or-dir)))))

(defn ls_r
  ([] (ls_r "."))
  ([file-or-dir] (file-seq (file file-or-dir))))

(defn cp [from to]
  (let [dest (if (directory? to) (file to (file-name from)) to)]
    (if (directory? from)
      (do (mkdir dest)
          (doseq [cur (ls from)] (cp cur dest)))
      (du/copy from dest))))

(defn rm [file-or-dir] (.delete (file file-or-dir)))
(defn delete-on-exit [file-or-dir] (.deleteOnExit (file file-or-dir)))

(defn rm_rf [path]
  (let [p (file path)]
    (if (directory? p) (doseq [cur (ls p)] (rm_rf cur)))
    (rm p)))

(defn re-filter-files [re files] (ru/re-filter re files))
(defn re-exists? [re files] (not-empty (re-filter-files re files)))

(defn byte-seq-reader [rdr]
  (let [result (. rdr read)]
    (if (= result -1)
      (do (. rdr close) nil)
      (lazy-seq (cons (byte result) (byte-seq-reader rdr))))))

(defn byte-seq-file [afile]
  (byte-seq-reader (BufferedReader. (FileReader. (file afile)))))


;; TODO make crc32 crc32-file and crc32-reader a multimethod

(defn- crc32 [bytes]
  (let [crc (CRC32.)]
    (doseq [chunk (su/partition-all 256 bytes)] (.update crc (into-array Byte/TYPE chunk)))
    (.getValue crc)))

(defn crc32-file [afile] (crc32 (byte-seq-file afile)))
(defn crc32-reader [rdr] (crc32 (byte-seq-reader rdr)))

(let [file-seq-cache (ref {})]

  (defn file-seq-cache-key [file-or-dir] (str (file file-or-dir)))

  (defn file-seq-cache-keys [] (keys @file-seq-cache))

  (defn clear-file-seq-cache
    ([]
       (dosync (alter file-seq-cache empty)))
    ([& paths]
       (dosync (doseq [key (map file-seq-cache-key paths)] (alter file-seq-cache dissoc key)))
       @file-seq-cache))

  (defn get-file-seq-cache [path]
    (let [file-or-dir (file path) key (file-seq-cache-key file-or-dir)]
      (dosync
       (when-not (contains? @file-seq-cache key)
         (alter file-seq-cache conj {path (file-seq (file key))}))
       (@file-seq-cache key))))

  )

