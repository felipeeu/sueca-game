(ns sueca-game.views
  (:require
   [re-frame.core :as re-frame]
   [sueca-game.subs :as subs]
   [sueca-game.events :as events]
   [clojure.string :refer [lower-case]]))

(def css-suits {:♦ "diams"
                :♠ "spades"
                :♣ "clubs"
                :♥ "hearts"})
(defn render-deck
  [table round round-end?]
  (let [cards-by-round ((keyword (str round)) table)]
    [:div
     (map (fn [cards] [:div {:key cards} cards]) cards-by-round)

     [:button {:on-click (fn [] (re-frame/dispatch [::events/increment-round])) :disabled (not round-end?)} "get cards"]]))


(defn card-creator
  [values & args]
  (let [round-end? @(re-frame/subscribe [::subs/round-end?])
        turn @(re-frame/subscribe [::subs/turn])
        suit ((keyword (first values)) css-suits)
        number (lower-case (second values))
        player (second args)
        id (:id player)
        has-event (not (nil? (first args)))
        is-player-turn? (= id (str turn))
        is-dispatchable?  (and is-player-turn?
                               has-event
                               (not round-end?))]

    [:li {:key (str id suit number)}
     [:a {:class (str "card " "rank-" number " " suit)
          :on-click (if is-dispatchable?
                      (fn []  (re-frame/dispatch [(first args) values]))
                      nil)
          :style {:cursor "pointer"}}
      [:span {:class "rank"} (second values)]
      [:span {:class "suit"} (first values)]]]))

(defn render-table
  [table round]
  (let [table-by-round ((keyword (str "round" round)) table)]
    [:div {:class "playingCards"}
     [:ul {:class "table"}
      (map #(card-creator (second %)) table-by-round)]]))


(defn render-hand [player]
  [:ul {:class "hand"}
   (map #(card-creator % ::events/select-card player) (:hand player))])

(defn add-player [player]
  [:div {:key (:id player)}
   [:p (str "player " (:id player)) ": " (:name player)]
   [:div {:class "playingCards rotateHand"} (render-hand player)]])

(defn render-players [player-list]
  (map #(add-player %) player-list))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])
        players-cards @(re-frame/subscribe [::subs/players])
        round @(re-frame/subscribe [::subs/round])
        table @(re-frame/subscribe [::subs/table])
        started? @(re-frame/subscribe [::subs/started?])
        round-end? @(re-frame/subscribe [::subs/round-end?])]
    [:div
     [:h1
      "Hello from " @name]
     [:button  {:on-click (fn [] (re-frame/dispatch [::events/start-game 1]))
                :disabled started?} "start game"]

     (render-deck table round round-end?)
     (render-players players-cards)
     (render-table table round)]))
