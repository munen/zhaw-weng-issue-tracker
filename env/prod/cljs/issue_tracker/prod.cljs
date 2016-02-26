(ns issue-tracker.app
  (:require [issue-tracker.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
