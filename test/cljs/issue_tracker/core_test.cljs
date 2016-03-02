(ns issue-tracker.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [reagent.core :as r :refer [atom]]))

(defn setup []
  (def issues-counter (r/atom 0))

  (def new-issue (r/atom {:priority 1
                          :date (-> (js/Date.) .toISOString (.slice 0 10))}))

  (def issues-atom (r/atom []))


  (defn add-issue-to-list []
    (swap! issues-atom conj {:key @issues-counter
                             :name (:name @new-issue)
                             :priority (:priority @new-issue)
                             :date (:date @new-issue)})
    (swap! issues-counter inc))

  (swap! new-issue assoc :priority 1)
  (swap! new-issue assoc :date "2016-12-12")
  (swap! new-issue assoc :name "foo")
  (swap! new-issue assoc :completed false)
  )


(deftest toggle-issue
  (testing "Should toggle a specific issue"
    (setup)
    (is (= (:completed (first (issue-tracker.core/toggle-issue 0)))
           true))
    (is (= (:completed (first (issue-tracker.core/toggle-issue 0)))
           false))
    (is (= (:completed (first (issue-tracker.core/toggle-issue 0)))
           true))))
