(ns issue-tracker.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [reagent.core :as r :refer [atom]]
            [issue-tracker.core :as itc :refer [issues-atom
                                                new-issue
                                                add-issue-to-list
                                                issues-counter]]))
(defn setup []
  (reset! issues-atom {})
  (reset! issues-counter 0))

(deftest delete-issue-test
  (testing "Should delete a specific issue"
    (setup)
    
    ))

(deftest toggle-issue-test
  (testing "Should toggle a specific issue"
    (setup)
    (dotimes [n 3]
      (add-issue-to-list issues-atom))
    (println @issues-atom)
    (is (= (:completed (get-in @issues-atom [1]))
           false))
    (do
      (itc/toggle-issue issues-atom 1)
      (is (= (:completed (get-in @issues-atom [1]))
             true)))
    (do
      (itc/toggle-issue issues-atom 1)
      (is (= (:completed (get-in @issues-atom [1]))
             false)))
    (do
      (itc/toggle-issue issues-atom 1)
      (is (= (:completed (get-in @issues-atom [1]))
             true)))))

(deftest add-issue-to-list-test
  (testing "Should increment the issue-counter key"
    (setup)
    (is (= @issues-atom
           {}))
    (do
      (add-issue-to-list issues-atom)
      (is (= (count (keys @issues-atom))
             1)))
    (do
      (add-issue-to-list issues-atom)
      (is (= (count (keys @issues-atom))
             2)))))

;; (comment

;;   (keys (deref issue-tracker.core/issues-atom) )


;;   (for [key (keys (deref issue-tracker.core/issues-atom))]
;;        (println key) )

;;   (get-in (deref issue-tracker.core/issues-atom) [1])

;;   (swap! issue-tracker.core/issues-atom update-in [18 :completed] not)


;;   (def ppl (atom {"persons" {"joe" {:age 1}}}))

;;   (get-in @ppl ["persons" "joe"])

;;   (swap! ppl assoc-in ["persons" "joe"] {:age 11})
;;   )
