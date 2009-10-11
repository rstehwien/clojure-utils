(ns com.arcanearcade.clojure.utils.regex-utils)

(defn re-match? [re s] (not (nil? (re-matches re (str s)))))

(defn re-filter [re col] (filter #(re-match? re %) col))
