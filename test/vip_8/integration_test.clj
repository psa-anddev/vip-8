(ns vip-8.integration-test
  (:require [clojure.test :refer :all]
            [vip-8.core :as core]
            [vip-8.rom :as rom]
            [vip-8.screen :as screen]))

(defn run-instructions [status runs]
  (loop [remaining-runs runs
         result status]
    (if (zero? remaining-runs)
      result
      (recur (dec remaining-runs)
             (core/step result)))))

(deftest ibm-logo-test
  (with-redefs [rom/read-rom 
                (fn [_]
                  [0x00 0xe0 0xa2 0x2a 0x60 0x0c 0x61 0x08 0xd0 0x1f 0x70 0x09 0xa2 0x39 0xd0 0x1f
                   0xa2 0x48 0x70 0x08 0xd0 0x1f 0x70 0x04 0xa2 0x57 0xd0 0x1f 0x70 0x08 0xa2 0x66
                   0xd0 0x1f 0x70 0x08 0xa2 0x75 0xd0 0x1f 0x12 0x28 0xff 0x00 0xff 0x00 0x3c 0x00
                   0x3c 0x00 0x3c 0x00 0x3c 0x00 0xff 0x00 0xff 0xff 0x00 0xff 0x00 0x38 0x00 0x3f
                   0x00 0x3f 0x00 0x38 0x00 0xff 0x00 0xff 0x80 0x00 0xe0 0x00 0xe0 0x00 0x80 0x00
                   0x80 0x00 0xe0 0x00 0xe0 0x00 0x80 0xf8 0x00 0xfc 0x00 0x3e 0x00 0x3f 0x00 0x3b
                   0x00 0x39 0x00 0xf8 0x00 0xf8 0x03 0x00 0x07 0x00 0x0f 0x00 0xbf 0x00 0xfb 0x00
                   0xf3 0x00 0xe3 0x00 0x43 0xe0 0x00 0xe0 0x00 0x80 0x00 0x80 0x00 0x80 0x00 0x80
                   0x00 0xe0 0x00 0xe0])]

    (let [initial-status (core/load-rom "ibm-logo.ch8")]
      (testing "loading a ROM"
        (is (= (nth (:memory initial-status) 
                    (:pc (:registers initial-status)))
               0x00)))
      (let [screen (atom (into [] (repeat 32 (into [] (repeat 64 true)))))]
        (with-redefs [screen/clear (fn [] (swap! screen (fn [_] (into [] (repeat 32 (into [] (repeat 64 false)))))))
                      screen/is-on? (fn [x y] 
                                      (let [current-screen @screen]
                                        (nth (nth current-screen y)
                                             x)))
                      screen/set (fn [x y on?] 
                                   (swap! screen 
                                          (fn [v] 
                                            (assoc v
                                                   y
                                                   (assoc (nth v y)
                                                          x
                                                          on?)))))
                      screen/width (fn [] 64)
                      screen/height (fn [] 32)]
          (testing "First instruction in the execution"
            (let [status (core/step initial-status)]
              (is (= (:pc (:registers status))
                     0x202))
              (is (= @screen
                     (repeat 32 (repeat 64 false))))))
          (testing "First two instructions in the execution"
            (let [status (run-instructions initial-status 2)
                  registers (:registers status)]
              (is (= (:pc registers) 0x204))
              (is (= (:index registers) 0x22a))))
          (testing "First three instructions in the execution"
            (let [status (run-instructions initial-status 3)
                  registers (:registers status)]
              (is (= (:pc registers) 0x206))
              (is (= (:v0 registers)
                     0x0C))))
          (testing "First four instructions in the execution"
            (let [status (run-instructions initial-status 4)
                  registers (:registers status)]
              (is (= (:pc registers) 0x208))
              (is (= (:v1 registers) 0x08))))
          (testing "First five instructions in the execution"
            (let [status (run-instructions initial-status 5)
                  registers (:registers status)]
              (is (= (:pc registers) 0x20A))
              (let [result-screen @screen]
                (loop [x 12
                       y 8]
                  (when (< y 0xf)
                    (let [actual (nth (nth result-screen y) x)]
                      (is actual)
                      (when actual
                        (recur (if (< x 19) (inc x) 12)
                               (if (= x 19) (inc y) y)))))))
              (is (= (:vF registers) 0x00))))
          (testing "Execution up to the first 0x7 instruction"
            (let [status (run-instructions initial-status 6)
                  registers (:registers status)]
              (is (= (:pc registers) 0x20C))
              (is (= (:v0 registers) 0x15))))
          (testing "Sets the index to 0x239"
            (let [status (run-instructions initial-status 7)
                  registers (:registers status)]
              (is (= (:pc registers) 0x20E))
              (is (= (:index registers) 0x239))))
          (testing "Sprite is drawn in position X: 21 Y: 8"
            (let [status (run-instructions initial-status 8)
                  registers (:registers status)
                  result-screen @screen]
              (is (= (:pc registers) 0x210))
              (loop [x 21
                     y 8]
                (let [actual (nth (nth result-screen y) x)]
                  (when (< y 0xf)
                    (is actual)
                    (when actual
                      (recur (if (< x 28) (inc x) 21)
                             (if (= x 28) (inc y) y))))))))
          (testing "Something"
            (let [status (run-instructions initial-status 11)])))))))


