(ns vip-8.sound
  (:require [clojure.core.async :as a])
  (:import [javax.sound.midi MidiSystem]))

(def playing? (atom false))

(defn play-note [channel note-map]
  (let [{:keys [note velocity]
         :or {note 60
              velocity 127}} note-map]
    (. channel noteOn note velocity)
    (loop [c @playing?]
      (when c
        (recur @playing?)))
    (. channel noteOff note)))

(defn play []
  (when (not @playing?)
    (swap! playing? (fn [_] true))
    (a/thread
      (with-open [synth (doto (MidiSystem/getSynthesizer) .open)]
        (let [channel (aget (.getChannels synth) 0)]
          (play-note channel {}))))))

(defn stop []
  (swap! playing? (fn [_] false)))
