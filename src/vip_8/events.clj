(ns vip-8.events)

(def active-mode (atom '(:pause)))
(def cancel-mode (atom '()))

(defn mode 
  ([] @active-mode)
  ([m] 
   (when (not= (first (mode)) :command)
     (swap! cancel-mode (fn [_] (mode))))
   (swap! active-mode (fn [_] m))))

(defn cancel []
  (mode @cancel-mode))
