(ns issue-tracker.test.db.core
  (:require [issue-tracker.db.core :refer [*db*] :as db]
            [issue-tracker.db.migrations :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [config.core :refer [env]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'issue-tracker.db.core/*db*)
    (migrations/migrate ["migrate"])
    (f)))

(deftest test-issues
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (testing "generated functions are working"
      (is (= 1 (db/create-issue!
                t-conn
                {:id         1
                 :title      "Test Issue 1"})))
      (is (= {:id         1
              :title      "Test Issue 1"}
             (db/get-issue t-conn {:id 1}))))))
