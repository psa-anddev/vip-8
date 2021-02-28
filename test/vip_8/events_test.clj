(ns vip-8.events-test
  (:require [clojure.test :refer :all]
            [vip-8.events :refer :all]))

(deftest mode-test
  (testing "default mode is pause"
    (is (= (mode) '(:pause))))
  (testing "set mode to load test ROM"
    (mode '(:load "test.ch8"))
    (is (= (mode) '(:load "test.ch8"))))
  (testing "set mode to load tetris ROM"
    (mode '(:load "tetris.ch8"))
    (is (= (mode) '(:load "tetris.ch8"))))
  (testing "set mode to run"
    (mode '(:run))
    (is (= (mode) '(:run)))))

