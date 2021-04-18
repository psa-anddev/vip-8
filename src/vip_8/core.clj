(ns vip-8.core
  (:require [vip-8.rom :as rom]
            [vip-8.screen :as screen]
            [vip-8.cpu :as cpu]
            [vip-8.sound :as sound]
            [vip-8.events :as events]
            [clojure.java.io :refer [file]]
            [clojure.string :as string]))

(defn now []
  (System/currentTimeMillis))

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
     :deltas {:delay (now)
              :sound (now)
              :cpu (now)}
     :executed-instructions 0}))

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

(defn value-or-default [ value default]
  (if (nil? value)
    default
    value))
(defn update-sound-timer [status]
  (let [update-time (now)
        last-update (value-or-default (:sound (:deltas status)) 0)
        stored-sound-timer (value-or-default (:sound (:timers status)) 0)
        should-update? (> (- update-time last-update) 16)
        sound-timer (if (and (> stored-sound-timer 0)
                             should-update?)
                      (dec stored-sound-timer)
                      stored-sound-timer)]
    (if (> sound-timer 0)
      (sound/play)
      (sound/stop))
    (if should-update?
      (assoc 
        (assoc status
               :timers
               (assoc (:timers status)
                      :sound
                      sound-timer))
        :deltas
        (assoc (:deltas status)
               :sound
               update-time))
      status)))

(defn advance-program-counter [prev-status]
  (assoc prev-status 
         :registers 
         (assoc (:registers prev-status)
                :pc
                (+ (:pc (:registers prev-status)) 2))))
(defn cycle-processor [prev-status]
  (let [current-time (now)
        last-execution (value-or-default (:cpu (:deltas prev-status)) (now))
        should-execute? (> (- current-time last-execution) 1)]
    (if should-execute?
      (let [instruction (cpu/read-instruction prev-status)
            starting-status (advance-program-counter prev-status)
            result-status (cpu/execute instruction starting-status)
            cpu-result-update (assoc result-status
                                     :deltas
                                     (assoc (:deltas result-status)
                                            :cpu 
                                            current-time))]
        (assoc cpu-result-update 
               :executed-instructions
               (inc (:executed-instructions cpu-result-update))))
      prev-status)))
(defn step
  "Executes an instruction and returns the resulting status"
  [prev-status]
  (cycle-processor
    (update-delay-timer
      (update-sound-timer prev-status))))

(defn check-rom [args]
  (when (seq? args)
    (events/mode (list :load (first args)))))

(defmulti iteration (fn [m _ _] (first m)))

(defmethod iteration :default [_ _ _]
  nil)

(defn title-text [current-file]
  (let [fname-bit (if (nil? current-file) "" " - test.ch8")]
    (str "Vip 8" fname-bit)))

(defn update-bars [_ current-file]
  (let [fpath-bit (if (nil? current-file) "<No ROM>" "/home/pablo/repos/emulators/vip-8/test.ch8")]
    (screen/title (title-text current-file))
    (screen/modline (str "Run | " fpath-bit))))

(defmethod iteration :run [current-mode current-file status]
  (update-bars current-mode current-file)
  {:mode (events/mode)
   :file current-file
   :status (step status)})

(defmethod iteration :load [current-mode current-file status]
  (let [file-to-load (second current-mode)
        file-path (file file-to-load "")
        new-status (load-rom file-to-load)]
    (screen/title (str  "Vip 8 - " (.getName file-path)))
    (screen/modline (str  "Loading " 
                         (.getAbsolutePath file-path)
                         " ..."))
    (events/mode (list :run))
    {:mode (events/mode)
     :file file-to-load
     :status new-status}))

(defmethod iteration :pause [current-mode current-file status]
  (let [fname-bit (if (nil? current-file) "" " - test.ch8")
        fpath-bit (if (nil? current-file) "<No ROM>" "/home/pablo/repos/emulators/vip-8/test.ch8")]
    (screen/title (str  "Vip 8" fname-bit))
    (screen/modline (str "Pause | " fpath-bit))
    {:mode (events/mode)
     :file current-file
     :status status}))

(defmethod iteration :command [current-mode current-file status]
  (screen/modline (second current-mode))
  {:mode (events/mode) 
   :file current-file 
   :status status })

(defmethod iteration :execute [current-mode current-file status]
  (let [order (string/split (second current-mode) #" ")]
    (cond
      (= (first order) ":q") (events/mode (list :closing))
      (= (first order) ":load") (events/mode (list :load (second order)))
      (= (first order) ":pause") (events/mode (list :pause))
      (= (first order) ":run") (events/mode (list :run)))
    {:mode (events/mode)
     :file current-file
     :status status}))

(defn run-emulator []
  (loop [current-mode (events/mode)
         current-file nil
         status {}]
    (let [result (iteration current-mode current-file status)]
      (when (not (nil? result))
        (recur (:mode result)
               (:file result)
               (:status result))))))

(defn -main [& args]
  (screen/load-window)
  (check-rom args)
  (run-emulator)
  (screen/close-window))
