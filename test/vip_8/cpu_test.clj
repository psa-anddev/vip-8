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
            :nnn 0x10B}))))

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
              (execute {:instruction 0x1
                        :nnn 0x001} 
                       {:memory [0x1A 0xBA 0x91] 
                        :registers {:index 0x0}})]
          (is (not  @cleared?))
          (is (= (:index (:registers result))
                 0xBA91)))
        (let [result 
              (execute {:instruction 0x1
                        :nnn 0x000} 
                       {:memory [0x1C 0xBA] 
                        :registers {:index 0x0}})]
          (is (not  @cleared?))
          (is (= (:index (:registers result))
                 0x1CBA)))))))
