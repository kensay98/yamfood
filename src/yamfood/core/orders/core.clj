(ns yamfood.core.orders.core
  (:require
    [honeysql.core :as hs]
    [honeysql.helpers :as hh]
    [yamfood.core.utils :as cu]
    [clojure.java.jdbc :as jdbc]
    [yamfood.core.db.core :as db]
    [yamfood.core.clients.core :as clients]
    [yamfood.telegram.handlers.utils :as u]))


(def order-statuses
  {:new        "new"
   :on-kitchen "onKitchen"
   :on-way     "onWay"
   :finished   "finished"
   :canceled   "canceled"})


(def active-order-statuses
  [(:new order-statuses)
   (:on-kitchen order-statuses)
   (:on-way order-statuses)])


(def ended-order-statuses
  [(:canceled order-statuses)
   (:finished order-statuses)])


(def cancelable-order-statuses
  [(:new order-statuses)])


(defn fmt-order-location
  [order]
  (assoc order
    :location
    (db/point->clj (:location order))))


(defn- order-totals-query
  [order-id]
  {:select [(hs/raw "coalesce(sum(order_products.count * products.price), 0) as total_cost")
            (hs/raw "coalesce(sum(order_products.count * products.energy), 0) as total_energy")]
   :from   [:order_products :products]
   :where  [:and
            [:= :order_products.order_id order-id]
            [:= :products.id :order_products.product_id]]})


(defn order-total-sum-query
  [order-id]
  {:select [(hs/raw "coalesce(sum(order_products.count * products.price), 0) as total_cost")]
   :from   [:order_products :products]
   :where  [:and
            [:= :order_products.order_id order-id]
            [:= :products.id :order_products.product_id]]})


(defn order-totals!
  [order-id]
  (->> (order-totals-query order-id)
       (hs/format)
       (jdbc/query db/db)
       (first)))


(defn- products-from-basket-query
  [basket-id]
  {:select    [:basket_products.product_id
               :basket_products.count
               :categories.is_delivery_free]
   :from      [:basket_products :products]
   :where     [:and
               [:= :basket_products.basket_id basket-id]
               [:= :basket_products.product_id :products.id]]
   :left-join [:categories
               [:= :products.category_id :categories.id]]})


(defn order-current-status-query
  [order-id]
  {:select   [:order_logs.status]
   :from     [:order_logs]
   :where    [:= :order_logs.order_id order-id]
   :order-by [[:order_logs.created_at :desc]]
   :limit    1})


(def order-detail-query
  {:select    [:orders.id
               :orders.location
               :orders.payment
               [:bots.id :bot_id]
               [:bots.name :bot_name]
               [:kitchens.id :kitchen_id]
               [:kitchens.name :kitchen]
               [:kitchens.payload :kitchen_payload]
               :orders.created_at
               [:clients.id :client_id]
               :clients.tid
               :clients.name
               :clients.phone
               [(hs/raw "clients.payload->'lang'") :lang]
               [:riders.name :rider_name]
               [:riders.phone :rider_phone]
               [(order-total-sum-query :orders.id) :total_sum]
               [(order-current-status-query :orders.id) :status]
               :orders.comment
               :orders.notes
               :orders.delivery_cost
               :orders.rate
               :orders.address]
   :from      [:orders]
   :left-join [:riders [:= :orders.rider_id :riders.id]
               :clients [:= :orders.client_id :clients.id]
               :kitchens [:= :orders.kitchen_id :kitchens.id]
               :bots [:= :clients.bot_id :bots.id]]
   :order-by  [:orders.id]})


(def order-products-query
  {:select    [:products.id
               :products.name
               :products.price
               :products.payload
               :order_products.comment
               :order_products.count
               :categories.is_delivery_free
               [(hs/call :* :order_products.count :products.price) :total]]
   :from      [:order_products :products]
   :left-join [:categories [:= :categories.id :products.category_id]]
   :where     [:= :order_products.product_id :products.id]})


(defn products-by-order-id-query
  [order-id]
  (hs/format (hh/merge-where
               order-products-query
               [:= :order_products.order_id order-id])))


(defn products-by-order-id!
  [order-id]
  (let [keywordize-fn (fn [product]
                        (-> product
                            (cu/keywordize-field)
                            (cu/keywordize-field :name)))]
    (->> (products-by-order-id-query order-id)
         (jdbc/query db/db)
         (map keywordize-fn))))


(defn add-products!
  [order]
  (assoc order :products (products-by-order-id! (:id order))))


(defn orders-by-client-id-query
  [client-id]
  (hs/format (hh/merge-where order-detail-query [:= :orders.client_id client-id])))


(def active-orders-query
  {:with   [[:cte_orders order-detail-query]]
   :select [:*]
   :from   [:cte_orders]
   :where  [:in :cte_orders.status active-order-statuses]})


(defn client-finished-orders!
  [client-id]
  (->> (-> {:with   [[:cte_orders order-detail-query]]
            :select [:%count.cte_orders.id]
            :from   [:cte_orders]
            :where  [:and
                     [:= :cte_orders.client_id client-id]
                     [:= :cte_orders.status (:finished order-statuses)]]}
           (hs/format))
       (jdbc/query db/db)
       (first)
       :count))


(defn client-canceled-orders!
  [client-id]
  (->> (-> {:with   [[:cte_orders order-detail-query]]
            :select [:%count.cte_orders.id]
            :from   [:cte_orders]
            :where  [:and
                     [:= :cte_orders.client_id client-id]
                     [:= :cte_orders.status (:canceled order-statuses)]]}
           (hs/format))
       (jdbc/query db/db)
       (first)
       :count))


(defn active-orders!
  []
  (->> active-orders-query
       (hs/format)
       (jdbc/query db/db)
       (map fmt-order-location)))


