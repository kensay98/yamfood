(ns yamfood.core.orders.core
  (:require
    [honeysql.core :as hs]
    [honeysql.helpers :as hh]
    [clojure.java.jdbc :as jdbc]
    [yamfood.core.db.core :as db]
    [yamfood.core.baskets.core :as b]
    [yamfood.core.users.core :as users]))


(def order-statuses
  {:new "new"})


(defn- products-from-basket-query
  [basket-id]
  (hs/format {:select [:product_id :count]
              :from   [:basket_products]
              :where  [:= :basket_id basket-id]}))


(def order-detail-query
  {:select   [:orders.id
              :orders.location
              :orders.comment]
   :from     [:orders]
   :order-by [:id]})


(def order-products-query
  {:select [:products.name
            :products.price
            :order_products.count]
   :from   [:order_products :products]
   :where  [:= :order_products.product_id :products.id]})


(defn products-by-order-id-query
  [order-id]
  (hs/format (hh/merge-where
               order-products-query
               [:= :order_products.order_id order-id])))


(defn products-by-order-id!
  [order-id]
  (->> (products-by-order-id-query order-id)
       (jdbc/query db/db)))


(defn orders-by-user-id-query
  [user-id]
  (hs/format (hh/merge-where order-detail-query [:= :orders.user_id user-id])))


(defn fmt-order-location
  [order]
  (assoc order
    :location
    (db/point->clj (:location order))))


(defn orders-by-user-id!
  [user-id]
  (->> (orders-by-user-id-query user-id)
       (jdbc/query db/db)
       (map fmt-order-location)))


(defn add-products
  [order]
  (assoc order :products (products-by-order-id! (:id order))))


(defn user-active-order!
  [user-id]
  (-> (orders-by-user-id! user-id)
      (last)
      (add-products)))


(defn products-from-basket!
  [basket-id]
  (->> (products-from-basket-query basket-id)
       (jdbc/query db/db)))


(defn prepare-basket-products-to-order
  [basket-products order-id]
  (map #(assoc % :order_id order-id) basket-products))


(defn insert-order-query
  [user-id lat lon comment]
  ["insert into orders (user_id, location, comment) values (?, POINT(?, ?), ?);"
   user-id
   lon lat
   comment])


(defn insert-order!
  [user-id lon lat comment]
  (let [query (insert-order-query user-id
                                  lon lat
                                  comment)]
    (jdbc/execute! db/db query {:return-keys ["id"]})))


(defn insert-products!
  [products]
  (jdbc/insert-multi! db/db "order_products" products))


(defn create-order-and-clear-basket!
  [basket-id location comment]
  (let [user (users/user-with-basket-id! basket-id)
        order (insert-order! (:id user)
                             (:longitude location)
                             (:latitude location)
                             comment)]
    (-> (products-from-basket! basket-id)
        (prepare-basket-products-to-order (:id order))
        (insert-products!))
    (jdbc/insert!
      db/db "order_logs"
      {:order_id (:id order)
       :status   (:new order-statuses)})
    (b/clear-basket! basket-id)))
