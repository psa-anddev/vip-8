(ns vip-8.events-test
  (:require [clojure.test :refer :all]
            [vip-8.events :refer :all]))

(deftest mode-test
  (mode '(:pause)) ;; Make sure test don't affect each other
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

(deftest cancel-test
  (testing "if it was paused it comes back to paused"
    (mode '(:pause))
    (mode '(:command ":"))
    
    (cancel)
    
    (is (= (mode) '(:pause))))
  (testing "if it was running, it comes back to running"
    (mode '(:run "chipquarium.ch8"))
    (mode '(:command ":"))
    
    (cancel)
    
    (is (= (mode) '(:run "chipquarium.ch8"))))
  (testing "cancel always leaves command mode"
    (mode '(:pause "tetris.ch8"))
    (mode '(:command ":"))
    (mode '(:command ":l"))
    
    (cancel)
    
    (is (= (mode) '(:pause "tetris.ch8")))))
