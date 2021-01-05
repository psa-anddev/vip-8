(ns vip-8.rom-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :refer [file input-stream output-stream]]
            [vip-8.rom :refer :all]))

(deftest load-rom-test
  (testing "A ROM cam be loaded"
    (let [file (java.io.File/createTempFile "test" nil)
          expected [0 224 42 64]]
      (with-open [out (output-stream file)]
        (loop [rbytes expected]
          (if (not-empty rbytes)
            (let [next-byte (first rbytes)]
              (.write out next-byte)
              (recur (rest rbytes)))
            nil)))
      (is (= expected
             (read-rom (.getAbsolutePath file)))))))
