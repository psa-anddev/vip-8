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
          :nnn 0x0})))

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
  (testing "instruction 0xD draws a N-pixel tall sprite at coordinate given by vX and vY"
    (let [screen (atom #{})]
      (with-redefs [screen/is-on? (fn [x y] (not (empty? (filter (fn [[ax ay]] 
                                                                   (and (= x ax)
                                                                        (= y ay)))
                                                                 @screen))))
                    screen/set (fn [x y on?]
                                 (swap! screen
                                        (fn [v]
                                          (if on?
                                            (into #{} (cons (list x y) v))
                                            (into #{} (filter #(not= % '(x y)) v))))))]
        (let [result
              (execute {:instruction 0xD
                        :x 0xF
                        :y 0x3
                        :n 0x8}
                       {:memory [0xFF]
                        :registers {:v3 0x0
                                    :vf 0x0
                                    :index 0x0
                                    :pc 0x0}})]
          (is (= #{'(0x0 0x0)
                   '(0x1 0x0)
                   '(0x2 0x0)
                   '(0x3 0x0)
                   '(0x4 0x0)
                   '(0x5 0x0)
                   '(0x6 0x0)
                   '(0x7 0x0)
                   '(0x0 0x1)
                   '(0x1 0x1)
                   '(0x2 0x1)
                   '(0x3 0x1)
                   '(0x4 0x1)
                   '(0x5 0x1)
                   '(0x6 0x1)
                   '(0x7 0x1)
                   '(0x0 0x2)
                   '(0x1 0x2)
                   '(0x2 0x2)
                   '(0x3 0x2)
                   '(0x4 0x2)
                   '(0x5 0x2)
                   '(0x6 0x2)
                   '(0x7 0x2)
                   '(0x0 0x3)
                   '(0x1 0x3)
                   '(0x2 0x3)
                   '(0x3 0x3)
                   '(0x4 0x3)
                   '(0x5 0x3)
                   '(0x6 0x3)
                   '(0x7 0x3)
                   '(0x0 0x4)
                   '(0x1 0x4)
                   '(0x2 0x4)
                   '(0x3 0x4)
                   '(0x4 0x4)
                   '(0x5 0x4)
                   '(0x6 0x4)
                   '(0x7 0x4)
                   '(0x0 0x5)
                   '(0x1 0x5)
                   '(0x2 0x5)
                   '(0x3 0x5)
                   '(0x4 0x5)
                   '(0x5 0x5)
                   '(0x6 0x5)
                   '(0x7 0x5)
                   '(0x0 0x6)
                   '(0x1 0x6)
                   '(0x2 0x6)
                   '(0x3 0x6)
                   '(0x4 0x6)
                   '(0x5 0x6)
                   '(0x6 0x6)
                   '(0x7 0x6)
                   '(0x0 0x7)
                   '(0x1 0x7)
                   '(0x2 0x7)
                   '(0x3 0x7)
                   '(0x4 0x7)
                   '(0x5 0x7)
                   '(0x6 0x7)
                   '(0x7 0x7)}
                 @screen)))))))
