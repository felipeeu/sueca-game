(ns sueca-game.events
  (:require
   [re-frame.core :as re-frame]
   [sueca-game.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))


(re-frame/reg-event-db
 ::prepare-cards
 (fn [db [_ _]]
   (let [{:keys [values suits]} (:cards db)
         suits  (list (:spade suits)
                      (:heart suits)
                      (:diamond suits)
                      (:club suits))
         cards (mapcat (fn [value]
                         (map #(conj value %)
                              suits)) values)]
     (assoc-in db [:allcards] cards))))


(re-frame/reg-event-db
 ::spread-cards
 (fn [db [_ _]]
   (let [{:keys [allcards players-quantity]} db
         players  (range players-quantity)
         shuffled-cards (shuffle allcards)
         split (partition 10 shuffled-cards)
         spread (map #(assoc {} :id  (str (inc %))
                             :hand (nth split %)) players)]
     (assoc-in db [:players] spread))))


(re-frame/reg-event-db
 :inc-turn
 (fn [db [_ _]]
   (let [{:keys [turn players-quantity]} db]
     (if (= turn players-quantity)
       (assoc db :turn 1)
       (assoc db :turn (inc turn))))))

(re-frame/reg-event-db
 :remove-card
 (fn [db [_ selected-card]]
   (let [{:keys [players]} db
         updated-players (map (fn
                                [player]
                                (update-in player [:hand]
                                           #(filter
                                             (fn
                                               [card]
                                               (not (= card selected-card))) %))) players)]

     (assoc db :players updated-players)))) ;


(re-frame/reg-event-fx
 ::select-card
 (fn [{:keys [db]} [_ selected-card]]
   (let [{:keys [table]} db
         cards (conj table selected-card)]
     {:db (assoc db :table cards)
      :fx [[:dispatch [:remove-card selected-card]] [:dispatch [:inc-turn]]]})))