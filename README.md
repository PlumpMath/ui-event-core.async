# ui-event

Trying out this article ["core. async in the browser is sweet"](http://ku1ik.com/2015/10/12/sweet-core-async.html)

Using core.async for event handling is quite nice!

Here is the function for creating a channel for an event type:

```clojure
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
```

And, here is how to define mousemove events:
```clojure
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
```

To run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).

