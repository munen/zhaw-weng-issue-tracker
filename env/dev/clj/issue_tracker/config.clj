(ns issue-tracker.config
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [issue-tracker.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[issue_tracker started successfully using the development profile]=-"))
   :middleware wrap-dev})