(defn ended-orders-query
  [offset limit]
  {:with   [[:cte_orders order-detail-query]]
   :select [:*]
   :from   [:cte_orders]
   :where  [:in :cte_orders.status ended-order-statuses]
   :offset offset
   :limit  limit})


(defn ended-orders!
  ([]
   (ended-orders! 0 100))
  ([offset limit]
   (ended-orders! offset limit nil))
  ([offset limit where]
   (->> (-> (ended-orders-query offset limit)
            (assoc :order-by [[:cte_orders.id :desc]])
            (hh/merge-where where))
        (hs/format)
        (jdbc/query db/db)
        (map fmt-order-location))))


(defn ended-orders-count!
  ([]
   (ended-orders-count! nil))
  ([where]
   (->> (-> (ended-orders-query 0 100)
            (dissoc :limit)
            (dissoc :offset)
            (assoc :select [:%count.cte_orders.id])
            (hh/merge-where where))
        (hs/format)
        (jdbc/query db/db)
        (first)
        (:count))))


(defn active-order-by-rider-id-query
  [rider-id]
  (hs/format
    {:with   [[:cte_orders (hh/merge-where
                             order-detail-query
                             [:= :orders.rider_id rider-id])]]
     :select [:*]
     :from   [:cte_orders]
     :where  [:= :cte_orders.status " on-way "]}))


(defn active-order-by-rider-id!
  [rider-id]
  (let [order (->> (active-order-by-rider-id-query rider-id)
                   (jdbc/query db/db)
                   (first))]
    (when order
      (fmt-order-location order))))


(defn order-by-id-query
  [order-id]
  (hs/format (hh/merge-where order-detail-query [:= :orders.id order-id])))


(def order-by-id-options {:products? true :totals? true})
(defn order-by-id!
  ([order-id]
   (order-by-id! order-id order-by-id-options))
  ([order-id options]
   (let [options (merge options order-by-id-options)
         order (->> (order-by-id-query order-id)
                    (jdbc/query db/db)
                    (map fmt-order-location)
                    (map #(cu/keywordize-field % :kitchen_payload))
                    (first))
         products? (:products? options)
         totals? (:totals? options)]
     (when order
       (merge (if totals? (order-totals! order-id) {})
              (if products? (add-products! order) {})
              order)))))


(defn orders-by-client-id!
  [client-id]
  (->> (orders-by-client-id-query client-id)
       (jdbc/query db/db)
       (map fmt-order-location)))


(defn accept-order!
  [order-id admin-id]
  (jdbc/insert!
    db/db "order_logs"
    {:order_id order-id
     :status   (:on-kitchen order-statuses)
     :payload  {:admin_id admin-id}}))


(defn cancel-order!
  [order-id]
  (jdbc/insert!
    db/db "order_logs"
    {:order_id order-id
     :status   (:canceled order-statuses)}))


(defn client-active-order!
  [client-id]
  (-> (orders-by-client-id! client-id)
      (last)
      (#(order-by-id! (:id %)))))


(defn insert-order-query
  [client-id lon lat address comment kitchen-id payment delivery_cost]
  ["insert into orders (client_id, location, address, comment, kitchen_id, payment, delivery_cost) values (?, POINT (?, ?), ?, ?, ?, ?, ?) ;"
   client-id
   lon lat
   address
   comment
   kitchen-id
   payment
   delivery_cost])


(defn insert-order!
  [client-id lon lat address comment kitchen-id payment delivery-cost]
  (let [query (insert-order-query client-id
                                  lon lat
                                  address
                                  comment
                                  kitchen-id
                                  payment
                                  delivery-cost)]
    (jdbc/execute! db/db query {:return-keys ["id"]})))


(defn create-order!
  ; TODO: Use transaction!
  [basket-id kitchen-id location address comment payment default-delivery-cost]
  (let [client (clients/client-with-basket-id! basket-id)
        products (->> (products-from-basket-query basket-id)
                      (hs/format)
                      (jdbc/query db/db))
        delivery-cost (if (every? false? (map :is_delivery_free products))
                        default-delivery-cost
                        0)
        order-id (:id (insert-order! (:id client)
                                     (:longitude location)
                                     (:latitude location)
                                     address
                                     comment
                                     kitchen-id
                                     payment
                                     delivery-cost))]
    (->> products
         (map #(select-keys % [:product_id :count]))
         (map #(assoc % :order_id order-id))
         (jdbc/insert-multi! db/db "order_products"))
    (when (= payment u/cash-payment)
      (jdbc/insert!
        db/db "order_logs"
        {:order_id order-id
         :status   (:new order-statuses)}))
    order-id))


(defn pay-order!
  [order-id payment-id]
  (jdbc/with-db-transaction
    [t-con db/db]
    (jdbc/update! t-con "orders"
                  {:is_payed true}
                  ["id = ?" order-id])
    (jdbc/insert! t-con "order_logs"
                  {:order_id order-id
                   :status   (:new order-statuses)
                   :payload  {:payment_id payment-id}})))


(defn update-order-products!
  ([order-id products]
   (update-order-products! db/db order-id products))
  ([c order-id products]
   (let [products (map
                    #(assoc % :order_id order-id)
                    products)]
     (jdbc/with-db-transaction
       [t-con c]
       (jdbc/delete!
         t-con
         "order_products"
         ["order_products.order_id = ?" order-id])
       (jdbc/insert-multi!
         t-con
         "order_products"
         products)))))


(defn update!
  [order-id order]
  (let [products (:products order)
        order (dissoc order :products)]
    (jdbc/with-db-transaction
      [t-con db/db]
      (when products
        (update-order-products! t-con order-id products))
      (when (seq order)
        (jdbc/update!
          t-con
          "orders"
          order
          ["orders.id = ?" order-id])))))
