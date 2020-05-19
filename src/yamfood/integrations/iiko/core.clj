(ns yamfood.integrations.iiko.core
  (:require
    [environ.core :refer [env]]
    [yamfood.core.params.core :as p]
    [yamfood.integrations.iiko.api :as api]
    [yamfood.core.products.core :as products]
    [yamfood.integrations.iiko.utils :as utils]))


(defn iiko-context!
  [params access-token organization-id]
  (let [iiko-payment-types (:paymentTypes
                             (api/iiko-payment-types! access-token
                                                      organization-id))]
    {:access-token    access-token
     :organization-id organization-id
     :city            "Ташкент"
     :delivery-id     (:iiko-delivery-id params)
     :delivery-cost   (:delivery-cost params)
     :home            (:iiko-home params)
     :street          (:iiko-street params)
     :payments        {:cash (:id (first (filter #(= (:code %) "CASH") iiko-payment-types)))
                       :card (:id (first (filter #(= (:code %) "PAYME") iiko-payment-types)))}}))


(defn create-order!
  [order]
  (let [params (p/params!)
        access-token (api/get-access-token! (:iiko-user-id params)
                                            (:iiko-user-secret params))
        organization-id (-> (api/organizations! access-token)
                            (first)
                            :id)
        context (iiko-context! params access-token organization-id)
        order (utils/order->iiko context order)]
    (api/create-order! access-token order)))


(defn check-order!
  [order]
  (let [params (p/params!)
        access-token (api/get-access-token! (:iiko-user-id params)
                                            (:iiko-user-secret params))]
    (api/check-order! access-token order)))


(comment
  (def nomenclature
    (let [params (p/params!)
          access-token (api/get-access-token! (:iiko-user-id params)
                                              (:iiko-user-secret params))
          organization-id (-> (api/organizations! access-token)
                              (first)
                              :id)]
      (api/nomenclature! access-token organization-id)))


  (let [params (p/params!)
        access-token (api/get-access-token! (:iiko-user-id params)
                                            (:iiko-user-secret params))
        organization-id (-> (api/organizations! access-token)
                            (first)
                            :id)]
    (api/delivery-terminals! access-token organization-id))


  (map utils/iiko->product (utils/products nomenclature "16c0bb0c-ad96-41ee-84f4-2f5ddcdea86c"))

  (map utils/iiko->modifier (utils/modifiers nomenclature)))

