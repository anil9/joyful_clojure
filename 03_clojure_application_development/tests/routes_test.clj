(ns routes-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [test-helpers :refer [with-database-reset]]
            [app :refer [app]]
            [db.core :refer [connection]]))

(use-fixtures :each with-database-reset)

(deftest test-get-url-route
  (testing "should respond with a 404 if the specified url does not exist"
    (let [req {:request-method :get
               :uri            "/urls/98765"}
          res (app req)]
      (is (= (:status res)
             404))))
  (testing "should respond with the url if it exists."
    (let [req {:request-method :get
               :uri            "/urls/12345"}
          res (app req)
          body (json/read-str (:body res)
                              :key-fn keyword)]
      (is (= {:id "12345" :url "https://someawesomewebsite.com"}
             body)))))

(deftest test-create-url-route
  (testing "should respond with 201 and the created entity when creation is successful"
    (let [req {:request-method :post
               :uri            "/urls"
               :body           {:url "https://yetanotherwebsite.com"}}
          res (app req)
          debug (print res)
          body (json/read-str (:body res)
                              :key-fn keyword)]
      (is (= 201 (:status res)))
      (is (= "https://yetanotherwebsite.com" (:url body)))
      (is (string? (:id body)))))
  (testing "should create the url in the database"
    (let [req {:request-method :post
               :uri            "/urls"
               :body           {:url "https://website4you.com"}}
          res (app req)
          query ["SELECT * FROM urls WHERE url = ?" "https://website4you.com"]
          result (jdbc/query connection query)
          created-entity (first result)]
      (is (= "https://website4you.com" (:url created-entity)))))
  (testing "should create the url with the provided ID in the database"
    (let [req {:request-method :post
               :uri            "/urls"
               :body           {:url "https://website2you.com"}
               :query-string   "id=myId"}
          res (app req)
          query ["SELECT * FROM urls WHERE id = ?" "myId"]
          result (jdbc/query connection query)
          created-entity (first result)]
      (is (= "https://website2you.com" (:url created-entity)))))
  (testing "should respond with 409 (conflict) when id already exists"
    (let [req {:request-method :post
               :uri            "/urls"
               :body           {:url "https://website2you.com"}
               :query-string   "id=amysdIdsddd"}
          result (map (fn [_] (app req)) (range 2))
          created-response (first result)
          error-response (second result)]
      (is (= 201 (:status created-response)))
      (is (= 409 (:status error-response)))))
  (testing "should delete an existing URL"
    (let [create-req {:request-method :post
                      :uri            "/urls"
                      :body           {:url "https://website2you.com"}
                      :query-string   "id=deleteMe"}
          delete-req {:request-method :delete
                      :uri            "/urls"
                      :query-string   "id=deleteMe"}
          _ (app create-req)
          res (app delete-req)
          query ["SELECT COUNT(*) FROM urls WHERE id = ?" "deleteMe"]
          result (jdbc/query connection query)]
      (is (= 200 (:status res)))
      (is (= 0 (:count (first result))))))
  (testing "should respond with status 400 when delete is missing an id param"
    (let [delete-req {:request-method :delete
                      :uri            "/urls"}
          res (app delete-req)]
      (is (= 400 (:status res))))))
