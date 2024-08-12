(ns sueca-game.views
  (:require
   [re-frame.core :as re-frame]
   [sueca-game.subs :as subs]
   [sueca-game.events :as events]))

(defn render-deck
  [table round round-end?]
  (let [cards-by-round ((keyword (str round)) table)]
    [:div
     (map (fn [cards] [:div {:key cards} cards]) cards-by-round)

     [:button {:on-click (fn [] (re-frame/dispatch [::events/increment-round])) :disabled (not round-end?)} "get cards"]]))

(defn render-cards [player turn round-end?]

  (map (fn [value]
         (let [id (:id player)
               disabled (or (not (= id (str turn)))  round-end?)]
           [:div
            [:button {:on-click (fn []
                                  (re-frame/dispatch [::events/select-card value]))
                      :disabled  disabled}
             (second value)
             (first value)]]))
       (:hand player)))


(defn add-player [player turn round-end?]
  [:div {:key (:id player)}
   [:p (str "player " (:id player)) ": " (:name player)]
   [:span "hand:" (render-cards player turn round-end?)]])

(defn render-players [player-list turn round-end?]
  (map #(add-player % turn round-end?) player-list))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])
        players-cards @(re-frame/subscribe [::subs/players])
        turn @(re-frame/subscribe [::subs/turn])
        round @(re-frame/subscribe [::subs/round])
        table @(re-frame/subscribe [::subs/table])
        started? @(re-frame/subscribe [::subs/started?])
        round-end? @(re-frame/subscribe [::subs/round-end?])]
    [:div
     [:h1
      "Hello from " @name]
     [:button  {:on-click (fn [] (re-frame/dispatch [::events/start-game 1])) :disabled started?} "start game"]

     [:div "table: " table]
     (render-deck table round round-end?)
     (render-players players-cards turn round-end?)]))
