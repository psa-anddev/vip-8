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
      (or (= opcode 0x6)
          (= opcode 0x7)
          (= opcode 0x3)
          (= opcode 0x4))
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
      (throw (Exception. (str  "Instruction " (format "0x%x" opcode) " not implemented (Full instruction: " (format "%x" i) ")"))))))

(defn execute 
  "Executes a given instruction"
  [instruction state]
  (defn clear-screen []
    (screen/clear)
    state)

  (defn get-register-key [data-byte]
    (keyword (str "v" (string/upper-case (format "%x" data-byte)))))

  (defn set-register [status register value]
    (assoc status
           :registers
           (assoc (:registers status)
                  register
                  value)))

  (defn set-v-register []
    (set-register state
                  (get-register-key (:x instruction))
                  (:nn instruction)))

  (defn set-index-register []
    (let [new-value (:nnn instruction)]
      (set-register state
                    :index
                    new-value)))

  (defn draw-sprite []
    (let [height (:n instruction)
          registers (:registers state)
          start-x (mod (registers (get-register-key (:x instruction)))
                       (screen/width))
          start-y (mod (registers (get-register-key (:y instruction)))
                       (screen/height))]
      (loop [current-bit 7
             current-row 0 
             s (set-register state :vF 0x0)
             byt-addr (:index registers)]
        (if (and (< current-row height)
                 (>= current-bit 0))
          (let [x (+ start-x (- 7 current-bit))
                y (+ start-y current-row)
                sprite-bits (nth (:memory state) byt-addr)
                was-on? (screen/is-on? x y)
                in-range? (and (<= 0 x 63)
                               (<= 0 y 31))
                should-flip? (bit-test sprite-bits current-bit)]
            (when (and in-range? should-flip?)
              (screen/set x y (not was-on?)))
            (recur (if (> current-bit 0) (dec current-bit) 7)
                   (if (= current-bit 0) (inc current-row) current-row)
                   (if (and (= (:vF (:registers s))
                               0x0)
                            in-range?
                            should-flip?
                            was-on?)
                     (set-register s :vF 0x1)
                     s)
                   (if (> current-bit 0) byt-addr (inc byt-addr))))
          s))))
  (defn add-to-v-register []
    (let [reg-keyword (get-register-key (:x instruction)) 
          old-value (reg-keyword (:registers state))
          added-value (:nn instruction)
          set-value (bit-and  (+ old-value added-value)
                             0xFF)]
      (set-register state
                    reg-keyword
                    set-value))) 

  (defn set-program-counter []
    (set-register state
                  :pc
                  (:nnn instruction)))

  (defn jump-equal []
    (let [registers (:registers state)
          new-counter (+ (:pc registers) 2)]
      (if (= (registers (get-register-key (:x instruction)))
             (:nn instruction))
        (set-register state
                      :pc
                      new-counter)
        state)))

  (defn jump-not-eual []
    (let [registers (:registers state)
          new-counter (+ (:pc registers) 2)]
      (if (not= (registers (get-register-key (:x instruction)))
             (:nn instruction))
        (set-register state
                      :pc
                      new-counter)
        state)))

  (let [instructions {:e0 clear-screen
                      :3 jump-equal
                      :4 jump-not-eual
                      :6 set-v-register
                      :7 add-to-v-register
                      :a set-index-register
                      :d draw-sprite
                      :1 set-program-counter}
        op-keyword (keyword (format "%x" (:instruction instruction)))]
    (if (nil? (op-keyword instructions))
      (throw (Exception. (str  "Instruction " (format "%x" (:instruction instruction)) " not implemented. (Full instruction: " instruction ")")))
      ((op-keyword instructions)))))

