(ns com.arcanearcade.clojure.utils.test-file-utils
  (:require [com.arcanearcade.clojure.utils.file-utils :as fu])
  (:use clojure.test)
  (:import (java.util UUID)))

(def test-dir (fu/file "." (str "test-dir-" (UUID/randomUUID))))
(def test-dir-s (str test-dir))
(def test-subdir (fu/file test-dir "subdir"))
(def test-file1 (fu/file test-dir "file1.txt"))
(def test-file2 (fu/file test-dir "file2.txt"))

(defn fixture-file-utils [test-function]
  (fu/mkdirs test-subdir)
  (fu/touch test-file1)
  (fu/touch test-file2)
  (fu/touch (fu/file test-dir "file3.blarg"))
  (test-function)
  (fu/rm_rf test-dir))

(use-fixtures :each fixture-file-utils)

(deftest test-file
  (is (= "some_file_name" (str (fu/file "some_file_name"))))
  (is (=
       (str test-dir-s fu/separator "some_file_name")
       (str (fu/file test-dir "some_file_name")))))

(deftest test-read?-pass
  (is (fu/read? ".")))

(deftest test-write?-pass
  (is (fu/write? ".")))

(deftest test-exists?-pass
  (is (fu/exists? "."))
  (is (fu/exists? test-file2))
  (is (fu/exists? test-file2 (fu/ls test-dir)))
  )

(deftest test-exists?-fail
  (is (not (fu/exists? "should_not_exist"))))

(deftest test-not-exists?-pass
  (is (fu/not-exists? "should_not_exist"))
  (is (fu/not-exists? "should_not_exist") (fu/ls_r)))

(deftest test-not-exists?-fail
  (is (not (fu/not-exists? test-file2))))

(deftest test-equals?-pass
  (is (fu/equals? test-file1 test-file1))
  (is (fu/equals? test-file1 (str test-file1)))
  )

(deftest test-equals?-fail
  (is (not (fu/equals? "." ".."))))

(deftest test-equals?-fail
  (is (not (fu/equals? "." []))))

(deftest test-absolute?-pass
  (is (fu/absolute? (fu/absolute-path ".")))
  (is (fu/absolute? (fu/absolute-file "."))))

(deftest test-absolute?-fail
  (is (not (fu/absolute? test-file1))))

(deftest test-directory?-pass
  (is (fu/directory? "."))
  (is (fu/directory? test-subdir)))

(deftest test-directory?-fail
  (is (not (fu/directory? test-file1))))

(deftest test-file?-pass
  (is (fu/file? test-file1)))

(deftest test-file?-fail
  (is (not (fu/file? test-dir))))

(deftest test-hidden?-fail
  (is (not (fu/hidden? test-dir))))

(deftest test-make-parents
  (let [file (fu/file test-dir "new_dir1" "new_dir2")]
    (is (fu/make-parents file))
    (is (fu/exists? (fu/parent file)))
    (is (not (fu/exists? file)))))

(deftest test-parent
  (is (fu/equals? (fu/parent-file test-subdir) (fu/parent test-subdir))))

(deftest test-pwd
  (is (not (empty? (fu/pwd))))
  (is (= (fu/pwd) (fu/parent (fu/absolute-file ".")))))

(deftest test-touch
  (let [test-file (fu/file test-dir "a_new_file")]
    (is (not (fu/exists? test-file)))
    (fu/touch test-file)
    (is (fu/exists? test-file))))

(deftest test-mv
  (let [dir1  (fu/file test-dir "foo")
        dir2 (fu/file test-dir "smeggle")]
    (fu/mkdirs (fu/file dir1 "bar" "baz"))
    (fu/touch (fu/file dir1 "bar" "baz" "mine.txt"))
    (is (fu/not-exists? dir2 (fu/ls test-dir)))
    (is (fu/exists? (fu/file dir1 "bar" "baz" "mine.txt") (fu/ls_r test-dir)))
    (fu/mv dir1 dir2)
    (is (fu/not-exists? dir1 (fu/ls test-dir)))
    (is (fu/exists? (fu/file dir2 "bar" "baz" "mine.txt") (fu/ls_r test-dir)))
    ))

