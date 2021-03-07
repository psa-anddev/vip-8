(ns vip-8.screen-test
  (:require [clojure.test :refer [deftest testing is]]
            [vip-8.screen :refer :all]))

(deftest clear-tests
  (testing "clears the screen"
    (swap! ui-state
           #(assoc % 
                   :active-pixels 
                   #{(list 0 0)
                     (list 1 0)
                     (list 17 50)}))
    (clear)
    (is (= (:active-pixels @ui-state) #{}))))

(deftest is-on?-test 
  (swap! ui-state
         #(assoc % :active-pixels #{(list 15 8)
                                    (list 20 7)
                                    (list 5 22)}))
  (testing "returns true if the pixel is lightened"
    (is (is-on? 15 8))
    (is (is-on? 20 7))
    (is (is-on? 5 22)))
  (testing "returns false if the pixel is not lightened"
    (is (not (is-on? 0 0)))
    (is (not (is-on? 15 2)))
    (is (not (is-on? 15 7)))))

(deftest set-test
  (swap! ui-state #(assoc % :active-pixels #{}))
  (testing "ligtened pixels are added to the set"
    (set 15 8 true)
    (set 20 7 true)
    (set 5 22 true)
    (set 30 20 true)
    (is (contains? (:active-pixels  @ui-state) 
                   (list 15 8)))
    (is (contains? (:active-pixels @ui-state)
                   (list 20 7))))
  (testing "not lightened pixels are removed from the set"
    (set 5 22 false)
    (set 30 20 false)
    (is (not (contains? (:active-pixels @ui-state)
                        (list 5 22))))))

(deftest boundaries-test
  (testing "screen dimensions are the ones in the Chip 8"
    (is (= (width) 64))
    (is (= (height) 32))))

(deftest title-test
  (testing "returns the current title"
    (is (= (title) "Vip 8"))
    (swap! ui-state #(assoc % :title "New Title"))
    (is (= (title) "New Title")))
  (testing "sets a new title"
    (title "Test 1")
    (is (= (:title @ui-state) "Test 1"))
    (title "Some other title")
    (is (= (:title @ui-state) "Some other title"))))

(deftest modline-test
  (testing "returns the value for the modline"
    (is (= (modline) "Pause | <No ROM>"))
    (swap! ui-state #(assoc % :modline "Some modline"))
    (is (= (modline) "Some modline")))
  (testing "modifies the value for the modline"
    (modline "No modline")
    (is (= (:modline @ui-state) "No modline"))
    (modline "Some other text for the modline")
    (is (= (:modline @ui-state) "Some other text for the modline"))))
