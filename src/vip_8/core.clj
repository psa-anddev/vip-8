(ns vip-8.core
  (:require [vip-8.rom :as rom]
            [vip-8.screen :as screen]
            [vip-8.cpu :as cpu]))

(defn load-rom [filename]
  (let [font '(
               0xF0 0x90 0x90 0x90 0xF0 
               0x20 0x60 0x20 0x20 0x70
               0xF0 0x10 0xF0 0x80 0xF0
               0xF0 0x10 0xF0 0x10 0xF0
               0x90 0x90 0xF0 0x10 0x10
               0xF0 0x80 0xF0 0x10 0xF0
               0xF0 0x80 0xF0 0x90 0xF0
               0xF0 0x10 0x20 0x40 0x40
               0xF0 0x90 0xF0 0x90 0xF0
               0xF0 0x90 0xF0 0x10 0xF0
               0xF0 0x90 0xF0 0x90 0x90
               0xE0 0x90 0xE0 0x90 0xE0
               0xF0 0x80 0x80 0x80 0xF0
               0xE0 0x90 0x90 0x90 0xE0
               0xF0 0x80 0xF0 0x80 0xF0
               0xF0 0x80 0xF0 0x80 0x80)
        rom (rom/read-rom filename)]
    {:memory (into [] (concat (repeat 0x050 0x00) 
                              font
                              (repeat (- 0x200 (+ 0x50 (count font))) 
                                      0x00)
                              rom
                              (repeat (- 4096 (+ 0x200 (count rom)))
                                      0x00)))
     :stack []
     :registers {:v0 0x00
                 :v1 0x00
                 :v2 0x00
                 :v3 0x00
                 :v4 0x00
                 :v5 0x00
                 :v6 0x00
                 :v7 0x00
                 :v8 0x00
                 :v9 0x00
                 :vA 0x00
                 :vB 0x00
                 :vC 0x00
                 :vD 0x00
                 :vE 0x00
                 :vF 0x00
                 :pc 0x200
                 :index 0x000}
     :timers {:delay 0
              :sound 0}
     :deltas {:delay (System/currentTimeMillis)
              :sound (System/currentTimeMillis)}}))

(defn now []
  (System/currentTimeMillis))

(defn update-delay-timer [status]
  (let [deltas (:deltas status)
        timers (:timers status)]
    (if (not (nil? deltas))
      (let [update-time (now)]
        (if (> (- update-time (:delay deltas)) 16)
          (let [new-timers (assoc timers 
                                  :delay
                                  (if (zero? (:delay timers))
                                    0
                                    (dec (:delay timers))))]
            (assoc
              (assoc status
                     :timers
                     new-timers)
              :deltas
              (assoc deltas 
                     :delay
                     update-time)))
          status))
      status)))

(defn step
  "Executes an instruction and returns the resulting status"
  [prev-status]
  (let [instruction 
        (cpu/read-instruction prev-status)
        after-reading-status 
    (assoc prev-status 
           :registers 
           (assoc (:registers prev-status)
                  :pc
                  (+ (:pc (:registers prev-status)) 2)))]
    (cpu/execute instruction 
                 (update-delay-timer after-reading-status))))

(defn -main [& args]
  (screen/load-window)
  (loop [status (load-rom "./roms/bc_test.ch8")
         inst-counter 300]
    (when (> inst-counter 0)
      (recur (step status)
             (dec inst-counter)))))
