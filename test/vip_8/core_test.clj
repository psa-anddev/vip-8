(ns vip-8.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :refer [file]]
            [vip-8.core :refer :all]
            [vip-8.rom :as rom]
            [vip-8.screen :as screen]
            [vip-8.sound :as sound]
            [vip-8.events :as events]))

(deftest load-rom-tests
  (testing "font is loading in memory in addresses from 050 to 09F"
    (with-redefs [rom/read-rom (fn [_] [])]
      (let [status (load-rom "test.ch8")
            memory (:memory status)]
        (is (= (nth memory 0x050)
               0xF0))
        (is (= (nth memory 0x051)
               0x90))
        (is (= (nth memory 0x052)
               0x90))
        (is (= (nth memory 0x053)
               0x90))
        (is (= (nth memory 0x054)
               0xF0))
        (is (= (nth memory 0x055)
               0x20))
        (is (= (nth memory 0x056)
               0x60))
        (is (= (nth memory 0x057)
               0x20))
        (is (= (nth memory 0x058)
               0x20))
        (is (= (nth memory 0x59)
               0x70))
        (is (= (nth memory 0x5A)
               0xF0))
        (is (= (nth memory 0x5B)
               0x10))
        (is (= (nth memory 0x5C)
               0xF0))
        (is (= (nth memory 0x5D)
               0x80))
        (is (= (nth memory 0x5E)
               0xF0))
        (is (=  (nth memory 0x5F)
               0xF0))
        (is (= (nth memory 0x60)
               0x10))
        (is (= (nth memory 0x61)
               0xF0))
        (is (= (nth memory 0x62)
               0x10))
        (is (= (nth memory 0x63)
               0xF0))
        (is (= (nth memory 0x64)
               0x90))
        (is (= (nth memory 0x65)
               0x90))
        (is (= (nth memory 0x66)
               0xF0))
        (is (= (nth memory 0x67)
               0x10))
        (is (= (nth memory 0x68)
               0x10))
        (is (= (nth memory 0x69)
               0xF0))
        (is (= (nth memory 0x6A)
               0x80))
        (is (= (nth memory 0x6B)
               0xF0))
        (is (= (nth memory 0x6C)
               0x10))
        (is (= (nth memory 0x6D)
               0xF0))
        (is (= (nth memory 0x6E)
               0xF0))
        (is (= (nth memory 0x6F)
               0x80))
        (is (= (nth memory 0x70)
               0xF0))
        (is (= (nth memory 0x71)
               0x90))
        (is (= (nth memory 0x72)
               0xF0))
        (is (= (nth memory 0x73)
               0xF0))
        (is (= (nth memory 0x74)
               0x10))
        (is (= (nth memory 0x75)
               0x20))
        (is (= (nth memory 0x76)
               0x40))
        (is (= (nth memory 0x77)
               0x40))
        (is (= (nth memory 0x78)
               0xF0))
        (is (= (nth memory 0x79)
               0x90))
        (is (= (nth memory 0x7A)
               0xF0))
        (is (= (nth memory 0x7B)
               0x90))
        (is (= (nth memory 0x7C)
               0xF0))
        (is (= (nth memory 0x7D)
               0xF0))
        (is (= (nth memory 0x7E)
               0x90))
        (is (= (nth memory 0x7F)
               0xF0))
        (is (= (nth memory 0x80)
               0x10))
        (is (= (nth memory 0x81)
               0xF0))
(is (= (nth memory 0x82)
       0xF0))
(is (= (nth memory 0x83)
       0x90))
(is (= (nth memory 0x84)
       0xF0))
(is (= (nth memory 0x85)
       0x90))
(is (= (nth memory 0x86)
       0x90))
(is (= (nth memory 0x87)
       0xE0))
(is (= (nth memory 0x88)
       0x90))
(is (= (nth memory 0x89)
       0xE0))
(is (= (nth memory 0x8A)
       0x90))
(is (= (nth memory 0x8B)
       0xE0))
(is (= (nth memory 0x8C)
       0xF0))
(is (= (nth memory 0x8D)
       0x80))
(is (= (nth memory 0x8E)
       0x80))
(is (= (nth memory 0x8F)
       0x80))
(is (= (nth memory 0x90)
       0xF0))
(is (= (nth memory 0x91)
       0xE0))
(is (= (nth memory 0x92)
       0x90))
(is (= (nth memory 0x93)
       0x90))
(is (= (nth memory 0x94)
       0x90))
(is (= (nth memory 0x95)
       0xE0))
(is (= (nth memory 0x96)
       0xF0))
(is (= (nth memory 0x97)
       0x80))
(is (= (nth memory 0x98)
       0xF0))
(is (= (nth memory 0x99)
       0x80))
(is (= (nth memory 0x9A)
       0xF0))
(is (= (nth memory 0x9B)
       0xF0))
(is (= (nth memory 0x9C)
       0x80))
(is (= (nth memory 0x9D)
       0xF0))
(is (= (nth memory 0x9E)
       0x80))
(is (= (nth memory 0x9F)
       0x80)))))
(testing "Rom is loaded from address 0x200"
  (let [expected [0x00 0xE0 0x14 0x78]]
    (with-redefs [rom/read-rom (fn [_] expected)]
      (let [status (load-rom "test.ch8")
            memory (:memory status)
            actual (take (count expected) 
                         (drop 0x200 memory))]
        (is (= actual expected))))))
(testing "there are 4096 bytes of available memory"
  (let [rom [0x00 0xE0 0x14 0x78]]
    (with-redefs [rom/read-rom (fn [_] rom)]
      (let [status (load-rom "test.ch8")
            memory (:memory status)]
        (is (= 4096 (count memory)))))))
(testing "the stack is empty"
  (with-redefs [rom/read-rom (fn [_] [])]
    (let [stack (:stack (load-rom "test.ch8"))]
      (is (and (empty? stack)
               (not (nil? stack)))))))
(testing "the v registers are set to 0x00"
  (with-redefs [rom/read-rom (fn [_] [])]
    (let [registers (:registers (load-rom "tetris.ch8"))]
      (is (and (= (:v0 registers)
                  0x00)
               (= (:v1 registers)
                  0x00)
               (= (:v2 registers)
                  0x00)
               (= (:v3 registers)
                  0x00)
               (= (:v4 registers)
                  0x00)
               (= (:v5 registers)
                  0x00)
               (= (:v6 registers)
                  0x00)
               (= (:v7 registers)
                  0x00)
               (= (:v8 registers)
                  0x00)
               (= (:v9 registers)
                  0x00)
               (= (:vA registers)
                  0x00)
               (= (:vB registers)
                  0x00)
               (= (:vC registers)
                  0x00)
               (= (:vD registers)
                  0x00)
               (= (:vE registers)
                  0x00)
               (= (:vF registers)
                  0x00))))))
(testing "program counter is set to 0x200"
  (with-redefs [rom/read-rom (fn [_] [])]
    (is (= (:pc (:registers (load-rom "tetris.ch8")))
           0x200))))
(testing "timers start at value 0"
  (with-redefs [rom/read-rom (fn [_] [])]
    (let [timers (:timers (load-rom "tetris.ch8"))]
      (is (and (= (:delay timers)
                  0)
               (= (:sound timers)
                  0))))))
(testing "index register is set to 0"
  (with-redefs [rom/read-rom (fn [_] [])]
    (let [registers (:registers (load-rom "tetris.ch8"))]
      (is (= (:index registers) 0x000)))))
(let [screen (atom (repeat 32 (repeat 64 true)))
      current-time (atom (System/currentTimeMillis))]
  (with-redefs [rom/read-rom 
                (fn [_] [0x00 0xE0 0xA2 0x01])
                screen/clear 
                (fn []
                  (swap! screen 
                         (fn [_] 
                           (repeat 32 (repeat 64 false)))))
                now (fn [] 
                      (let [result @current-time]
                        (swap! current-time inc)
                        result))]
    (let [initial-status (load-rom "pong.ch8")]
      (testing "clear instruction clears the screen"
        (let [status (step initial-status)]
          (is (= (:pc (:registers status))
                 0x202))
          (is (= (count (filter 
                          #(> (count 
                                (filter (fn [x] x) %)) 0) @screen))
                 0))))
      (testing "index register can be set"
        (let [status (step (assoc initial-status
                                  :registers
                                  (assoc (:registers initial-status)
                                         :pc
                                         0x202)))
              registers (:registers status)]
          (is (= (:pc registers) 0x204))
          (is (= (:index registers) 0x201))))))))

