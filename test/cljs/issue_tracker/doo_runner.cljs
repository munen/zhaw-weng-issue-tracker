(ns issue-tracker.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [issue-tracker.core-test]))

(doo-tests 'issue-tracker.core-test)

