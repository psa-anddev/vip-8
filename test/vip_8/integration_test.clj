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
              (let [result-screen @screen
                    white? (fn [x y] (nth (nth result-screen y) x))]
                (is (white? 12 8))
                (is (white? 13 8))
                (is (white? 14 8))
                (is (white? 15 8))
                (is (white? 16 8))
                (is (white? 17 8))
                (is (white? 18 8))
                (is (white? 19 8))
                (is (not (white? 12 9)))
                (is (not (white? 13 9)))
                (is (not (white? 14 9)))
                (is (not (white? 15 9)))
                (is (not (white? 16 9)))
                (is (not (white? 17 9)))
                (is (not (white? 18 9)))
                (is (not (white? 19 9)))
                (is (white? 12 10))
                (is (white? 13 10))
                (is (white? 14 10))
                (is (white? 15 10))
                (is (white? 16 10))
                (is (white? 17 10))
                (is (white? 18 10))
                (is (white? 19 10))
                (is (not (white? 12 11)))
                (is (not (white? 13 11)))
                (is (not (white? 14 11)))
                (is (not (white? 15 11)))
                (is (not (white? 16 11)))
                (is (not (white? 17 11)))
                (is (not (white? 18 11)))
                (is (not (white? 19 11)))
                (is (not (white? 12 12)))
                (is (not (white? 13 12)))
                (is (white? 14 12))
                (is (white? 15 12))
                (is (white? 16 12))
                (is (white? 17 12))
                (is (not (white? 18 12)))
                (is (not (white? 19 12)))
                (is (not (white? 12 13)))
                (is (not (white? 13 13)))
                (is (not (white? 14 13)))
                (is (not (white? 15 13)))
                (is (not (white? 16 13)))
                (is (not (white? 17 13)))
                (is (not (white? 18 13)))
                (is (not (white? 19 13)))
                (is (not (white? 12 14)))
                (is (not (white? 13 14)))
                (is (white? 14 14))
                (is (white? 15 14))
                (is (white? 16 14))
                (is (white? 17 14))
                (is (not (white? 18 14)))
                (is (not (white? 19 14)))
                (is (not (white? 12 15)))
                (is (not (white? 13 15)))
                (is (not (white? 14 15)))
                (is (not (white? 15 15)))
                (is (not (white? 16 15)))
                (is (not (white? 17 15)))
                (is (not (white? 18 15)))
                (is (not (white? 19 15)))
                (is (not (white? 12 16)))
                (is (not (white? 13 16)))
                (is (white? 14 16))
                (is (white? 15 16))
                (is (white? 16 16))
                (is (white? 17 16))
                (is (not (white? 18 16)))
                (is (not (white? 19 16)))
                (is (not (white? 12 17)))
                (is (not (white? 13 17)))
                (is (not (white? 14 17)))
                (is (not (white? 15 17)))
                (is (not (white? 16 17)))
                (is (not (white? 17 17)))
                (is (not (white? 18 17)))
                (is (not (white? 19 17)))
                (is (not (white? 12 18)))
                (is (not (white? 13 18)))
                (is (white? 14 18))
                (is (white? 15 18))
                (is (white? 16 18))
                (is (white? 17 18))
                (is (not (white? 18 18)))
                (is (not (white? 19 18)))
                (is (not (white? 12 19)))
                (is (not (white? 13 19)))
                (is (not (white? 14 19)))
                (is (not (white? 15 19)))
                (is (not (white? 16 19)))
                (is (not (white? 17 19)))
                (is (not (white? 18 19)))
                (is (not (white? 19 19)))
                (is (white? 12 20))
                (is (white? 13 20))
                (is (white? 14 20))
                (is (white? 15 20))
                (is (white? 16 20))
                (is (white? 17 20))
                (is (white? 18 20))
                (is (white? 19 20))
                (is (not (white? 12 21)))
                (is (not (white? 13 21)))
                (is (not (white? 14 21)))
                (is (not (white? 15 21)))
                (is (not (white? 16 21)))
                (is (not (white? 17 21)))
                (is (not (white? 18 21)))
                (is (not (white? 19 21)))
                (is (white? 12 22))
                (is (white? 13 22))
                (is (white? 14 22))
                (is (white? 15 22))
                (is (white? 16 22))
                (is (white? 17 22))
                (is (white? 18 22))
                (is (white? 19 22)))
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
          (testing "Application jumps to address 0x228"
            (let [status (run-instructions initial-status 21)]
              (is (= (:pc (:registers status))
                     0x228)))))))))


