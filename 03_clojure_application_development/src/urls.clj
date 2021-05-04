(ns urls
  (:require [clojure.java.jdbc :as jdbc]
            [db.core :refer [connection]]
            [utils.errors :refer [not-found]]
            [clojure.string :as str]
            [ring.util.codec :as ring]
            [clojure.walk :as map])
  (:import (java.util UUID)
           (org.postgresql.util PSQLException)))

(defn get-url-by-id
  "Gets the url from the database with the given id, or nil if no such
   url exists."
  [id]
  (let [query ["SELECT * FROM urls WHERE id = ?" id]
        result (jdbc/query connection query)]
    (first result)))

(defn get-url-handler
  [req]
  (let [id (get-in req [:params :id])
        url (get-url-by-id id)]
    (if-not url
      (throw (not-found))
      {:status 200 :body url})))

(defn create-url!
  "Given a url as a string, creates a url row in the database and returns
   the created row."
  ([url] (create-url! url (.toString (UUID/randomUUID))))
  ([url id]
   (let [row {:url url :id id}
         result (jdbc/insert! connection :urls row)]
     (first result))))

(defn create-url-handler
  [req]
  (let [url (get-in req [:body :url])
        query-params (when (contains? req :query-string)
                       (map/keywordize-keys (ring/form-decode (:query-string req))))
        id (:id query-params)]
    (try
      (if (nil? id)
        (let [row (create-url! url)]
          {:status 201
           :body row})
        (let [row (create-url! url id)]
          {:status 201
           :body row}))
      (catch PSQLException e
        (hash-map :status 409
                  :body (str (.getMessage e)))))))

(defn delete-by-id! [id]
  (let [result (jdbc/delete! connection :urls ["id = ?" id])]
    (prn "result: " result)))



(defn delete-url-handler
  [req]
  (let [query-params (when (contains? req :query-string)
                       (map/keywordize-keys (ring/form-decode (:query-string req))))
        id (:id query-params)]
    (if (or (nil? query-params) (nil? id))
      {:status 400}
      (try
        (delete-by-id! id)
        {:status 200}))))


