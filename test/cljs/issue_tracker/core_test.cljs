(ns issue-tracker.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [reagent.core :as r :refer [atom]]
            [issue-tracker.core :as itc :refer [issues-atom
                                                new-issue
                                                delete-issue
                                                toggle-issue
                                                add-issue-to-list
                                                issues-counter]]))
(defn setup []
  (reset! issues-atom {})
  (reset! issues-counter 0))

(deftest delete-issue-test
  (testing "Should delete a specific issue"
    (setup)
    (is (= (count (keys @issues-atom))
           0))
    (do
      (dotimes [n 3]
        (add-issue-to-list issues-atom))
      (is (= (keys @issues-atom)
             [0 1 2]))
    (do
      (delete-issue issues-atom 0)
      (is (= (keys @issues-atom)
             [ 1 2]))))))

(deftest toggle-issue-test
  (testing "Should toggle a specific issue"
    (setup)
    (dotimes [n 3]
      (add-issue-to-list issues-atom))
    (is (= (:completed (get-in @issues-atom [1]))
           false))
    (do
      (toggle-issue issues-atom 1)
      (is (= (:completed (get-in @issues-atom [1]))
             true)))
    (do
      (toggle-issue issues-atom 1)
      (is (= (:completed (get-in @issues-atom [1]))
             false)))
    (do
      (toggle-issue issues-atom 1)
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
