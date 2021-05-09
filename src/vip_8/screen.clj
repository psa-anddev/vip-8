(ns vip-8.screen
  (:require [cljfx.api :as fx]
            [vip-8.keyboard :as keyboard])
  (:import [javafx.scene.canvas Canvas]
           [javafx.scene.paint Color]))

(def ui-state (atom {:active-pixels #{}
                     :title "Vip 8"
                     :modline ""
                     :showing true}))

(defn clear 
  "Clears the screen"
  []
  (swap! ui-state #(assoc % :active-pixels #{})))

(defn is-on? 
  "Returns true if the given pixel is on"
  [x y]
  (contains? (:active-pixels @ui-state)
             (list x y)))

(defn set-pixel 
  "Sets a pixel of the screen"
  [x y on?]
  (let [active-pixels (:active-pixels @ui-state)]
    (swap! ui-state 
           #(assoc % :active-pixels 
                   (into #{} 
                         (if on?
                           (cons (list x y) active-pixels)
                           (filter (fn [[vx vy]] (or (not= x vx)
                                                     (not= y vy)))
                                   active-pixels)))))))

(defn width []
  64)

(defn height []
  32)

(defn title 
  ([] (:title @ui-state))
  ([title] (swap! ui-state #(assoc % :title title))))

(defn modline 
  ([] (:modline @ui-state))
  ([modline] (swap! ui-state #(assoc % :modline modline))))

(defn emulator-display [{:keys [active-pixels modline]}]
  {:fx/type :canvas
   :draw (fn [^Canvas canvas]
           (let [context (.getGraphicsContext2D canvas)
                 canvas-width (.getWidth canvas)
                 canvas-height (.getHeight canvas)
                 display-height (/ canvas-width 2)
                 display-y-offset (/ (- canvas-height display-height)
                                     2)
                 x-scale (/ canvas-width (width))
                 y-scale (/ display-height (height))]
             (doto context
               (.clearRect 0 0 canvas-width canvas-height)
               (.setFill Color/BLACK)
               (.fillRect 0 0 canvas-width canvas-height)
               (.setFill Color/LIGHTGRAY)
               (.setStroke Color/LIGHTGRAY)
               (.fillText modline 10 
                          (* canvas-height 0.99))
               (.strokeLine 0 
                          (* canvas-height 0.97)
                          canvas-width 
                          (* canvas-height 0.97)))
             (loop [rem-pixels active-pixels]
               (when (seq rem-pixels) 
                 (let [[x y] (first rem-pixels)]
                   (.fillRect context 
                              (* x x-scale)
                              (+  (* y y-scale) display-y-offset)
                              x-scale
                              y-scale))
                 (recur (rest rem-pixels))))))})

(def renderer
  (fx/create-renderer 
    :opts {:fx.opt/map-event-handler keyboard/handle-keyboard-event}
    :middleware 
    (fx/wrap-map-desc
      (fn [ui-state]
        {:fx/type :stage
         :on-close-request {:event/type ::keyboard/close}
         :showing (:showing ui-state)
         :title (:title ui-state)
         :scene {:fx/type :scene
                 :on-key-pressed {:event/type ::keyboard/key_pressed}
                 :on-key-released {:event/type ::keyboard/key_released}
                 :fill :black
                 :root {:fx/type fx/ext-on-instance-lifecycle
                         :on-created 
                         #(doseq [child (.getChildrenUnmodifiable %)]
                            (when (instance? Canvas child)
                              (.bind (.widthProperty child) (.widthProperty %))
                              (.bind (.heightProperty child) (.heightProperty %))))
                        :desc {:fx/type :v-box
                               :alignment :center
                               :children [{:fx/type emulator-display
                                           :active-pixels (:active-pixels ui-state)
                                           :modline (:modline ui-state)}]}}}}))))


(defn load-window []
  (swap! ui-state #(assoc % :showing true))
  (fx/on-fx-thread
    (fx/mount-renderer ui-state renderer)))

(defn close-window []
  (swap! ui-state #(assoc % :showing false))
  (fx/on-fx-thread
    (fx/unmount-renderer ui-state renderer)))

