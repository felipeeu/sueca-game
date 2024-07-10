(ns sueca-game.events
  (:require
   [re-frame.core :as re-frame]
   [sueca-game.db :as db]))


;---------------------------------------------- Functions ---------------------------------------------------------------------------------------------------
(def ^:const points-map   {:ace 11
                           :seven 10
                           :king 4
                           :jack 3
                           :queen 2
                           :none 0})

(defn start-game 
  "add a flag to indicate the game was started"
  [db]
  (assoc db :started? true))

(defn prepare-cards 
  "map the useful cards with its suits"
  [db]
  (let [{:keys [values suits]} (:cards db)
        suits  (list (:spade suits)
                     (:heart suits)
                     (:diamond suits)
                     (:club suits))
        cards (mapcat (fn [value]
                        (map #(conj value %)
                             suits)) values)]
    (assoc-in db [:allcards] cards)))

(defn spread-cards 
  "spread cards ramdomically"
  [db] 
  (let [{:keys [allcards players-quantity]} db
        players  (range players-quantity)
        shuffled-cards (shuffle allcards)
        split (partition 10 shuffled-cards)
        spread (map #(assoc {} :id  (str (inc %))
                            :hand (nth split %)) players)]
    (assoc-in db [:players] spread)))

(defn get-trump 
  "get the trump card to use for 10 rounds"
  [db]
  (let [{:keys [allcards]} db
        trump (rand-nth allcards)]
    (assoc db :trump-card trump)))

(defn set-player-turn 
  "set the turns to start a game"
  [db player]
  (assoc db :turn player
         :trump-player (+ player 3)))

(defn remove-card 
  "remove the selected card from the hand of the player"
  [db selected-card]
  (let [{:keys [players]} db
        updated-players (map (fn
                               [player]
                               (update-in player [:hand]
                                          #(filter
                                            (fn
                                              [card]
                                              (not (= card selected-card))) %))) players)]

    (assoc db :players updated-players)))

(defn inc-turn 
  "inc the turn to another player start the next turn"
  [db]
  (let [{:keys [turn players-quantity]} db]
    (if (= turn players-quantity)
      (assoc db :turn 1 :round-end? true)
      (assoc db :turn (inc turn) :round-end? false))))

(defn select-card 
  "select the card to add table"
  [db selected-card]
  (let [{:keys [table round]} db
        cards (update table (keyword (str round)) conj selected-card)]
    (assoc db :table cards)))

(defn set-round-end 
"indicate the end of all turns in order to start another round"
  [db]
  (assoc db :round-end? (not (:round-end? db))))

(defn get-trump-suit 
"get the trump suit that will be used for a entirely round"
[card-trump]
  (first card-trump))

(defn get-card-point 
"get the point of the card based on a constant points map"
 [card]
  ((last card) points-map))

(defn get-card-with-point 
"get the default card format with its point added"
[card]
  (list 
  (str (first card)) 
  (str (second card)) 
  (get-card-point card)))

(defn get-cards-points 
"get the sum of points of a collection of cards"
  [cards]
  (reduce + (map #(get-card-point %) cards)))

(defn get-round-points 
"get the points made in a round"
  [db round]
  (let [{:keys [table round-points]} db
        cards-by-round ((keyword (str round)) table)
        points (get-cards-points cards-by-round)
        pontuation (conj round-points points)]
    (assoc db :round-points pontuation)))

(defn get-major-card 
"get the major value card to check which player win the round"
  [cards]
  (let [card-with-points (->> cards
                              (mapv #(get-card-with-point %)))
        sort-by-points (sort-by last > card-with-points)]
    (first sort-by-points)))

(defn increment-round 
"increment the round after all player turns"
  [db]
  (update db :round inc))

(defn pair-players 
"sort the players that will be pairs for all game"
  [db]
  (let [{:keys [players-quantity]} db
        players (map inc (range players-quantity))
        randomized (shuffle players)
        pairs-quantity (/ players-quantity 2)
        pairs (partition pairs-quantity randomized)]
    (assoc db :pairs pairs)))

;-------Event--Handlers-----------------------------------------------------------------------------------------------------------------------------------------------------

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::start-game
 (fn [{:keys [db]} [_ match]]
   {:db (-> db
            start-game
            prepare-cards
            spread-cards
            get-trump
            pair-players
            (set-player-turn match))}))

(re-frame/reg-event-fx
 ::increment-round
 (fn [{:keys [db]} [_ round]]
   {:db (-> db
            increment-round
            set-round-end
            (get-round-points round))}))

(re-frame/reg-event-fx
 ::select-card
 (fn [{:keys [db]} [_ selected-card]]
   {:db (-> db
            (select-card selected-card)
            (remove-card selected-card)
            inc-turn)}))


