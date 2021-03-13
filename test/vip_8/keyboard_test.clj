(ns vip-8.keyboard-test
  (:require [clojure.test :refer [deftest testing is]]
            [cljfx.api :as fx]
            [vip-8.keyboard :refer :all]
            [vip-8.screen :as screen]
            [vip-8.events :as events])
  (:import [javafx.scene.input KeyCode KeyEvent]))

(defn event [event-type key-code]
  {:event/type (keyword "vip-8.keyboard" event-type)
   :fx/event (KeyEvent. (if (= event-type "key_pressed")
                          (KeyEvent/KEY_PRESSED)
                          (KeyEvent/KEY_RELEASED))
                        ""
                        ""
                        key-code
                        false
                        false
                        false
                        false)})

(deftest handle-keyboard-event-tests
  (testing "key pressed events in run mode"
    (events/mode (list :run))

    (testing "pressing 1 gets key 1 pressed"
      (handle-keyboard-event (event "key_pressed" 
                                    (KeyCode/DIGIT1)))
      (is (key-pressed? 0x1)))
    (testing "pressing 2 gets key 2 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/DIGIT2)))
      (is (key-pressed? 0x2)))
    (testing "pressing 3 gets key 3 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/DIGIT3)))
      (is (key-pressed? 0x3)))
    (testing "pressing 4 gets key C pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/DIGIT4)))
      (is (key-pressed? 0xC)))
    (testing "pressing Q gets key 4 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/Q)))
      (is (key-pressed? 0x4)))
    (testing "pressing W gets key 5 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/W)))
      (is (key-pressed? 0x5)))
    (testing "pressing E gets key 6 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/E)))
      (is (key-pressed? 0x6)))
    (testing "pressing R gets key D pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/R)))
      (is (key-pressed? 0xD)))
    (testing "pressing A gets key 7 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/A)))
      (is (key-pressed? 0x7)))
    (testing "pressing S gets key 8 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/S)))
      (is (key-pressed? 0x8)))
    (testing "pressing key D gets key 9 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/D)))
      (is (key-pressed? 0x9)))
    (testing "pressing key F gets key E pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/F)))
      (is (key-pressed? 0xE)))
    (testing "pressing Z gets key A pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/Z)))
      (is (key-pressed? 0xA)))
    (testing "pressing X gets key 0 pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/X)))
      (is (key-pressed? 0x0)))
    (testing "pressing C gets key B pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/C)))
      (is (key-pressed? 0xB)))
    (testing "pressing V gets key F pressed"
      (handle-keyboard-event (event "key_pressed"
                                    (KeyCode/V)))
      (is (key-pressed? 0xF))))
  (testing "key releases in run mode"
    (events/mode (list :run))
    (testing "releasing key 1 gets key 1 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/DIGIT1)))
      (is (= (get-pressed) 0x1)))
    (testing "releasing key 2 gets key 2 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/DIGIT2)))
      (is (= (get-pressed) 0x2)))
    (testing "releasing key 3 gets key 3 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/DIGIT3)))
      (is (= (get-pressed) 0x3)))
    (testing "releasing key 4 gets key C released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/DIGIT4)))
      (is (= (get-pressed) 0xC)))
    (testing "releasing key Q gets key 4 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/Q)))
      (is (= (get-pressed) 0x4)))
    (testing "releasing key W gets key 5 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/W)))
      (is (= (get-pressed) 0x5)))
    (testing "releasing key E gets key 6 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/E)))
      (is (= (get-pressed) 0x6)))
    (testing "releasing key R gets key D released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/R)))
      (is (= (get-pressed) 0xD)))
    (testing "releasing key A gets key 7 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/A)))
      (is (= (get-pressed) 0x7)))
    (testing "releasing key S gets key 8 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/S)))
      (is (= (get-pressed) 0x8)))
    (testing "releasing key D gets key 9 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/D)))
      (is (= (get-pressed) 0x9)))
    (testing "releasing key F gets key E released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/F)))
      (is (= (get-pressed) 0xE)))
    (testing "releasing key Z gets key A released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/Z)))
      (is (= (get-pressed) 0xA)))
    (testing "releasing key X gets key 0 released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/X)))
      (is (= (get-pressed) 0x0)))
    (testing "releasing key C gets key B released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/C)))
      (is (= (get-pressed) 0xB)))
    (testing "releasing key V gets key F released"
      (handle-keyboard-event (event "key_released"
                                    (KeyCode/V)))
      (is (= (get-pressed) 0xF)))))
