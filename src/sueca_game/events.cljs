(ns sueca-game.events
  (:require [clojure.set :refer [map-invert]]
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
  (assoc db
         :started? true
         :new-round true))

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

(defn is-trump?
  "check if the suit of a card is trump"
  [db card]

  (let [{:keys [trump-card]} db
        suit-trump (first trump-card)
        suit-card (first card)]
    (= suit-trump suit-card)))

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

(defn add-card-to-table
  "select the card to add table"
  [db selected-card]

  (let [{:keys [table round new-round turn round-suit]} db
        updated-table (assoc-in
                       table [(keyword (str "round" round))
                              (keyword (str "player" turn))]
                       selected-card)
        suit (if new-round (first selected-card) round-suit)]
    (merge db
           {:table  updated-table
            :new-round false
            :round-suit suit})))

(defn convert-card
  "convert card point to key"
  [card]
  (let [inverted-map (clojure.set/map-invert points-map)]
    (list (first card) (second card) (get inverted-map (last card)))))

(defn get-cards-list-by-round
  "get the list of cards in a round "
  [db round]
  (let [{:keys [table]} db
        cards-by-round ((keyword (str "round" round)) table)
        cards-list (vals cards-by-round)]
    cards-list))

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

(defn get-points-by-round
  "get the points made in a round"
  [db round]
  (let [cards-list (get-cards-list-by-round db round)
        points (get-cards-points cards-list)]
    points))

(defn get-major-card
  "get the major value card to check which player win the round"
  [cards]
  (let [card-with-points (->> cards
                              (mapv #(get-card-with-point %)))
        sort-by-points (sort-by last > card-with-points)]
    (first sort-by-points)))


(defn get-winner-card-round
  "get the card which wins the round"
  [db round]

  (let [{:keys [round-suit]} db
        cards-list (get-cards-list-by-round db round)
        trump-cards (filter #(is-trump? db %) cards-list)
        round-suit-cards (filter #(= (first %) round-suit) cards-list)]
    (if (empty? trump-cards)
      (get-major-card round-suit-cards)
      (get-major-card trump-cards))))

(defn get-winner-player-round
  "get the player who wins current round and start the next"
  [db round]
  (let [{:keys [table]} db
        card-map (-> "round"
                     (str round)
                     (keyword)
                     (table))
        winner-card (convert-card (get-winner-card-round db round))
        inverted-map (map-invert card-map)]
    (get inverted-map winner-card)))

(defn set-round-end
  "indicate the end of all turns in order to start another round"
  [db]
  (assoc db
         :round-end? (not (:round-end? db))
         :new-round true))

(defn convert-player-to-turn
  "convert keyword of player to turn number"
  [player]
  (let [player-word-length 6
        player-number (subs (name player) player-word-length)]
    (js/parseInt player-number)))

(defn increment-round
  "increment the round after all player turns"
  [db]
  (update db :round inc))

(defn increment-turn
  "inc the turn to another player start the next turn"
  [db]
  (let [{:keys [table turn round players-quantity]} db
        round-cards (get-in table [(keyword (str "round" round))])
        number-of-turns (count (keys round-cards))
        full-round? (true? (= number-of-turns players-quantity))
        current-turn (if (= players-quantity turn) 0 turn)]

    (if full-round?
      (assoc db :turn (convert-player-to-turn (get-winner-player-round db round)) :round-end? true)
      (assoc db :turn (inc current-turn) :round-end? false))))

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
 (fn [{:keys [db]} [_ _]]
   {:db (-> db
            increment-round
            (set-round-end))}))

(re-frame/reg-event-fx
 ::select-card
 (fn [{:keys [db]} [_ selected-card]]
   {:db (-> db
            (add-card-to-table selected-card)
            (remove-card selected-card)
            increment-turn)}))


