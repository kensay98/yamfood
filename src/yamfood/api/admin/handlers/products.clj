(ns yamfood.api.admin.handlers.products
  (:require
    [compojure.core :as c]
    [yamfood.core.products.core :as products]))


(defn products-list
  [_]
  {:body (products/all-products!)})


(c/defroutes
  routes
  (c/GET "/" [] products-list))