(deftest timers-test
  (testing "delay timer doesn't get modified if delta is less than 60 Hz"
    (with-redefs [now (fn [] 1400)]
      (let [status (step {:memory [0x00 0xE0]
                           :registers {:pc 0x0}
                           :timers {:delay 0x10}
                           :deltas {:delay 1398}})]
        (is (= (:delay (:timers status)) 0x10))
        (is (= (:delay (:deltas status)) 1398)))))

  (testing "delay timer gets modified if the delta is over 60 Hz"
    (with-redefs [now (fn [] 1500)]
      (let [status (step {:memory [0x00 0xE0]
                           :registers {:pc 0x0}
                           :timers {:delay 0x10}
                           :deltas {:delay 1483}})]
        (is (= (:delay (:timers status)) 0xF))
        (is (= (:delay (:deltas status)) 1500)))))
  (testing "delay timer stays at 0"
    (with-redefs [now (fn [] 1600)]
      (let [status (step {:memory [0x00 0xE0]
                          :registers {:pc 0x0}
                          :timers {:delay 0x0}
                          :deltas {:delay 1483}})]
        (is (= (:delay (:timers status)) 0x0))
        (is (= (:delay (:deltas status)) 1600)))))
  (let [sound (atom :stopped)
        current-time (atom 1000)]
    (with-redefs [now (fn [] @current-time)
                  sound/play 
                  (fn [] (swap! sound (fn [_] :playing)))
                  sound/stop 
                  (fn [] (swap! sound (fn [_] :stopped)))]
      (testing "no sound is played with the sound timer set to 0"
        (step {:memory [0x00 0xE0]
               :registers {:pc 0x0}
               :deltas {:sound 1000
                        :delay 1000}
               :timers {:sound 0
                        :delay 0}})
        (is (= @sound :stopped)))
      (testing "sound is played when the sound timer is greater than 0"
        (step {:memory [0x00 0xE0]
               :registers {:pc 0x0}
               :deltas {:sound 1000
                        :delay 1000}
               :timers {:sound 1
                        :delay 0}})
        (is (= @sound :playing)))
      (testing "sound timer is updated at 60 Hz"
        (swap! current-time #(+ % 20))
        (let [result (step {:memory [0x00 0xE0]
                            :registers {:pc 0x0}
                            :deltas {:sound 1000
                                     :delay 1000}
                            :timers {:sound 1
                                     :delay 0}})]
          (is (= (:sound (:timers result)) 0))
          (is (= (:sound (:deltas result)) 1020))
          (is (= @sound :stopped)))))))

(deftest main-tests
  (let [main-window-loaded? (atom false)
        operations (atom '())
        modes (atom '())]
    (defn mode-fn 
      ([] (let [m (first @modes)]
            (swap! modes rest)
            m))
      ([m] (swap! operations #(concat % (list {:set-mode m})))))
    (defn clear-operations []
      (swap! operations (fn [_] '())))

    (defn set-modes [& new-modes]
      (swap! modes #(concat new-modes %)))

    (with-redefs [screen/load-window 
                  (fn [] (swap! main-window-loaded? (fn [_] true)))
                  screen/close-window 
                  (fn [] (swap! operations #(concat % '(:window-closed))))
                  load-rom (fn [f] (swap! operations #(concat % (list {:load f}))))
                  events/mode mode-fn
                  step (fn [_] (swap! operations #(concat % '(:step))))]
      (testing "loads the main window"
        (-main)
        (is @main-window-loaded?))
      (testing "closes the window when in closing mode"
        (clear-operations)
        (set-modes '(:closing))
        (-main)
        (is (= @operations '(:window-closed))))
      (testing "loads the tetris ROM"
        (clear-operations)
        (set-modes '(:load "tetris.ch8")
                   '(:closing))
        (-main)
        (is (= @operations
               '({:load "tetris.ch8"}
                 {:set-mode (:run)}
                 :window-closed))))
      (testing "loads the pacman ROM"
        (clear-operations)
        (set-modes '(:load "pacman.ch8")
                   '(:closing))
        (-main)
        (is (= @operations
               '({:load "pacman.ch8"}
                 {:set-mode (:run)}
                 :window-closed))))
      (testing "steps once"
        (clear-operations)
        (set-modes '(:run) '(:closing))
        (-main)
        (is (= @operations '(:step :window-closed))))
      (testing "steps twice"
        (clear-operations)
        (set-modes '(:run) '(:run) '(:closing))
        (-main)
        (is (= @operations 
               '(:step :step :window-closed))))
      (testing "loads and runs the mario ROM if passed as argument"
        (clear-operations)
        (set-modes '(:closing))
        (-main "mario.ch8")
        (is (= @operations
               '({:set-mode (:load "mario.ch8")}
                 :window-closed))))
      (testing "loads and runs the lunar lander ROM if passed as argument"
        (clear-operations)
        (set-modes '(:closing))
        (-main "lunar-lander.ch8")
        (is (= @operations
               '({:set-mode (:load "lunar-lander.ch8")}
                 :window-closed))))
      (testing "pause keeps the loop running"
        (clear-operations)
        (set-modes '(:pause)
                   '(:run)
                   '(:closing))
        (-main)
        (is (= @operations
               '(:step :window-closed))))
      (testing "command keeps the loop running"
        (clear-operations)
        (set-modes '(:command ":test")
                   '(:run)
                   '(:closing))
        (-main)
        (is (= @operations '(:step :window-closed))))
      (testing "command updates the modline"
        (clear-operations)
        (set-modes '(:command ":test")
                   '(:closing))
        (-main)
        (is (= (screen/modline) ":test"))
        (clear-operations)
        (set-modes '(:command ":q")
                   '(:closing))
        (-main)
        (is (= (screen/modline) ":q")))
      (testing "pause mode shows pause modeline and title"
        (clear-operations)
        (set-modes '(:pause)
                   '(:closing))
        (-main)
        (is (= (screen/modline) "Pause | <No ROM>"))
        (is (= (screen/title) "Vip 8")))
      (testing "load mode shows loading modeline and title"
        (testing "load ./ROMS/test.ch8"
          (let [abs-path (.getAbsolutePath (file "./ROMS/test.ch8"))]
            (clear-operations)
            (set-modes '(:load "./ROMS/test.ch8")
                       '(:closing))
            (-main)
            (is (= (screen/title) "Vip 8 - test.ch8"))
            (is (= (screen/modline) (str "Loading " abs-path " ...")))))
        (testing "load ~/roms/chipquarium.ch8"
          (let [abs-path (.getAbsolutePath (file "~/roms/chipquarium.ch8"))]
            (clear-operations)
            (set-modes 
              '(:load "~/roms/chipquarium.ch8"))
            (-main)
            (is (= (screen/title) 
                   "Vip 8 - chipquarium.ch8"))
            (is (= (screen/modline) 
                   (str "Loading " abs-path " ..."))))))
(testing "test.ch8 is displayed properly"
  (let [abs-path (.getAbsolutePath (file "test.ch8"))]
    (testing "pause mode shows the right file name"
      (clear-operations)
      (set-modes '(:load "test.ch8")
                 '(:pause))

      (-main)
      (is (= (screen/title) "Vip 8 - test.ch8"))
      (is (= (screen/modline) 
             (str "Pause | " abs-path))))
    (testing "run mode shows the right file name"
      (clear-operations)
      (set-modes '(:load "test.ch8")
                 '(:run))

      (-main)
      (is (= (screen/title) "Vip 8 - test.ch8"))
      (is (= (screen/modline) 
             (str "Run | " abs-path))))))
(testing "space-invaders.ch8 is displayed properly"
  (let [abs-path (.getAbsolutePath (file "space-invaders.ch8"))]
    (testing "pause mode shows the right file name"
      (clear-operations)
      (set-modes '(:load "space-invaders.ch8")
                 '(:pause))

      (-main)
      (is (= (screen/title) "Vip 8 - space-invaders.ch8"))
      (is (= (screen/modline) 
             (str "Pause | " abs-path))))
    (testing "run mode shows the right file name"
      (clear-operations)
      (set-modes '(:load "space-invaders.ch8")
                 '(:run))

      (-main)
      (is (= (screen/title) "Vip 8 - space-invaders.ch8"))
      (is (= (screen/modline) 
             (str "Run | " abs-path))))))

(testing "modline doesn't get updated if mode hasn't changed"
  (clear-operations)
  (set-modes '(:pause))
  
  (screen/modline "Pause | Error")

  (-main)
  
  (is (= (screen/modline) "Pause | Error")))
(testing "commands get executed"
  (testing ":q will quit"
    (clear-operations)
    (set-modes '(:execute ":q"))

    (-main)
    (is (= @operations '({:set-mode (:closing)} 
                         :window-closed))))
  (testing ":load will load chipquarium"
    (clear-operations)
    (set-modes '(:execute ":load chipquarium.ch8"))

    (-main)
    
    (is (= @operations '({:set-mode (:load "chipquarium.ch8")} 
                         :window-closed))))
  (testing ":load will load mario"
    (clear-operations)
    (set-modes '(:execute ":load mario.ch8"))

    (-main)

    (is (= @operations '({:set-mode (:load "mario.ch8")} 
                         :window-closed))))
  (testing ":pause will pause"
    (clear-operations)
    (set-modes '(:execute ":pause"))
    
    (-main)
    
    (is (= @operations '({:set-mode (:pause)}
                         :window-closed))))
  (testing ":run will run"
    (clear-operations)
    (set-modes '(:execute ":run"))
    
    (-main)
    
    (is (= @operations '({:set-mode (:run)}
                         :window-closed))))
  
  (testing "unrecognized commands will be ignored"
    (clear-operations)
    (set-modes '(:execute ":aa")
               '(:execute ":bb")
               '(:execute ":false"))
    
    (-main)
    
    (is (= @operations '(:window-closed))))))

(with-redefs [screen/load-window 
              (fn [] (swap! main-window-loaded? (fn [_] true)))
              screen/close-window 
              (fn [] (swap! operations #(concat % '(:window-closed))))
              load-rom (fn [_] 
                         (throw (Exception. "File not found")))
              events/mode mode-fn
              step (fn [_] 
                     (swap! operations #(concat % '(:step))))]
  (clear-operations)
  (set-modes '(:load "chipquarium.ch8"))
  
  (-main)
  
  (is (= @operations '({:set-mode (:pause)} :window-closed)))
  (is (= (screen/modline) "Pause | Error: File not found"))
  (is (= (screen/title) "Vip 8")))))
