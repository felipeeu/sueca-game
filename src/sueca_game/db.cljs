(ns sueca-game.db)

(def default-db
  {:name "re-frame"
   :players-quantity 4
   :cards {:values '(("2" :none) ("3" :none) ("4" :none) ("5" :none) ("6" :none) ("Q" :queen) ("J" :jack) ("K" :king) ("7" :seven) ("A" :ace))
           :suits {:spade "♠"
                   :heart "♥"
                   :diamond "♦"
                   :club "♣"}}
   :round-points []
   :turn 1
   :round 1})

