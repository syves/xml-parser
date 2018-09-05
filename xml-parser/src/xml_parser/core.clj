(ns xml-parser.core

(:require [clojure.java.io :as io]
          [clojure.xml :as xml]
          [clojure.zip :as zip]
          [clojure.data.xml :refer :all]
          [clojure.data.zip.xml :as zip-xml]
          [clojure.pprint :refer [pprint]])
          (:import org.apache.commons.io.input.BOMInputStream
                   org.apache.commons.io.ByteOrderMark
                   java.util.zip.GZIPInputStream))

(def base "/Users/syves/github.com/syves/lambdawerk-backend-test/xml-parser/resources/")
(def gzip-filepath (str base "update-file.xml.gz"))
(def with-bom-gz (str base "withBom.xml.gz"))
;(def small-whitespace (str base "small-whitespace.xml"))
(def members (str base "just-members.xml.gz"))

(defn gzip-reader [filename]
    (-> filename
        io/file
        io/input-stream
        GZIPInputStream.
        io/reader))

;;;;Note cannot read a gziped file without gzipreader
;XMLStreamException ParseError at [row,col]:[1,1]
;Message: Content is not allowed in prolog.

;https://gist.github.com/biggert/6453648
(def bom-array
  (into-array [ByteOrderMark/UTF_16LE
               ByteOrderMark/UTF_16BE
               ByteOrderMark/UTF_8
               ByteOrderMark/UTF_32BE
               ByteOrderMark/UTF_32LE]))

;Note does not strip whitespace 
(defn bom-reader
  "removes BOMs"
  [file]
    ;Reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
    (io/reader
      ;Constructs a new BOM InputStream that excludes the specified BOMs.
      (BOMInputStream.
        ;Creates a new input stream with the specified buffer size.
        (GZIPInputStream.
          (io/input-stream
            (io/file
               file)))
        false
        bom-array)))

;(parse (bom-reader gzip-filepath)) ;;outOfMemboryError
;why does this work?
(def bom-free-rdr (bom-reader gzip-filepath))

;;Returns a lazy tree of the xml/element struct-map,
;;which has the keys :tag, :attrs, and :content. and accessor fns tag, attrs, and content.
(def x (parse bom-free-rdr))

(:tag x)

;;Returns a zipper for xml elements, easily filterable

(defn get-values-from-tree
  [tree]
  (map (fn [person]
         (->> (filter #(= (:tag %) :member)
              (:content person)
              (apply str))))
       (:content tree)))

(get-values-from-tree x)

;(take 10 (get-values-from-tree new-tree))
(->> new-tree
     get-values-from-file)
     (take 10));; remove (take 100000) to get the full sequence

;;var to pass around
(def updates-tree (xml-to-tree filepath))

;;TODO for each child 'member' in root, get its values.. and create a clojure vector for each?
;;2. create an sql query for each hug or HoneySql
(def tree (tree-root filepath))

(->{:tag :root :content [{:tag :member :content
  ["firstname", "last name", "date-of-birth", "phone"]}] }
updtes-tree)
