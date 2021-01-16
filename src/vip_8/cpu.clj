(ns vip-8.cpu
  (:require [vip-8.screen :as screen]
            [clojure.string :as string]))

(defn read-instruction 
  "Reads the instruction at the program counter
  and returns the nibles"
  [status]
  (let [pc (:pc (:registers status))
        memory (:memory status)
        first-byte (nth memory pc)
        second-byte (nth memory (inc pc))
        i (bit-or (bit-shift-left first-byte 0x8)
                  second-byte)
        opcode (bit-shift-right (bit-and i 0xF000) 0xC)]
    (cond
      (= opcode 0x0)
      {:instruction i
       :x 0
       :y 0
       :n 0
       :nn 0
       :nnn 0}
      (= opcode 0x6)
      {:instruction opcode
       :x (bit-and first-byte 0xF)
       :y 0
       :n 0
       :nn second-byte
       :nnn 0x0}
      (or (= opcode 0xA)
          (= opcode 0x2)
          (= opcode 0x1))
      {:instruction opcode
       :x 0
       :y 0
       :n 0
       :nn 0
       :nnn (bit-and i 0xFFF)}
      (= opcode 0xD)
      {:instruction opcode
       :x (bit-shift-right (bit-and i 0xF00) 0x8)
       :y (bit-shift-right (bit-and i 0xF0) 0x4)
       :n (bit-and i 0xF)
       :nn 0x0
       :nnn 0x0}
      :else
      (throw (Exception. (str  "Instruction " (format "0x%x" opcode) " not implemented"))))))

(defn execute 
  "Executes a given instruction"
  [instruction state]
  (defn clear-screen []
    (screen/clear)
    state)

  (defn set-register [status register value]
    (assoc status
           :registers
           (assoc (:registers status)
                  register
                  value)))

  (defn set-v-register []
    (set-register state
                  (keyword (str "v" (string/upper-case (format "%x" (:x instruction)))))
                  (:nn instruction)))

  (defn set-index-register []
    (let [new-value (:nnn instruction)]
      (set-register state
                    :index
                    new-value)))

  (defn draw-sprite []
    (let [height (:n instruction)]
      (loop [x 0
             y 0]
        (if (< y height)
          (do (screen/set x y true)
              (recur (if (< x 7) (inc x) 0)
                     (if (= x 7) (inc y) y)))
          state))))

  (let [instructions {:e0 clear-screen
                      :6 set-v-register
                      :a set-index-register
                      :d draw-sprite}
        op-keyword (keyword (format "%x" (:instruction instruction)))]
    (if (nil? op-keyword)
      (throw "Instruction ")
      ((op-keyword instructions)))))

