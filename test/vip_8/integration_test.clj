(ns vip-8.integration-test
  (:require [clojure.test :refer :all]
            [vip-8.core :as core]
            [vip-8.rom :as rom]
            [vip-8.screen :as screen]))

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
                      screen/is-on? (fn [x y] (nth y (nth x @screen)))
                      screen/set (fn [x y on?] 
                                   (swap! screen 
                                          (fn [v] 
                                            (assoc v
                                                   x
                                                   (assoc (nth v x)
                                                          y
                                                          on?)))))]
          (testing "First instruction in the execution"
            (let [status (core/step initial-status)]
              (is (= (:pc (:registers status))
                     0x202))
              (is (= @screen
                     (repeat 32 (repeat 64 false))))))
          (testing "First two instructions in the execution"
            (let [status (core/step (core/step initial-status))
                  registers (:registers status)]
              (is (= (:pc registers) 0x204))
              (is (= (:index registers) 0x22a))))
          (testing "First three instructions in the execution"
            (let [status (core/step (core/step (core/step initial-status)))
                  registers (:registers status)]
              (is (= (:pc registers) 0x206))
              (is (= (:v0 registers)
                     0x0C))))
          (testing "First four instructions in the execution"
            (let [status (core/step  (core/step (core/step (core/step initial-status))))
                  registers (:registers status)]
              (is (= (:pc registers) 0x208))
              (is (= (:v1 registers) 0x08))))
          (testing "First four instructions in the execution"
            (let [status (core/step (core/step  (core/step (core/step (core/step initial-status)))))
                  registers (:registers status)]
              (is (= (:pc registers) 0x20A))
              (let [result-screen @screen]
                (loop [x 0
                       y 0]
                  (if (< y 0xf)
                    (let [actual (nth (nth result-screen x) y)]
                      (is actual)
                      (recur (if (< x 7) (inc x) 0)
                             (if (= x 7) (inc y) y))))))
              (is (= (:vF registers) 0x00)))))))))


