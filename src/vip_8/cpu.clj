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
          (= opcode 0x4)
          (= opcode 0xF))
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
      (or (= opcode 0xD)
          (= opcode 0x5)
          (= opcode 0x9)
          (= opcode 0x8))
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

  (defn set-memory-address [status address value]
    (assoc status
           :memory
           (assoc (:memory status)
                  address
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

  (defn jump-equal-registers []
    (let [registers (:registers state)
          x-reg-key (get-register-key (:x instruction))
          y-reg-key (get-register-key (:y instruction))
          new-counter (+ (:pc registers) 2)]
      (if (= (registers x-reg-key)
             (registers y-reg-key))
        (set-register state
                      :pc
                      new-counter)
        state)))

  (defn jump-not-equal-registers []
    (let [registers (:registers state)
          x-reg-key (get-register-key (:x instruction))
          y-reg-key (get-register-key (:y instruction))
          new-counter (+ (:pc registers) 2)]
      (if (not= (registers x-reg-key)
             (registers y-reg-key))
        (set-register state
                      :pc
                      new-counter)
        state)))

  (defn jump-to-subroutine []
    (let [stack (:stack state)]
      (set-register 
        (assoc state
               :stack
               (cons (:pc (:registers state)) stack))
        :pc
        (:nnn instruction))))

  (defn return-from-subroutine []
    (let [stack (:stack state)]
      (assoc 
        (set-register state
                      :pc
                      (first stack))
        :stack
        (rest stack))))

  (defn logical-operation []
    (let [operation (:n instruction)
          x-reg-key (get-register-key (:x instruction))
          y-reg-key (get-register-key (:y instruction))
          registers (:registers state)]
      (cond 
        (= operation 0) 
        (set-register state
                      x-reg-key
                      (registers y-reg-key))
        (= operation 1)
        (set-register state
                      x-reg-key
                      (bit-or (registers x-reg-key)
                              (registers y-reg-key)))
        (= operation 2)
        (set-register state
                      x-reg-key
                      (bit-and (registers x-reg-key)
                               (registers y-reg-key)))
        (= operation 3)
        (set-register state
                      x-reg-key
                      (bit-xor (registers x-reg-key)
                               (registers y-reg-key)))
        
        (= operation 4)
        (let [raw-addition (+ (registers x-reg-key)
                              (registers y-reg-key))
              wrapped-addition (bit-and raw-addition
                                        0xFF)
              carry (if (> raw-addition 0xFF) 0x1 0x0)]
        (set-register (set-register state
                                    :vF
                                    carry)
                      x-reg-key
                      wrapped-addition))
        (= operation 5)
        (let [minuend (registers x-reg-key)
              sustrahend (registers y-reg-key)]
          (set-register 
            (set-register state
                          :vF
                          (if (> minuend sustrahend) 0x1 0x0))
            x-reg-key
            (bit-and (- minuend sustrahend) 0xFF)))
        (= operation 0x6)
        (set-register 
          (set-register state x-reg-key 
                        (bit-shift-right 
                          (registers x-reg-key)
                          0x1))
          :vF
          (if (bit-test (registers x-reg-key) 0) 0x1 0x0))
        (= operation 0x7)
        (let [minuend (registers (get-register-key (:y instruction)))
              sustrahend (registers (get-register-key (:x instruction)))
              carry (if (> minuend sustrahend) 0x1 0x0)]
          (set-register
            (set-register state
                          :vF
                          carry)
            (get-register-key (:x instruction))
            (bit-and (- minuend sustrahend) 0xFF)))
        (= operation 0xE)
        (set-register 
          (set-register state
                        x-reg-key
                        (bit-and (bit-shift-left (registers x-reg-key) 0x1) 0xFF))
          :vF
          (if (bit-test (registers x-reg-key) 0) 0x1 0x0))
        :else (throw (Exception. (str "Logical operation " operation " not implemented. (Full instruction: " instruction ")"))))))
  (defn timers-and-memory []
    (let [operation (:nn instruction)]
      (cond
        (= operation 0x55)
        (loop [reg 0
               partial-result state]
          (if (> reg (:x instruction))
            partial-result
            (let [address (+ (:index (:registers partial-result))
                                      reg)
                  value ((:registers partial-result) 
                         (get-register-key reg))]
              (recur (inc reg)
                     (set-memory-address partial-result
                                         address
                                         value)))))
        (= operation 0x65)
        (loop [reg 0
               partial-result state]
          (if (> reg (:x instruction))
            partial-result
            (recur (inc reg)
                   (set-register partial-result
                                 (get-register-key reg)
                                 (nth (:memory partial-result)
                                      (+ (:index (:registers partial-result))
                                         reg))))))
        (= operation 0x33)
        (let [registers (:registers state)
              address (:index registers)
              value (registers
                     (get-register-key (:x instruction)))
              cene (int (/ value 100))
              dece (int (/ (- value (* cene 100)) 10))
              unit (rem value 10)]
          (set-memory-address
            (set-memory-address
              (set-memory-address state
                                  address
                                  cene)
              (inc address)
              dece)
            (+ address 2)
            unit))
        :else
        (throw (Exception. (str "Timer and memory operation " (format "0x%x" operation) " not implemented. (Full instruction: " instruction ")"))))))

  (let [instructions {:e0 clear-screen
                      :ee return-from-subroutine
                      :2 jump-to-subroutine
                      :3 jump-equal
                      :4 jump-not-eual
                      :5 jump-equal-registers
                      :6 set-v-register
                      :7 add-to-v-register
                      :8 logical-operation
                      :9 jump-not-equal-registers
                      :a set-index-register
                      :d draw-sprite
                      :f timers-and-memory
                      :1 set-program-counter}
        op-keyword (keyword (format "%x" (:instruction instruction)))]
    (if (nil? (op-keyword instructions))
      (throw (Exception. (str  "Instruction " (format "%x" (:instruction instruction)) " not implemented. (Full instruction: " instruction ")")))
      ((op-keyword instructions)))))

