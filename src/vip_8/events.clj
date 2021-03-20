(ns vip-8.events)

(def active-mode (atom '(:pause)))

(defn mode 
  ([] @active-mode)
  ([m] (swap! active-mode (fn [_] m))))

(defn cancel [])
