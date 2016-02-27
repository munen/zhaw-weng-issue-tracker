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

; BEGIN DATA Definitions
(defonce issues-counter (r/atom 0))

(def new-issue (r/atom {:priority 1
                        ; Today
                        :date (-> (js/Date.) .toISOString (.slice 0 10))
                        }))

(defonce issues-atom (r/atom []))
; END DATA Definitions

; BEGIN DATA Manipulation Functions
(defn add-issue-to-list []
  (swap! issues-atom conj {:key @issues-counter
                           :name (:name @new-issue)
                           :priority (:priority @new-issue)
                           :date (:date @new-issue)})
  (swap! issues-counter inc))

(defn toggle-issue [key]
  (swap! issues-atom update-in [key :completed] not))

(defn delete-issue [key]
  (reset! issues-atom (filter #(not (= key (:key %))) @issues-atom)))
; END DATA Manipulation Functions


(defn priority-input []
  [:select.form-control {:field :list
                         :on-change #(swap! new-issue assoc :priority (-> % .-target .-value))}
   [:option {:key :1} "1"]
   [:option {:key :2} "2"]
   [:option {:key :3} "3"]])

(defn date-input [] 
  [:input{:type "date"
          :value (:date @new-issue)
          :on-change #(swap! new-issue assoc :date (-> % .-target .-value))}])

(defn issue-input []
  [:input {:field :text :id :issue-name
           :placeholder "Enter your issue"
           :on-change #(swap! new-issue assoc :name (-> % .-target .-value))}])

(defn create-issue-button []
  [:button.btn.btn-default
   {:on-click #(add-issue-to-list)}
   "Create Task"])

(defn issues-list []
  (fn []
    [:ul{:style {:list-style :none}}
     (for [issue @issues-atom]
       [:li
        [:div.row
         [:div.col-md-1
          [:input {:type :checkbox
                   :on-click #(toggle-issue (:key issue))
                   :checked (if (:completed issue) "checked" "")}]]
         [:div.col-md-3{:style {:text-decoration (if (:completed issue) :line-through :none)}}

          (:date issue)]
         [:div.col-md-1
          (:priority issue)]
         [:div.col-md-6{:style {:text-decoration (if (:completed issue) :line-through :none)}}
          (:name issue)]
         [:div.col-md-1
          [:button.destroy {:on-click #(delete-issue (:key issue))}
           "x"]]]])]))

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
    [issues-list]]
    [issues-list]])

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
