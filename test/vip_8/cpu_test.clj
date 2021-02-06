(ns vip-8.cpu-test
  (:require [clojure.test :refer :all]
            [vip-8.cpu :refer :all]
            [vip-8.screen :as screen]))

(deftest read-instruction-test
  (testing "instructions can be decoded"
    (is (= (read-instruction {:memory [0x00 0xe0]
                              :registers {:pc 0x000}})
           {:instruction 0x00e0
            :x 0x0
            :y 0x0
            :n 0x0
            :nn 0x0
            :nnn 0x0}))
    (is (= (read-instruction {:memory [0x12 0x24]
                              :registers {:pc 0x000}})
           {:instruction 0x1
            :x 0x0
            :y 0x0
            :n 0x0
            :nn 0x0
            :nnn 0x224}))
    (is (= (read-instruction {:memory [0x1F 0x8A]
                              :registers {:pc 0x000}})
           {:instruction 0x1
            :x 0x0
            :y 0x0
            :n 0x0
            :nn 0x0
            :nnn 0xF8A}))
    (is (= (read-instruction {:memory [0x1F 0x8A 0x00 0xEE]
                              :registers {:pc 0x002}})
           {:instruction 0x00EE
            :x 0x0
            :y 0x0
            :n 0x0
            :nn 0x0
            :nnn 0x000}))
    (is (= (read-instruction {:memory [0x1F 0x8A 0x23 0x9A]
                              :registers {:pc 0x002}})
           {:instruction 0x2
            :x 0x0
            :y 0x0
            :n 0x0
            :nn 0x0
            :nnn 0x39A}))
    (is (= (read-instruction {:memory [0x1F 0x8A 0xA1 0x0B]
                              :registers {:pc 0x002}})
           {:instruction 0xA
            :x 0x0
            :y 0x0
            :n 0x0
            :nn 0x0
            :nnn 0x10B}))
    (is (= (read-instruction {:memory [0x31 0xAB]
                              :registers {:pc 0x0}})
           {:instruction 0x3
            :x 0x1
            :y 0x0
            :n 0x0
            :nn 0xAB
            :nnn 0x0}))
    (is (= (read-instruction {:memory [0x4f 0x96]
                              :registers {:pc 0x0}})
           {:instruction 0x4
            :x 0xF
            :y 0x0
            :n 0x0
            :nn 0x96
            :nnn 0x0}))
    (is (= (read-instruction {:memory [0x1F 0x8A 0xA1 0x0B 0x60 0x45]
                              :registers {:pc 0x004}})
           {:instruction 0x6
            :x 0x0
            :y 0x0
            :n 0x0
            :nn 0x45
            :nnn 0x000}))
    (is (= (read-instruction {:memory [0x63 0xCD]
                              :registers {:pc 0x000}})
           {:instruction 0x6
            :x 0x3
            :y 0x0
            :n 0x0
            :nn 0xCD
            :nnn 0x000}))
    (is (= (read-instruction {:memory [0xd0 0x12]
                              :registers {:pc 0x000}})
           {:instruction 0xd
            :x 0x0
            :y 0x1
            :n 0x2
            :nn 0x0
            :nnn 0x0}))
    (is (= (read-instruction {:memory [0xd3 0xAF]
                              :registers {:pc 0x000}})
           {:instruction 0xd
            :x 0x3
            :y 0xA
            :n 0xF
            :nn 0x0
            :nnn 0x0}))
    (is (= (read-instruction {:memory [0x71 0x00]
                              :registers {:pc 0x000}})
           {:instruction 0x7
            :x 1
            :y 0x0
            :n 0x0
            :nn 0x00
            :nnn 0x000}))
(is (= (read-instruction {:memory [0x7A 0x9D]
                          :registers {:pc 0x000}})
       {:instruction 0x7
        :x 0xA
        :y 0x0
        :n 0x0
        :nn 0x9D
        :nnn 0x000}))
(is (= (read-instruction {:memory [0x51 0xA0]
                          :registers {:pc 0x0}})
       {:instruction 0x5
        :x 0x1
        :y 0xA
        :n 0
        :nn 0
        :nnn 0}))
(is (= (read-instruction {:memory [0x9B 0xF0]
                          :registers {:pc 0x0}})
       {:instruction 0x9
        :x 0xB
        :y 0xF
        :n 0
        :nn 0
        :nnn 0}))
(is (= (read-instruction {:memory [0x8A 0xB3]
                          :registers {:pc 0x0}})
       {:instruction 0x8
        :x 0xA
        :y 0xB
        :n 0x3
        :nn 0x0
        :nnn 0x0}))))

