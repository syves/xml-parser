(ns xml-parser.core)

(require '[clojure.java.io :as io])
(require '[clojure.xml :as xml])
(require '[clojure.zip :as zip])
(require '[clojure.data.zip.xml :as zip-xml])

  (defn meta->map
  [root]
  (into {}
        (for [m (zip-xml/xml-> root :head :meta)]
          [(keyword (zip-xml/attr m :type))
           (zip-xml/text m)])))

(defn nzb->map
  [input]
  (let [root (-> input
                 io/input-stream
                 xml/parse
                 zip/xml-zip)]
    {:meta  (meta->map root)
     :files (mapv file->map (zip-xml/xml-> root :file))}))


  (def filepath "/Users/syves/github.com/syves/lambdawerk-backend-test/xml-parser/resources/update-file.xml")

  (defn zip-str [file]
    (zip/xml-zip
      (xml/parse
        (io/input-stream (java.io.File. file)))))
  (def updates (zip-str filepath))
(println updates)
