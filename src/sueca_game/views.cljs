(ns sueca-game.views
  (:require
   [re-frame.core :as re-frame]
   [sueca-game.subs :as subs]
   [sueca-game.events :as events]))



(defn card [player turn]

  (map (fn [value]
         (let [id (:id player)
               disabled (not (= id (str turn)))]
           [:div
            [:button {:on-click (fn []
                                  (re-frame/dispatch [::events/select-card value id]))
                      :disabled  disabled}
             (second value)
             (first value)]]))
       (:hand player)))


(defn create-player [player turn]
  [:div {:key (:id player)}
   [:p (str "player " (:id player)) ": " (:name player)]
   [:span "hand:" (card player turn)]])

(defn create-deck [player-list turn]
  (map #(create-player % turn) player-list))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])
        players-cards @(re-frame/subscribe [::subs/players])
        allcards @(re-frame/subscribe [::subs/allcards])
        turn @(re-frame/subscribe [::subs/turn])]
    [:div
     [:h1
      "Hello from " @name]
     [:button  {:on-click (fn [] (re-frame/dispatch [::events/prepare-cards]))} "prepare cards"]
     [:button  {:on-click (fn [] (re-frame/dispatch [::events/spread-cards])) :disabled (not allcards)} "spread cards"]

     [:p allcards]
     (create-deck players-cards turn)]))
