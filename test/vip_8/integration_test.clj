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

(deftest chip-8-test-rom-test
  (with-redefs [rom/read-rom 
                (fn [_]
                  [
 0x12 0x4e 0xea 0xac 0xaa 0xea 0xce 0xaa 0xaa 0xae 0xe0 0xa0 0xa0 0xe0 0xc0 0x40
 0x40 0xe0 0xe0 0x20 0xc0 0xe0 0xe0 0x60 0x20 0xe0 0xa0 0xe0 0x20 0x20 0x60 0x40
 0x20 0x40 0xe0 0x80 0xe0 0xe0 0xe0 0x20 0x20 0x20 0xe0 0xe0 0xa0 0xe0 0xe0 0xe0
 0x20 0xe0 0x40 0xa0 0xe0 0xa0 0xe0 0xc0 0x80 0xe0 0xe0 0x80 0xc0 0x80 0xa0 0x40
 0xa0 0xa0 0xa2 0x02 0xda 0xb4 0x00 0xee 0xa2 0x02 0xda 0xb4 0x13 0xdc 0x68 0x01
 0x69 0x05 0x6a 0x0a 0x6b 0x01 0x65 0x2a 0x66 0x2b 0xa2 0x16 0xd8 0xb4 0xa2 0x3e
 0xd9 0xb4 0xa2 0x02 0x36 0x2b 0xa2 0x06 0xda 0xb4 0x6b 0x06 0xa2 0x1a 0xd8 0xb4
 0xa2 0x3e 0xd9 0xb4 0xa2 0x06 0x45 0x2a 0xa2 0x02 0xda 0xb4 0x6b 0x0b 0xa2 0x1e
 0xd8 0xb4 0xa2 0x3e 0xd9 0xb4 0xa2 0x06 0x55 0x60 0xa2 0x02 0xda 0xb4 0x6b 0x10
 0xa2 0x26 0xd8 0xb4 0xa2 0x3e 0xd9 0xb4 0xa2 0x06 0x76 0xff 0x46 0x2a 0xa2 0x02
 0xda 0xb4 0x6b 0x15 0xa2 0x2e 0xd8 0xb4 0xa2 0x3e 0xd9 0xb4 0xa2 0x06 0x95 0x60
 0xa2 0x02 0xda 0xb4 0x6b 0x1a 0xa2 0x32 0xd8 0xb4 0xa2 0x3e 0xd9 0xb4 0x22 0x42
 0x68 0x17 0x69 0x1b 0x6a 0x20 0x6b 0x01 0xa2 0x0a 0xd8 0xb4 0xa2 0x36 0xd9 0xb4
 0xa2 0x02 0xda 0xb4 0x6b 0x06 0xa2 0x2a 0xd8 0xb4 0xa2 0x0a 0xd9 0xb4 0xa2 0x06
 0x87 0x50 0x47 0x2a 0xa2 0x02 0xda 0xb4 0x6b 0x0b 0xa2 0x2a 0xd8 0xb4 0xa2 0x0e
 0xd9 0xb4 0xa2 0x06 0x67 0x2a 0x87 0xb1 0x47 0x2b 0xa2 0x02 0xda 0xb4 0x6b 0x10
 0xa2 0x2a 0xd8 0xb4 0xa2 0x12 0xd9 0xb4 0xa2 0x06 0x66 0x78 0x67 0x1f 0x87 0x62
 0x47 0x18 0xa2 0x02 0xda 0xb4 0x6b 0x15 0xa2 0x2a 0xd8 0xb4 0xa2 0x16 0xd9 0xb4
 0xa2 0x06 0x66 0x78 0x67 0x1f 0x87 0x63 0x47 0x67 0xa2 0x02 0xda 0xb4 0x6b 0x1a
 0xa2 0x2a 0xd8 0xb4 0xa2 0x1a 0xd9 0xb4 0xa2 0x06 0x66 0x8c 0x67 0x8c 0x87 0x64
 0x47 0x18 0xa2 0x02 0xda 0xb4 0x68 0x2c 0x69 0x30 0x6a 0x34 0x6b 0x01 0xa2 0x2a
 0xd8 0xb4 0xa2 0x1e 0xd9 0xb4 0xa2 0x06 0x66 0x8c 0x67 0x78 0x87 0x65 0x47 0xec
 0xa2 0x02 0xda 0xb4 0x6b 0x06 0xa2 0x2a 0xd8 0xb4 0xa2 0x22 0xd9 0xb4 0xa2 0x06
 0x66 0xe0 0x86 0x6e 0x46 0xc0 0xa2 0x02 0xda 0xb4 0x6b 0x0b 0xa2 0x2a 0xd8 0xb4
 0xa2 0x36 0xd9 0xb4 0xa2 0x06 0x66 0x0f 0x86 0x66 0x46 0x07 0xa2 0x02 0xda 0xb4
 0x6b 0x10 0xa2 0x3a 0xd8 0xb4 0xa2 0x1e 0xd9 0xb4 0xa3 0xe8 0x60 0x00 0x61 0x30
 0xf1 0x55 0xa3 0xe9 0xf0 0x65 0xa2 0x06 0x40 0x30 0xa2 0x02 0xda 0xb4 0x6b 0x15
 0xa2 0x3a 0xd8 0xb4 0xa2 0x16 0xd9 0xb4 0xa3 0xe8 0x66 0x89 0xf6 0x33 0xf2 0x65
 0xa2 0x02 0x30 0x01 0xa2 0x06 0x31 0x03 0xa2 0x06 0x32 0x07 0xa2 0x06 0xda 0xb4
 0x6b 0x1a 0xa2 0x0e 0xd8 0xb4 0xa2 0x3e 0xd9 0xb4 0x12 0x48 0x13 0xdc])]
    (let [status (core/load-rom "test-opcodes.ch8")]
      (testing "0x3 will jump an instruction since v6 is equal to 0x2B"
        (let [result (run-instructions status 13)]
          (is (= (:pc (:registers result))
                 0x268))))
      (testing "0x4 will not jump an instruction since v5 is equal to 0x2A"
        (let [result (run-instructions status 21)]
          (is (= (:pc (:registers result))
                 0x278))))
      (testing "0x5 will not jump since registers v5 and v6 are not equal"
        (let [result (run-instructions status 30)]
          (is (= (:pc (:registers result))
                 0x28A))))
      (testing "0x9 will not jump since the registers v5 and v6 are equal"
        (let [result (run-instructions status 49)
              registers (:registers result)]
          (is (= (:pc registers)
                 0x2B0))))
      (testing "instruction 0x2 jumps to subroutine in address 0x242"
        (let [result (run-instructions status 57)]
          (is (= (:stack result)
                 [0x2C0]))
          (is (= (:pc (:registers result))
                 0x242))))
      (testing "Instruction 0x00EE returns from subroutine"
        (let [result (run-instructions status 60)]
          (is (= (:pc (:registers result))
                 0x2C0))
          (is (empty? (:stack result)))))
      (testing "instruction 0x8750 sets the value of v7 in the v5 register"
        (let [result (run-instructions status 77)
              registers (:register result)]
          (is (= (:v5 registers)
                 (:v7 registers)))))
      (testing "instruction 0x87B1 sets v7 to the bitwise or of v7 and VB leaving vB unaffected"
        (let [result (run-instructions status 88)
              registers (:registers result)]
          (is (= (:vB registers) 0xB))
          (is (= (:v7 registers) 0x2B))))
      (testing "instruction 0x8762 sets v7 to the bitwise and of v7 and v6 leaving v6 unaffected"
        (let [result (run-instructions status 100)
              registers (:registers result)]
          (is (= (:v6 registers) 0x78))
          (is (= (:v7 registers) 0x18))))
      (testing "instruction 0x8763 sets v7 to the bitwise xor of v7 and v6 leaving v6 unaffected"
        (let [result (run-instructions status 112)
              registers (:registers result)]
          (is (= (:v7 registers) 0x67))))
      (testing "instruction 0x8764 sets v7 to the addition of v7 and v6 with carry in the vF register leaving v6 unaffected"
        (let [result (run-instructions status 124)
              registers (:registers result)]
          (is (= (:v7 registers) 0x18))
          (is (= (:v6 registers) 0x8C))
          (is (= (:vF registers) 0x1))))
      (testing "instruction 0x8765 sets v7 to v7 - v6 with carry"
        (let [result (run-instructions status 139)
              registers (:registers result)]
          (is (= (:vF registers) 0x0))
          (is (= (:v7 registers) 0xEC)))))))

