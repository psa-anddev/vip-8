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
            :nnn 0x10B})))
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
          :nnn 0x000})))

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
                       {:memory [0xFF 0xF8]
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
                        :x 0x0
                        :y 0x1
                        :n 0xF}
                       {:memory [0xFF]
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
           0xF18)))))
