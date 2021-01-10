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
      :else
      {:instruction opcode
       :x 0
       :y 0
       :n 0
       :nn 0
       :nnn (bit-and i 0xFFF)})))

(defn execute 
  "Executes a given instruction"
  [instruction state]
  (let [instructions {:e0 (fn [] 
                              (screen/clear)
                              state)
                      :6 (fn []
                           (assoc state
                                  :registers
                                  (assoc (:registers state)
                                         (keyword (str "v" (string/upper-case (format "%x" (:x instruction)))))
                                         (:nn instruction))))
                      :a (fn []
                           (let [address (:nnn instruction)
                                 memory (:memory state)
                                 first-byte (nth memory address)
                                 second-byte (nth memory (inc address))
                                 new-value (bit-or (bit-shift-left first-byte 0x8)
                                                   second-byte)]
                             (assoc state 
                                    :registers 
                                    (assoc (:registers state)
                                           :index
                                           new-value))))}
        op-keyword (keyword (format "%x" (:instruction instruction)))]
  (if (nil? op-keyword)
    (throw "Instruction ")
    ((op-keyword instructions)))))

