(ns issue-tracker.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[issue_tracker started successfully]=-"))
   :middleware identity})
