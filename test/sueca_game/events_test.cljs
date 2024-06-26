(ns sueca-game.events-test
  (:require [cljs.test :refer (deftest is testing)]
            [sueca-game.events  :as events]))


(deftest start-game-test
  (testing "start-game function"
    (let [initial-db {}
          expected-db {:started? true}
          new-db (events/start-game initial-db)]
      (is (= expected-db new-db))))

  (testing "start-game with existing data"
    (let [initial-db {:players ["Alice" "Bob"]}
          expected-db {:started? true :players ["Alice" "Bob"]}
          new-db (events/start-game initial-db)]
      (is (= expected-db new-db))))

  (testing "start-game when already started"
    (let [initial-db {:started? true}
          expected-db {:started? true}
          new-db (events/start-game initial-db)]
      (is (= expected-db new-db)))))


(deftest get-round-points-test
  (testing "get round points function"
    (let [initial-db {}
          round 1
          expected-db {:round-points '(0)}
          new-db (events/get-round-points initial-db round)]

      (is (= expected-db
             new-db)))))

(deftest convert-points-test
  (testing "convert points"
    (let  [cards '(("♠" "6" :none) ("♦" "Q" :queen) ("♠" "5" :none) ("♣" "2" :none))
           points 2]

      (is (= points (events/get-cards-points cards))))))

(deftest get-card-point-test
  (testing "test ace card "
    (let [card '("♥" "A" :ace)
          calculated-value (events/get-card-point card)
          expected-value 11]
      (is (= calculated-value expected-value))))

  (testing "test no-value card "
    (let [card '("♥" "6" :none)
          calculated-value (events/get-card-point card)
          expected-value 0]
      (is (= calculated-value expected-value)))))

(deftest get-trump-suit-test
  (testing "test trump suit"
    (let [trump-card '("♦" "7" :seven)
          received-suit (events/get-trump-suit trump-card)
          expected-suit "♦"] (is (= received-suit expected-suit)))))

(deftest get-major-card-test
  (testing "major card"
    (let [cards '(("♦" "7" :seven) ("♦" "Q" :queen))
          expected-card '("♦" "7" 10)
          receive-card (events/get-major-card cards)]
      (is (= expected-card receive-card)))))


(deftest get-card-with-point-test
  (testing "card with point"
    (let [card '("♦" "A" :ace)
          expected-card '("♦" "A" 11)
          receive-card (events/get-card-with-point card)]
      (is (= expected-card receive-card)))))