(ns issue-tracker.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [clojure.data.json :as json]
            [issue-tracker.handler :refer :all]))

(defn parse-body [res]
  ;; With :key-fn the resulting vector is made a map
  (json/read-str (slurp (:body res)) :key-fn #(keyword %)))
  
(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "single issue"
    (let [response (app (request :get "/issues/123"))
          issue (parse-body response)]
      (is (= 200 (:status response)))
      (is (= "Issue 123" (:name issue)))))

  (testing "list of issues"
    (let [response (app (request :get "/issues"))
          issues (parse-body response)]
      (is (= 200 (:status response)))
      (is (= "Issue 1" (:name (first issues))))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response))))))
