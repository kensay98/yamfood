(ns yamfood.telegram.helpers.status
  (:require
    [morse.api :as t]
    [environ.core :refer [env]]
    [yamfood.core.orders.core :as o]
    [yamfood.telegram.translation.core :refer [translate]]))


(def token (env :bot-token))


(defn notify-order-accepted!
  [order-id]
  (let [order (o/order-by-id! order-id)
        lang (or (:lang order) :ru)]
    (t/send-text
      token
      (:tid order)
      {:parse_mode "markdown"}
      (translate lang :status-on-kitchen))))


(defn notify-order-canceled!
  ([order-id]
   (notify-order-canceled! order-id nil))
  ([order-id reason]
   (let [order (o/order-by-id! order-id)
         lang (or (:lang order) :ru)
         reason (or reason
                    (translate lang :status-canceled))]
     (t/send-text
       token
       (:tid order)
       {:parse_mode "markdown"}
       reason))))


(defn notify-order-on-way!
  [order-id]
  (let [order (o/order-by-id! order-id)
        lang (or (:lang order) :ru)]
    (t/send-text
      token
      (:tid order)
      {:parse_mode "markdown"}
      (translate lang :status-on-way))))