(deftest test-cp
  (let [dir1  (fu/file test-dir "foo")
        dir2 (fu/file test-dir "smeggle")]
    (fu/mkdirs (fu/file dir1 "bar" "baz"))
    (fu/touch (fu/file dir1 "bar" "baz" "mine.txt"))
    (is (fu/not-exists? dir2 (fu/ls test-dir)))
    (is (fu/exists? (fu/file dir1 "bar" "baz" "mine.txt") (fu/ls_r test-dir)))
    (fu/cp dir1 dir2)
    (is (fu/exists? (fu/file dir1 "bar" "baz" "mine.txt") (fu/ls_r test-dir)))
    (is (fu/exists? (fu/file dir2 "bar" "baz" "mine.txt") (fu/ls_r test-dir)))
    ))

(deftest test-ls
  (is (fu/exists? test-dir (fu/ls)))
  (is (fu/not-exists? (fu/file test-dir "not_exist") (fu/ls test-dir)))
  )

(deftest test-ls_r
  (let [subfile (fu/file test-subdir "subfile.txt")]
    (is (fu/not-exists? subfile (fu/ls_r test-dir)))
    (fu/touch subfile)
    (is (fu/exists? subfile (fu/ls_r test-dir)))
    (is (fu/not-exists? subfile (fu/ls test-dir)))
    ))

;; TODO get rid of calls to not-any? and some in favor of
;; fu/not-exists? and fu/exists?

(deftest test-mkdir
  (let [subdir (fu/file test-dir "my_subdir")]
    (is (not-any? #(fu/equals? % subdir) (fu/ls test-dir)))
    (fu/mkdir subdir)
    (is (some #(fu/equals? % subdir) (fu/ls test-dir)))
    ))

(deftest test-mkdirs
  (let [subsubdir (fu/file test-dir "my_subdir" "another_subdir")]
    (is (not-any? #(fu/equals? % subsubdir) (fu/ls_r test-dir)))
    (is (not (fu/mkdir subsubdir)))
    (is (not-any? #(fu/equals? % subsubdir) (fu/ls_r test-dir)))
    (is (fu/mkdirs subsubdir))
    (is (some #(fu/equals? % subsubdir) (fu/ls_r test-dir)))
    (is (not-any? #(fu/equals? % subsubdir) (fu/ls test-dir)))
    ))

(deftest test-rm-file
  (let [file (fu/file test-dir "a_new_file")]
    (is (not-any? #(fu/absolute-path-equals? % file) (fu/ls test-dir)))
    (fu/touch file)
    (is (some #(fu/absolute-path-equals? % file) (fu/ls test-dir)))
    (fu/rm file)
    (is (not-any? #(fu/absolute-path-equals? % file) (fu/ls test-dir)))
    ))

(deftest test-rm_rf-file
  (let [subdir (fu/file test-dir "new_subdir")
        file (fu/file subdir "a_new_file")]
    (is (not-any? #(fu/absolute-path-equals? % file) (fu/ls_r test-dir)))
    (fu/make-parents file)
    (fu/touch file)
    (is (fu/exists? file (fu/ls_r test-dir)))
    (is (not (fu/rm subdir)))
    (is (some #(fu/absolute-path-equals? % file) (fu/ls_r test-dir)))
    (is (fu/rm_rf subdir))
    (is (not-any? #(fu/absolute-path-equals? % subdir) (fu/ls_r test-dir)))
    ))

(deftest test-filter-files-pass
  (is (< 0 (count (fu/re-filter-files #"\..*txt" (fu/ls test-dir))))))

(deftest test-filter-files-fail
  (is (= 0 (count (fu/re-filter-files #".*zip" (fu/ls test-dir))))))

;; TODO test the cache some more

(deftest test-keys-file-seq-cache
  (fu/clear-file-seq-cache)
  (is (= 0 (count (keys @fu/file-seq-cache))))
  (fu/get-file-seq-cache "should_not_exist1")
  (is (some #{"should_not_exist1"} (keys @fu/file-seq-cache)))
  )
