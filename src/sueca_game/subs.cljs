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
 ::turn
 (fn [db]
   (:turn db)))
(re-frame/reg-sub
 ::round
 (fn [db]
   (:round db)))

(re-frame/reg-sub
 ::table
 (fn [db]
   (:table db)))

(re-frame/reg-sub
 ::round-end?
 (fn [db]
   (:round-end? db)))

(re-frame/reg-sub
 ::started?
 (fn [db]
   (:started? db)))