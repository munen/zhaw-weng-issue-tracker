(ns issue-tracker.routes.home
  (:require [issue-tracker.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/issues" [] {:body [{:name "Issue 1"
                            :priority 1
                            :date "2016-12-30"
                             :id 123}
                            {:name "Issue 2"
                            :priority 2
                            :date "2016-12-29"
                            :id 124}
                            ]})
  (GET "/issues/:id" [id] { :body { :name (str "Issue " id) } } )
  (GET "/docs" [] (response/ok (-> "docs/docs.md" io/resource slurp))))
