(ns vip-8.rom
  (:require [clojure.java.io :refer [file input-stream]]))

(defn read-rom 
  "Reads a ROM from a file and returns a vector of bytes"
  [filename]
  (let [f (file filename)]
    (with-open [in (input-stream f)]
      (loop [result []]
        (let [next-byte (.read in)]
          (if (= next-byte -1)
            result
            (recur (conj result next-byte))))))))
