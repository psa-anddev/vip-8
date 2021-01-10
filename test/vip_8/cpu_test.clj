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
                 0xBA91)))
        (let [result 
              (execute {:instruction 0xA
                        :nnn 0x000} 
                       {:memory [0x1C 0xBA] 
                        :registers {:index 0x0}})]
          (is (not  @cleared?))
          (is (= (:index (:registers result))
                 0x1CBA))))))
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
        (is (= (:vD registers) 0xFF))))))
