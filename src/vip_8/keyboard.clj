(ns vip-8.keyboard
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.input KeyCode KeyEvent]))

(def ^:private released-keys (atom '()))
(def ^:private pressed-keys (atom #{}))

(defn get-pressed 
  "Returns the last key pressed by the user"
  []
  (let [key (first @released-keys)]
    (swap! released-keys rest)
    key))

(defn key-pressed? [key]
  (contains? @pressed-keys key))

(defn to-keypad [keycode]
  (let [conversion-map {KeyCode/DIGIT1 0x1
                        KeyCode/DIGIT2 0x2
                        KeyCode/DIGIT3 0x3
                        KeyCode/DIGIT4 0xC
                        KeyCode/Q 0x4
                        KeyCode/W 0x5
                        KeyCode/E 0x6
                        KeyCode/R 0xD
                        KeyCode/A 0x7
                        KeyCode/S 0x8
                        KeyCode/D 0x9
                        KeyCode/F 0xE
                        KeyCode/Z 0xA
                        KeyCode/X 0x0
                        KeyCode/C 0xB
                        KeyCode/V 0xF}]
    (conversion-map keycode)))

(defmulti handle-keyboard-event :event/type)

(defmethod handle-keyboard-event :default [event]
  (prn event))

(defmethod handle-keyboard-event ::key_pressed [{:keys [fx/context fx/event]}]
  (let [new-key (to-keypad (.getCode event))]
    (swap! pressed-keys #(into #{} (cons new-key %)))))

(defmethod handle-keyboard-event ::key_released [{:keys [fx/context fx/event]}]
  (let [new-key (to-keypad (.getCode event))]
    (swap! pressed-keys #(into #{} (remove (fn [v] (= v new-key)) %)))
    (swap! released-keys #(cons new-key %))))
