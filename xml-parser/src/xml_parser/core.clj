(ns xml-parser.core

(:require [clojure.java.io :as io]
          [clojure.xml :as xml]
          [clojure.zip :as zip]
          ;[clojure.data.xml :refer :all]
          [clojure.data.xml :as c-d-xml :refer [parse]]
          [clojure.data.zip.xml :as c-d-z-xml
                :refer [xml-> xml1-> attr attr= text]]
          [clojure.pprint :refer [pprint]])
          (:import org.apache.commons.io.input.BOMInputStream
                   org.apache.commons.io.ByteOrderMark
                   java.util.zip.GZIPInputStream))

(def base "/Users/syves/github.com/syves/lambdawerk-backend-test/xml-parser/resources/")
(def gzip-filepath (str base "update-file.xml.gz"))
(def with-bom-gz (str base "withBom.xml.gz"))
;(def small-whitespace (str base "small-whitespace.xml"))
(def members-gz (str base "just-members.xml.gz"))

(defn gzip-reader [filename]
    (-> filename
        io/file
        io/input-stream
        GZIPInputStream.
        io/reader))

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

;;Returns a lazy tree of the xml/element struct-map,
;;which has the keys :tag, :attrs, and :content. and accessor fns tag, attrs, and content.

;op takes 75 sec
(def list-map
  (with-open [rdr (bom-reader gzip-filepath)]
    (doall
      (take 1500000
        (->> rdr
             parse
             :content
             (map (fn [member]
                      (reduce (fn [acc elem]
                                  (assoc acc
                                         (:tag elem)
                                         (first (:content elem))))
                              {}
                              (:content member)))))))))

;for each map create a sql query
(map f list-map)

;output query to file?
