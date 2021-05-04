(ns routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [urls]))

(defroutes root-handler
  (GET "/urls/:id" [] urls/get-url-handler)
  (POST "/urls" [] urls/create-url-handler)
  (DELETE "/urls" [] urls/delete-url-handler)
  (route/not-found "Not found"))
