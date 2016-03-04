(ns issue-tracker.core
  (:require [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
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
 
(defn today []
  "Returns smth. like {:year 2016, :month 3, :day 3}"
  (zipmap [:year :month :day]
          (map js/parseInt (clojure.string/split (-> (js/Date.) .toISOString (.slice 0 10)) "-"))))

(defonce new-issue (r/atom {:priority 1
                            :completed false
                            :date (today)}))

(defonce issues-atom (r/atom {}))
; END DATA Definitions

; BEGIN DATA Manipulation Functions
(defn add-issue-to-list [d]
  "d is the atom that is to be updated"
  (swap! d merge {@issues-counter @new-issue})
  (swap! issues-counter inc))

(defn toggle-issue [d key]
  "d is the atom that is to be updated
   key is the id of the issue"
  (swap! issues-atom update-in [key :completed] not))

(defn delete-issue [d key]
  "d is the atom that is to be updated"
  (reset! d (dissoc @d key)))
; END DATA Manipulation Functions

(defn issues-list []
  (fn []
    [:ul{:style {:list-style :none}}
     (for [key (keys @issues-atom)]
       (let [issue (get-in @issues-atom [key])]
             [:li
              [:div.row
               [:div.col-md-1
                [:input {:type :checkbox
                         :on-click #(toggle-issue issues-atom key)
                         :checked (if (:completed issue) "checked" "")}]]
               [:div.col-md-3{:style {:text-decoration (if (:completed issue) :line-through :none)}}

                (let [date (:date issue)
                      year (:year date)
                      month (:month date)
                      day (:day date)]
                  (str year "-" month "-" day))]
               [:div.col-md-1
                ; TODO: Very likely, there's a built-in function for that
                (replace {":" "" } (str (:priority issue)))]
               [:div.col-md-6{:style {:text-decoration (if (:completed issue) :line-through :none)}}
                (:title issue)]
               [:div.col-md-1
                [:button.destroy {:on-click #(delete-issue issues-atom key)}
                 "x"]]]]))]))

(defn create-issue-button []
  [:button.btn.btn-default
   {:on-click #(add-issue-to-list issues-atom)}
   "Create"])


(def todo-form
  [:div.row
   [:div.col-md-1 
    [:select.form-control {:field :list :id :priority}
     [:option {:key :1} "1"]
     [:option {:key :2} "2"]
     [:option {:key :3} "3"]]]
   [:div.col-md-3
    [:div {:field :datepicker :id :date :format "yyyy-mm-dd"}]]
   [:div.col-md-7
    [:input {:field :text :id :title}]]
   [:div.col-md-1
    [create-issue-button]]])

(defn home-page []
  [:div.container
   [:div.jumbotron
    [bind-fields todo-form new-issue]]
   [issues-list]
   [:div.row
    [:div.col-md-6
     [:button.btn "Load from Server"]]
    [:div.col-md-6
     [:button.btn "Save to Server"]]]
   [:div.row
    [:label (str @new-issue)]]
   [:div.row
    [:label (str @issues-atom)]]])

(def pages
  {:home home-page
   :contact contact-page
   :about about-page})

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
