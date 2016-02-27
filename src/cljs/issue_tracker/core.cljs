(ns issue-tracker.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [issue-tracker.ajax :refer [load-interceptors!]]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(defn nav-link [uri title page collapsed?]
  [:ul.nav.navbar-nav>a.navbar-brand
   {:class (when (= page (session/get :page)) "active")
    :href uri
    :on-click #(reset! collapsed? true)}
   title])

(defn navbar []
  (let [collapsed? (r/atom true)]
    (fn []
      [:nav.navbar.navbar-light.bg-faded
       [:button.navbar-toggler.hidden-sm-up
        {:on-click #(swap! collapsed? not)} "☰"]
       [:div.collapse.navbar-toggleable-xs
        (when-not @collapsed? {:class "in"})
        [:a.navbar-brand {:href "#/"} [:div
                                       [:img{:width "30px" :src "img/orgmode-logo.png"}]
                                       "   re-issue"]]
        [:ul.nav.navbar-nav
         [nav-link "#/" "Home" :home collapsed?]
         [nav-link "#/about" "About" :about collapsed?]
         [nav-link "#/contact" "Contact" :contact collapsed?]]]])))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:p
      "This is the reference implementation of the Weng Issue Tracker"]]]])

(defn contact-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:p "Alain M. Lafon"]
     [:p
      [:a{:href "mailto:lafo@zhaw.ch"} "lafo@zhaw.ch"]]
     [:p "+41 76 40 50 567"]]]])

(defn priority-input []
  [:select.form-control {:field :list}
   [:option  "Priority"]
   [:option {:key :1} "1"]
   [:option {:key :2} "2"]])

(defn date-input [] 
  [:input{:type "date"}])

(def tmp-issue-name (r/atom nil))

(defn issue-input []
  [:input {:field :text :id :issue-name
           :placeholder "Enter your issue"
           :on-change #(reset! tmp-issue-name (-> % .-target .-value))}])

(def issues-atom (r/atom [{:name "initial issue"
                           :completed true
                           :date "28.02.2016"}]))

(defn create-issue-button []
  [:button.btn.btn-default
   {:on-click #(swap! issues-atom conj {:name @tmp-issue-name })}
   "Create Task"])

(defn issues-list []
  (fn []
    [:ul{:style {:list-style :none}}
     (for [issue @issues-atom]
       [:li
        [:div.row
         [:div.col-md-1
          [:input {:type :checkbox
                   ; TODO: This most likely does not yet update the
                   ; state in the issues-atom
                   :on-click #(swap! issue not (:completed issue))
                   :checked (if (:completed issue) "checked" "")}]]
         [:div.col-md-3
          (:date issue)]
         [:div.col-md-8
          (:name issue)]]])]))

(defn home-page []
  [:div.container
   [:div.jumbotron
    [:div.row
     [:div.col-md-3
      [priority-input]]
     [:div.col-md-3
      [date-input]]
     [:div.col-md-4
      [issue-input]]
     [:div.col-md-2
      [create-issue-button]]]
    [issues-list]]])

(def pages
  {:home #'home-page
   :contact #'contact-page
   :about #'about-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

(secretary/defroute "/contact" []
  (session/put! :page :contact))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
