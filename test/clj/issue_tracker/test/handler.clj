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

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response))))))
