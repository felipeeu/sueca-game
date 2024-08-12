(ns sueca-game.events-test
  (:require [cljs.test :refer (deftest is are testing)]
            [sueca-game.events  :as events]))


(deftest start-game-test
  (testing "start-game function"
    (let [initial-db {}
          expected-db {:started? true :new-round true}
          new-db (events/start-game initial-db)]
      (is (= expected-db new-db))))

  (testing "start-game with existing data"
    (let [initial-db {:players ["Alice" "Bob"]}
          expected-db {:started? true :players ["Alice" "Bob"] :new-round true}
          new-db (events/start-game initial-db)]
      (is (= expected-db new-db))))

  (testing "start-game when already started"
    (let [initial-db {:started? true}
          expected-db {:started? true :new-round true}
          new-db (events/start-game initial-db)]
      (is (= expected-db new-db)))))

(let [initial-db {:table
                  {:round1 {:player1 '("♦" "Q" :queen)
                            :player2 '("♦" "5" :none)
                            :player3 '("♦" "2" :none)
                            :player4 '("♦" "6" :none)}

                   :round2 {:player1 '("♦" "3" :none)
                            :player2 '("♦" "A" :ace)
                            :player3 '("♦" "7" :seven)
                            :player4 '("♦" "4" :none)}

                   :round3 {:player1 '("♦" "5" :none)
                            :player2 '("♦" "A" :ace)
                            :player3 '("♦" "Q" :queen)
                            :player4 '("♣" "4" :none)}

                   :round4 {:player1 '("♣" "4" :none)
                            :player2 '("♦" "A" :ace)
                            :player3 '("♦" "7" :seven)
                            :player4 '("♣" "6" :none)}

                   :round5 {:player1 '("♦" "2" :none)
                            :player2 '("♦" "6" :none)
                            :player3 '("♦" "5" :none)
                            :player4 '("♦" "4" :none)}}
                  :round-points []
                  :turn 1
                  :round 2
                  :trump-card '("♣" "K" :king)
                  :new-round false
                  :round-suit "♦"}
      round1 1
      round2 2
      round1-points 2
      points 21
      cards-round2 '(("♦" "3" :none) ("♦" "A" :ace) ("♦" "7" :seven) ("♦" "4" :none))
      round2-ended (merge initial-db {:new-round true :round-end? true :round round2})]

  (deftest get-points-by-round-test
    (testing "get points by round function"
      (is (= round1-points
             (events/get-points-by-round initial-db round1)))))

  (deftest get-cards-points-test
    (testing "convert points"
      (is (= points (events/get-cards-points cards-round2)))))

  (deftest get-cards-list-by-round-test
    (testing "Context of the test assertions"
      (is (= cards-round2 (events/get-cards-list-by-round initial-db round2)))))

  (deftest get-winner-card-round-test
    (testing "return winner card"
      (is (= '("♦" "Q" 2) (events/get-winner-card-round initial-db 1)))
      (is (= '("♦" "A" 11) (events/get-winner-card-round initial-db 2)))
      (is (= '("♣" "4" 0) (events/get-winner-card-round initial-db 3)))
      (is (= '("♣" "6" 0) (events/get-winner-card-round initial-db 4)))
      (is (= '("♦" "6" 0) (events/get-winner-card-round initial-db 5)))))

  (deftest get-winner-player-round-test
    (testing "return winner player"
      (is (= :player2 (events/get-winner-player-round initial-db 2)))
      (is (= :player1 (events/get-winner-player-round initial-db 1)))
      (is (= :player4 (events/get-winner-player-round initial-db 3)))
      (is (= :player4 (events/get-winner-player-round initial-db 4)))
      (is (= :player2 (events/get-winner-player-round initial-db 5)))))

  (deftest set-round-end-test
    (testing "set round"
      (is (= round2-ended (events/set-round-end initial-db)))))

  (deftest convert-player-to-turn
    (testing "convert to number"
      (is (= 2 (events/convert-player-to-turn :player2))))))

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

(deftest get-major-card-test
  (testing "major card"
    (let [cards '(("♦" "7" :seven) ("♦" "Q" :queen))
          expected-card '("♦" "7" 10)
          receive-card (events/get-major-card cards)]
      (is (= expected-card receive-card))
      (is (= '("♥" "6" 0) (events/get-major-card '(("♥" "4" :none) ("♥" "6" :none))))))))

(deftest get-card-with-point-test
  (testing "card with point"
    (let [card '("♦" "A" :ace)
          expected-card '("♦" "A" 11)
          receive-card (events/get-card-with-point card)]
      (is (= expected-card receive-card)))))

(deftest is-trump-test
  (testing "is suit trump test"
    (let [initial-db  {:trump-card '("♣" "A" :ace)}
          card-1 '("♥" "A" :ace)
          card-2 '("♣" "6" :none)
          card-3 '("♣" "Q" :queen)
          card-4 '("♠" "3" :none)
          card-5 '("♦" "7" :seven)
          is-trump?  events/is-trump?]
      (do
        ;same suits as trump
        (are [attr func] (true? (func initial-db attr))
          card-2 is-trump?
          card-3 is-trump?)
        ;no trump suits
        (are [attr func] (not (true? (func initial-db attr)))
          card-1  is-trump?
          card-4  is-trump?
          card-5 is-trump?)))))

(deftest add-card-to-table-test
  (testing "selected card test"
    (let [initial-db {:round 1 :table {} :turn 1 :round-suit "♣" :new-round true}
          selected-card '("♥" "7" :seven)
          expected-db {:table {:round1 {:player1 '("♥" "7" :seven)}} :turn 1 :round 1 :new-round false :round-suit "♥"}
          new-db (events/add-card-to-table initial-db selected-card)]
      (is  (= expected-db new-db)))))

(deftest convert-card
  (testing ""
    (is (= '("♦" "A" :ace) (events/convert-card '("♦" "A" 11)))))) 