(deftest execute-test
  (testing "instruction 0x00e0 clears the screen"
    (let [ cleared? (atom false)]
      (with-redefs [screen/clear 
                    (fn [] 
                      (swap! cleared? (fn [_] true)))]
        (execute {:instruction 0x00e0} {})
        (is @cleared?))))
  (testing "instruction 0xA sets the index registry"
    (let [ cleared? (atom false)]
      (with-redefs [screen/clear 
                    (fn [] 
                      (swap! cleared? (fn [_] true)))]
        (let [result 
              (execute {:instruction 0xA
                        :nnn 0x001} 
                       {:memory [0x1A 0xBA 0x91] 
                        :registers {:index 0x0}})]
          (is (not  @cleared?))
          (is (= (:index (:registers result))
                 0x001)))
        (let [result 
              (execute {:instruction 0xA
                        :nnn 0xABC} 
                       {:memory [0x1C 0xBA] 
                        :registers {:index 0x0}})]
          (is (not  @cleared?))
          (is (= (:index (:registers result))
                 0xABC))))))
  (testing "instruction 0x6 sets given register to given value"
    (let [status {:registers {:v0 0x0
                              :v1 0x0
                              :v2 0x0
                              :v3 0x0
                              :v4 0x0
                              :v5 0x0
                              :v6 0x0
                              :v7 0x0
                              :v8 0x0
                              :v9 0x0
                              :vA 0x0
                              :vB 0x0
                              :vC 0x0
                              :vD 0x45
                              :vE 0x69
                              :vF 0x0}}]
      (is (= (:v3 (:registers (execute {:instruction 0x6
                                        :x 0x3
                                        :y 0x0
                                        :n 0x0
                                        :nn 0x15
                                        :nnn 0x0}
                                       status)))
             0x15))
      (is (= (:v3 (:registers (execute {:instruction 0x6
                                        :x 0x3
                                        :y 0x0
                                        :n 0x0
                                        :nn 0xBA
                                        :nnn 0x0}
                                       status)))
             0xBA))
      (let [actual (execute {:instruction 0x6
                             :x 0xD
                             :y 0x0
                             :n 0x0
                             :nn 0xFF
                             :nnn 0x0}
                            status)
            registers (:registers actual)]
        (is (= (:v3 registers) 0x0))
        (is (= (:vD registers) 0xFF)))))
  (testing "instruction 0x7 adds nn to whatever is in vX and sets it to vX"
    (let [actual (execute {:instruction 0x7
                           :x 0x2
                           :nn 0x20}
                          {:registers {:v2 0xFA
                                       :v7 0x10}})]
      (is (= (:v7 (:registers actual))
             0x10))
      (is (= (:v2 (:registers actual))
             0x1a)))
    (let [actual (execute {:instruction 0x7
                           :x 0x7
                           :nn 0x03}
                          {:registers {:v2 0xFA
                                       :v7 0x02}})
          registers (:registers actual)]
      (is (= (:v2 registers) 0xFA))
      (is (= (:v7 registers) 0x05))))
  (testing "instruction 0xD draws a N-pixel tall sprite at coordinate given by vX and vY"
    (let [screen (atom #{})]
      (with-redefs [screen/is-on? 
                    (fn [x y] 
                      (contains? @screen (list x y)))
                    screen/set 
                    (fn [x y on?]
                      (swap! screen
                             (fn [v]
                               (if on?
                                 (into #{} (cons (list x y) v))
                                 (into #{} (filter #(not= % (list x y)) v))))))
                    screen/width (fn [] 64)
                    screen/height (fn [] 32)]
        (let [result
              (execute {:instruction 0xD
                        :x 0xF
                        :y 0x3
                        :n 0x1}
                       {:memory [0xFF]
                        :registers {:v3 0x0
                                    :vF 0x0
                                    :index 0x0
                                    :pc 0x0}})]
          (loop [x 0x0]
            (when (< x 0x8)
              (is (contains? @screen (list x 0x0)))
              (recur (inc x))))
          (is (= (count @screen) 8))
          (is (= (:vF (:registers result))
                 0x0)))
        (let [result
              (execute {:instruction 0xD
                        :x 0x2
                        :y 0xB
                        :n 0x1}
                       {:memory [0xFF]
                        :registers {:v2 0xA
                                    :vB 0xC
                                    :vF 0x0
                                    :index 0x0
                                    :pc 0x0}})
              current-screen @screen]
          (loop [x 0xA]
            (when (< x (+ 0xA 0x8))
              (is (contains? current-screen (list x 0xC)))
              (recur (inc x))))
          (is (= (count current-screen) 16))
          (is (= (:vF (:registers result))
                 0x0)))
        (let [result
              (execute {:instruction 0xD
                        :x 0x5
                        :y 0xA
                        :n 0x8}
                       {:memory [0xFF 0xF8 0xF8 0xF8 0xF8 0xF8 0xF8 0xF8 0xF8 0xF8]
                        :registers {:v5 0x7
                                    :vA 0x0
                                    :vF 0x0
                                    :index 0x1
                                    :pc 0x0}})]
          (is (not (contains? @screen (list 0x7 0x0))))
          (is (contains? @screen (list 0x8 0x0)))
          (is (contains? @screen (list 0x9 0x0)))
          (is (contains? @screen (list 0xA 0x0)))
          (is (contains? @screen (list 0xB 0x0)))
          (is (not (contains? @screen (list 0xC 0x0))))
          (is (= (:vF (:registers result))
                 0x1)))
        (swap! screen (fn [_] #{}))
        (let [result 
              (execute {:instruction 0xD
                        :x 0x7
                        :y 0xD
                        :n 0x3}
                       {:memory [2r10000000
                                 2r01000000
                                 2r00100001]
                        :registers {:v7 0x3
                                    :vD 0x5
                                    :index 0x0}})
              result-screen @screen]
          (is (= (:vF (:registers result)) 0))
          (is (= (count result-screen) 4))
          (is (contains? result-screen (list 3 5)))
          (is (contains? result-screen (list 4 6)))
          (is (contains? result-screen (list 5 7)))
          (is (contains? result-screen (list 10 7))))
        (swap! screen (fn [_] #{}))
        (let [result 
              (execute {:instruction 0xD
                        :x 0x0
                        :y 0x1
                        :n 0xF}
                       {:memory [0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF]
                        :registers {:v0 0x7c
                                    :v1 0x3e
                                    :vF 0x1
                                    :index 0x0
                                    :pc 0x0}})]
          (is (= (:vF (:registers result)) 0x0))
          (is (contains? @screen (list 0x3c 0x1e)))
          (is (= 0
                 (count
                 (filter (fn [[x y]]
                           (or (not (<= 0 x 63))
                               (not (<= 0 y 31))))
                         @screen))))))))
(testing "Instruction 1 sets the program counter to the given address"
  (let [result (execute {:instruction 0x1
                         :nnn 0xABC}
                        {:registers {:pc 0x0}})]
    (is (= (:pc (:registers result))
           0xABC)))
  (let [result (execute {:instruction 0x1
                         :nnn 0xF18}
                        {:registers {:pc 0x0}})]
    (is (= (:pc (:registers result))
           0xF18))))
(testing "Instruction 0x3 jumps an instruction if the given register stores the value in the instruction"
  (let [result (execute {:instruction 0x3
                         :x 0x3
                         :nn 0x1A}
                        {:registers {:pc 0x2
                                     :v3 0x90}})]
    (is (= (:pc (:registers result))
           0x2)))
  (let [result (execute {:instruction 0x3
                         :x 0x4
                         :nn 0x3B}
                        {:registers {:pc 0x2
                                     :v4 0x3B
                                     :v3 0x90}})]
    (is (= (:pc (:registers result))
           0x4))))
(testing "Instruction 0x4 jumps an instruction if the given register does not store the value in the instruction"
  (let [result (execute {:instruction 0x4
                         :x 0xA
                         :nn 0x99}
                        {:registers {:pc 0x200
                                     :vA 0x99}})]
    (is (= (:pc (:registers result))
           0x200)))
  (let [result (execute {:instruction 0x4
                         :x 0x2
                         :nn 0x12}
                        {:registers {:pc 0x200
                                     :v2 0x20
                                     :vA 0x99}})]
    (is (= (:pc (:registers result))
           0x202))))
(testing "Instruction 0x5 jumps an instruction if register x and y have the same value"
  (let [result (execute {:instruction 0x5
                         :x 0xA
                         :y 0x8}
                        {:registers {:pc 0x202
                                    :vA 0x20
                                    :v8 0x20}})]
    (is (= (:pc (:registers result))
           0x204)))
  (let [result (execute {:instruction 0x5
                         :x 0x2
                         :y 0x5}
                        {:registers {:pc 0x202
                                     :v2 0x1
                                     :v5 0xF
                                     :vA 0x20
                                     :v8 0x20}})]
    (is (= (:pc (:registers result))
           0x202))))
(testing "Instruction 0x9 jumps an instruction if the given registers don't contain the same value"
  (let [result (execute {:instruction 0x9
                         :x 0x4
                         :y 0x7}
                        {:registers {:v4 0x1
                                     :v7 0x3
                                     :pc 0x220}})]
    (is (= (:pc (:registers result))
           0x222)))
  (let [result (execute {:instruction 0x9
                         :x 3
                         :y 0xF}
                        {:registers {:v3 0x10
                                     :vF 0x10
                                     :pc 0x230}})]
    (is (= (:pc (:registers result))
           0x230))))
(testing "Instruction 0x2 jumps to subroutine"
  (let [result (execute {:instruction 0x2
                         :nnn 0x218}
                        {:registers {:pc 0x508}
                         :stack [0x406]})]
    (is (= (:stack result)
           [0x508 0x406]))
    (is (= (:pc (:registers result))
           0x218))))
(testing "Instruction 0x00EE returns from a subroutine"
  (let [result (execute {:instruction 0x00EE}
                        {:registers {:pc 0x670}
                         :stack [0x340 0x407]})]
    (is (= (:pc (:registers result))
           0x340))
    (is (= (:stack result)
           [0x407]))))
(testing "instruction 0x8XY0 sets vX to the value of vY"
  (let [result (execute {:instruction 0x8
                         :x 0x1
                         :y 0x2
                         :n 0}
                        {:registers {:v1 0x3
                                     :v2 0x5}})
        registers (:registers result)]
    (is (= (:v1 registers) 0x5))))
(testing "instruction 0x8XY1 sets vX to the bitwise or of vX and vY leaving vY unaffected"
  (let [result (execute {:instruction 0x8
                         :x 0xC
                         :y 0xD
                         :n 0x1}
                        {:registers {:vC 0x0
                                    :vD 0x0}})
        registers (:registers result)]
    (is (= (:vC registers) 0x0))
    (is (= (:vD registers) 0x0)))
  (let [result (execute {:instruction 0x8
                         :x 0x1
                         :y 0x3
                         :n 0x1}
                        {:registers {:v1 0x0
                                    :v3 0x1}})
        registers (:registers result)]
    (is (= (:v1 registers) 0x1))
    (is (= (:v3 registers) 0x1))))
(testing "instruction 0x8XY2 sets vX to the bitwise and of vX and vY leaving vY unaffected"
  (let [result (execute {:instruction 0x8
                         :x 0xA
                         :y 0xB
                         :n 0x2}
                        {:registers {:vA 0x0
                                     :vB 0x0}})
        registers (:registers result)]
    (is (= (:vB registers) 0x0))
    (is (= (:vA registers) 0x0)))
  (let [result (execute {:instruction 0x8
                         :x 0x3
                         :y 0x5
                         :n 0x2}
                        {:registers {:v3 0x0
                                     :v5 0x1}})
        registers (:registers result)]
    (is (= (:v3 registers) 0x0))
    (is (= (:v5 registers) 0x1)))
  (let [result (execute {:instruction 0x8
                         :x 0x7
                         :y 0x2
                         :n 0x2}
                        {:registers {:v7 0x1
                                     :v2 0x0}})
        registers (:registers result)]
    (is (= (:v7 registers) 0x0))
    (is (= (:v2 registers) 0x0))))
(testing "instruction 0x8XY3 stores in vX the bitwise xor of vX and vY leaving vY unaffected"
  (let [result (execute {:instruction 0x8
                         :x 0xA
                         :y 0xB
                         :n 0x3}
                        {:registers {:vA 0x0
                                     :vB 0x0}})
        registers (:registers result)]
    (is (= (:vA registers) 0x0))
    (is (= (:vB registers) 0x0)))
  (let [result (execute {:instruction 0x8
                         :x 0x3
                         :y 0x8
                         :n 0x3}
                        {:registers {:v3 0x1
                                     :v8 0x1}})
        registers (:registers result)]
    (is (= (:v3 registers) 0x0))
    (is (= (:v8 registers) 0x1))))
(testing "instruction 0x8XY4 adds with carry"
  (let [result (execute {:instruction 0x8
                         :x 0x3
                         :y 0x5
                         :n 4}
                        {:registers {:v3 0x20
                                     :v5 0x30
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:v3 registers) 0x50))
    (is (= (:v5 registers) 0x30))
    (is (= (:vF registers) 0x0)))
  (let [result (execute {:instruction 0x8
                         :x 0xA
                         :y 0xB
                         :n 4}
                        {:registers {:vA 0x30
                                     :vB 0x40
                                     :vF 0x1}})
        registers (:registers result)]
    (is (= (:vA registers) 0x70))
    (is (= (:vB registers) 0x40))
    (is (= (:vF registers) 0x0)))
  (let [result (execute {:instruction 0x8
                         :x 0x2
                         :y 0x6
                         :n 4}
                        {:registers {:v2 0xFF
                                     :v6 0x01
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:v2 registers) 0x00))
    (is (= (:v6 registers) 0x01))
    (is (= (:vF registers) 0x1))))
(testing "instruction 0x8XY5 substracts vX - vY and puts the result in vX with carry"
  (let [result (execute {:instruction 0x8
                         :x 0x3
                         :y 0xA
                         :n 5}
                        {:registers {:v3 0x10
                                     :vA 0xE
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:vF registers) 0x1))
    (is (= (:v3 registers) 0x2)))
  (let [result (execute {:instruction 0x8
                         :x 0xA
                         :y 0xB
                         :n 5}
                        {:registers {:vA 0xE
                                     :vB 0xF
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:vF registers) 0x0))
    (is (= (:vA registers) 0xFF))))
(testing "instruction 0x8XYE shift bits to the left"
  (let [result (execute {:instruction 0x8
                         :x 0x3
                         :y 0x5
                         :n 0xE}
                        {:registers {:v3 0x1
                                     :v5 0x10
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:v3 registers) 0x2))
    (is (= (:vF registers) 0x1)))
  (let [result (execute {:instruction 0x8
                         :x 0x7
                         :y 0x2
                         :n 0xE}
                        {:registers {:v7 0x10
                                     :v2 0xFF
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:vF registers) 0x0))
    (is (= (:v7 registers) 0x20)))
  (let [result (execute {:instruction 0x8
                         :x 0xA
                         :y 0x1
                         :n 0xE}
                        {:registers {:v1 0x10
                                     :vA 0xFF
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:vF registers) 0x1))
    (is (= (:vA registers) 0xFE))))
(testing "instruction 0x8XY6 shift bits to the right"
  (let [result (execute {:instruction 0x8
                         :x 0x1
                         :y 0x2
                         :n 0x6}
                        {:registers {:v1 0x1
                                     :v2 0xFF
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:v1 registers) 0x0))
    (is (= (:vF registers) 0x1)))
  (let [result (execute {:instruction 0x8
                         :x 0xB
                         :y 0x7
                         :n 0x6}
                        {:registers {:v1 0x10
                                     :vB 0x2
                                     :v7 0x1
                                     :vF 0x0}})
        registers (:registers result)]
    (is (= (:v1 registers) 0x10))
    (is (= (:vB registers) 0x1))
    (is (= (:vF registers) 0x0)))))
