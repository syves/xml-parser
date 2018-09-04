(ns xml-parser.core

(:require [clojure.java.io :as io]
          [clojure.xml :as xml]
          [clojure.zip :as zip]
          [clojure.data.xml :refer :all]
          [clojure.data.zip.xml :as zip-xml]
          [clojure.pprint :refer [pprint]]))

(def base "/Users/syves/github.com/syves/lambdawerk-backend-test/xml-parser/resources/")
(def gzip-filepath (str base "update-file.xml.gz"))
(def test-str (str base "test-str.xml"))
(def test-dtd (str base "with-dtd.xml"))

;returns a buffer reader
(defn gzip-reader [filename]
    (-> filename
        ;java.io.File, passing each arg to as-file
        io/file
        ;Attempts to coerce its argument into an open java.io.InputStream.
        io/input-stream
        ;Reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
        io/reader))

(def rdr (gzip-reader test-str))

(defn tree [reader](parse reader))

;;Returns a lazy tree of the xml/element struct-map,
;;which has the keys :tag, :attrs, and :content. and accessor fns tag, attrs, and content.
(:tag (parse (io/reader (io/input-stream (io/file test-dtd)))))
(parse (io/reader (io/input-stream (io/file gzip-filepath))))

(def new-tree (parse (io/reader (io/input-stream (io/file test-dtd)))))
(:tag new-tree)


(defn tree [file]
    (xml/parse (gzip-reader file)))

    ;;Returns a zipper for xml elements, easily filterable
    ;zip/xml-zip

(defn get-values-from-tree
  [tree]
  (map (fn [person]
         (->> (filter #(= (:tag %) :member)
              (:content person)
              (apply str))))
       (:content tree)))

;(take 10 (get-values-from-tree new-tree))
(->> new-tree
     get-values-from-file)
     (take 10));; remove (take 100000) to get the full sequence

;;var to pass around
(def updates-tree (xml-to-tree filepath))

;;TODO for each child 'member' in root, get its values.. and create a clojure vector for each?
;;2. create an sql query for each hug or HoneySql
(def tree (tree-root filepath))

(->{:tag :root :content [{:tag :member :content ["firstname", "last name", "date-of-birth", "phone"]}] }
updtes-tree
)
