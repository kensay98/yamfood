(ns yamfood.core.baskets.core
  (:require
    [honeysql.core :as hs]
    [clojure.java.jdbc :as jdbc]
    [yamfood.core.db.core :as db]
    [yamfood.core.clients.core :as clients]
    [yamfood.core.products.core :as products]
    [yamfood.core.utils :as cu]))


(defn- basket-products-query
  [basket-id]
  (hs/format {:select    [:products.id
                          :basket_products.count
                          :products.name
                          :products.price
                          :categories.is_delivery_free
                          :products.energy]
              :from      [:basket_products :products]
              :where     [:and
                          [:= :basket_products.basket_id basket-id]
                          [:= :products.id :basket_products.product_id]]
              :left-join [:categories [:= :products.category_id :categories.id]]
              :order-by  [:id]}))


(defn- basket-totals-query
  [basket-id]
  (format "
    select
      coalesce(sum(basket_products.count * products.price), 0) as total_cost,
      coalesce(sum(basket_products.count * products.energy), 0) as total_energy
    from basket_products, products
    where basket_products.basket_id = %d and
          products.id = basket_products.product_id", basket-id))


(defn- basket-products!
  [basket-id]
  (->> (basket-products-query basket-id)
       (jdbc/query db/db)
       (map #(cu/keywordize-field % :name))))


(defn basket-totals!
  [basket-id]
  (->> (basket-totals-query basket-id)
       (jdbc/query db/db)
       (first)))


(defn basket-state!
  [basket-id]
  (let [basket (basket-totals! basket-id)]
    (assoc basket :products (basket-products! basket-id))))


(defn increment-product-query
  [basket-id product-id]
  (hs/format {:update :basket_products
              :set    {:count (hs/raw "count + 1")}
              :where  [:and
                       [:= :basket_id basket-id]
                       [:= :product_id product-id]]}))


(defn decrement-product-query
  [basket-id product-id]
  (hs/format {:update :basket_products
              :set    {:count (hs/raw "count - 1")}
              :where  [:and
                       [:= :basket_id basket-id]
                       [:= :product_id product-id]]}))


(defn increment-product-in-basket!
  [basket-id product-id]
  (->> (increment-product-query basket-id product-id)
       (jdbc/execute! db/db))
  (products/state-for-product-detail! basket-id product-id))


(defn decrement-product-in-basket!
  [basket-id product-id]
  (->> (decrement-product-query basket-id product-id)
       (jdbc/execute! db/db))
  (products/state-for-product-detail! basket-id product-id))


(defn add-product-to-basket!
  ([basket-id product-id]
   (add-product-to-basket! basket-id product-id []))
  ([basket-id product-id modifiers]
   (let [basket-product (-> (jdbc/insert! db/db "basket_products"
                                          {:product_id product-id
                                           :basket_id  basket-id
                                           :payload    {:modifiers modifiers}})
                            (first))]
     (products/state-for-product-detail! basket-id (:product_id basket-product)))))


(defn order-confirmation-state!
  [basket-id]
  {:basket (basket-state! basket-id)
   :client (clients/client-with-basket-id! basket-id)})


(defn clear-basket!
  [basket-id]
  (jdbc/delete! db/db "basket_products" ["basket_id = ?" basket-id]))
