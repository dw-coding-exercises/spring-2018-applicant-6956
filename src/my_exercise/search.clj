(ns my-exercise.search
  (:require [hiccup.page :refer [html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [my-exercise.us-state :as us-state]
            [clojure.string :as string]
            [clj-http.client :as client]))

(defn build-state-id [state]
;; When the state string is not empty build the ocd-id for the state
  (when-not (empty? state)
            (str "ocd-division/country:us/state:" (string/lower-case state))))

(defn build-place-id [state state-id city]
;; When the city and the state strings are not empty build the place id
  (let [formatted-city (string/lower-case (string/replace (string/trim city) " " "_"))]
  (when-not (or (empty? city) (empty? state))
            (str state-id "/place:" formatted-city))))

(defn page [query]
   (let [params (:form-params query)
         us-ocd-id "ocd-division/country:us" 
         
         state (params "state")
         state-ocd-id (build-state-id state)
         
         city (params "city")
         place-ocd-id (build-place-id state state-ocd-id city)
         
         ;; Join the ocd-ids for sending to the api
         ids (string/join "," (remove nil? [us-ocd-id state-ocd-id place-ocd-id])) 

         url "https://api.turbovote.org/elections/upcoming"
         response (client/get url {:query-params {:district-divisions ids}})
         ;; This could use some error handling for the api call but I am out of time. 
         elections (read-string (:body response))] 

    ;; Print out the description of all of the up coming elections for the given address.
    ;; This would be better if it was more interactive, such as clickable so the user can get more
    ;; info about the elections they are interested in.
    (html5
     [:div (if (not-empty elections)
               [:div (str "Up coming elections for: "
                      city ", " state)
                     (for [e elections]
                          [:div (str (:description e))])]
               [:div "No elections found"])])))
