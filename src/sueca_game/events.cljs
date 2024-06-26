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

(defn start-game [db]
  (assoc db :started? true))

(defn prepare-cards [db]
  (let [{:keys [values suits]} (:cards db)
        suits  (list (:spade suits)
                     (:heart suits)
                     (:diamond suits)
                     (:club suits))
        cards (mapcat (fn [value]
                        (map #(conj value %)
                             suits)) values)]
    (assoc-in db [:allcards] cards)))

(defn spread-cards [db]
  (let [{:keys [allcards players-quantity]} db
        players  (range players-quantity)
        shuffled-cards (shuffle allcards)
        split (partition 10 shuffled-cards)
        spread (map #(assoc {} :id  (str (inc %))
                            :hand (nth split %)) players)]
    (assoc-in db [:players] spread)))

(defn get-trump [db]
  (let [{:keys [allcards]} db
        trump (rand-nth allcards)]
    (assoc db :trump-card trump)))

(defn set-player-turn [db player]
  (assoc db :turn player
         :trump-player (+ player 3)))

(defn remove-card [db selected-card]

  (let [{:keys [players]} db
        updated-players (map (fn
                               [player]
                               (update-in player [:hand]
                                          #(filter
                                            (fn
                                              [card]
                                              (not (= card selected-card))) %))) players)]

    (assoc db :players updated-players)))

(defn inc-turn [db]
  (let [{:keys [turn players-quantity]} db]
    (if (= turn players-quantity)
      (assoc db :turn 1 :turn-end? true)
      (assoc db :turn (inc turn) :turn-end? false))))

(defn select-card [db selected-card]
  (let [{:keys [table round]} db
        cards (update table (keyword (str round)) conj selected-card)]
    (assoc db :table cards)))

(defn set-turn-end [db]
  (assoc db :turn-end? (not (:turn-end? db))))

(defn get-trump-suit [card-trump]
  (first card-trump))

(defn get-card-point [card]
  ((last card) points-map))

(defn get-card-with-point [card]
  (list (str (first card)) (str (second card)) (get-card-point card)))

(defn get-cards-points [cards]
  (reduce + (map #(get-card-point %) cards)))

(defn get-round-points [db round]
  (let [{:keys [table round-points]} db
        cards-by-round ((keyword (str round)) table)
        points (get-cards-points cards-by-round)
        pontuation (conj round-points points)]
    (assoc db :round-points pontuation)))

(defn get-major-card [cards]
  (let [card-with-points (->> cards
                              (mapv #(get-card-with-point %)))
        sort-by-points (sort-by last > card-with-points)]
    (first sort-by-points)))

(defn increment-round [db]
  (update db :round inc))

(defn pair-players [db]
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
            set-turn-end
            (get-round-points round))}))

(re-frame/reg-event-fx
 ::select-card
 (fn [{:keys [db]} [_ selected-card]]
   {:db (-> db
            (select-card selected-card)
            (remove-card selected-card)
            inc-turn)}))


