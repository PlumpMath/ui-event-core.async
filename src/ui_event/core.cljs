(ns ui-event.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classlist]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <! >! timeout close! alts!]]
            [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Move Your Mouse"}))

(defn header []
  [:h1 (:text @app-state)])

(defn activity-chan [input msec]
  (let [out (chan)]
    (go-loop []
      ;; wait for activity on input channel
      (<! input)
      (>! out true)

      ;; wait for inactivity on input channel
      (loop []
        (let [t (timeout msec)
              [_ c] (alts! [input t])]
          (when (= c input)
            (recur))))
      (>! out false)

      (recur))
    out))

(reagent/render-component [header]
                          (dom/getElement "header"))

(defn init []
  (let [dom-element (dom/getElement "field")
        mouse-moves (chan)
        mouse-activity (activity-chan mouse-moves 200)]
    (events/listen dom-element "mousemove" #(put! mouse-moves %))
    (go-loop []
      (let [v (<! mouse-activity)]
        (print v)
        (if v
          (classlist/add dom-element "on-mousemove")
          (classlist/remove dom-element "on-mousemove"))
        )
      (recur))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(init)
