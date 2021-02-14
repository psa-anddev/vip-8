(ns vip-8.screen
  (:require [cljfx.api :as fx]
            [vip-8.keyboard :as keyboard])
  (:import [javafx.scene.canvas Canvas]
           [javafx.scene.paint Color]))

(def active-pixels (atom #{}))

(defn clear 
  "Clears the screen"
  []
  (swap! active-pixels (fn [_] #{})))

(defn is-on? 
  "Returns true if the given pixel is on"
  [x y]
  (contains? @active-pixels (list x y)))

(defn set 
  "Sets a pixel of the screen"
  [x y on?]
  (swap! active-pixels
         (fn [v] 
           (into #{}
                 (if on?
                   (cons (list x y) v)
                   (filter (fn [[vx vy]] (or (not= x vx)
                                             (not= y vy)))
                           v))))))

(defn width []
  64)

(defn height []
  32)

(defn emulator-display [{:keys [viewport-width viewport-height active-pixels]}]
  (let [x-scale (/ viewport-width (width))
        y-scale (/ viewport-height (height))]
    {:fx/type :canvas
     :width viewport-width
     :height viewport-height
     :draw (fn [^Canvas canvas]
             (let [context (.getGraphicsContext2D canvas)]
               (doto context
                 (.clearRect 0 0 viewport-width viewport-height)
                 (.setFill Color/BLACK)
                 (.fillRect 0 0 viewport-width viewport-height)
                 (.setFill Color/LIGHTGRAY))
               (loop [rem-pixels active-pixels]
                 (when (not (empty? rem-pixels))
                   (let [[x y] (first rem-pixels)]
                     (.fillRect context 
                                (* x x-scale)
                                (* y y-scale)
                                x-scale
                                y-scale))
                   (recur (rest rem-pixels))))))}))

(def renderer
  (fx/create-renderer 
    :opts {:fx.opt/map-event-handler keyboard/handle-keyboard-event}
    :middleware 
    (fx/wrap-map-desc
      (fn [active-pixels]
        {:fx/type :stage
         :showing true
         :title "Vip 8"
         :width 1024
         :height 512
         :scene {:fx/type :scene
                 :on-key-pressed {:event/type ::keyboard/key_pressed}
                 :on-key-released {:event/type ::keyboard/key_released}
                 :root {:fx/type :v-box
                        :alignment :center
                        :children [{:fx/type emulator-display
                                    :viewport-width 512
                                    :viewport-height 256
                                    :active-pixels active-pixels}]}}}))))


(defn load-window []
  (fx/on-fx-thread
    (fx/mount-renderer active-pixels renderer)))



