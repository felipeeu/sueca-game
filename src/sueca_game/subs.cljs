(ns sueca-game.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::players
 (fn [db]
   (:players db)))

(re-frame/reg-sub
 ::allcards
 (fn [db]
   (:allcards db)))

(re-frame/reg-sub
 ::turn
 (fn [db]
   (:turn db)))


