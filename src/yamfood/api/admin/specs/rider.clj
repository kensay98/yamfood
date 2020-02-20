(ns yamfood.api.admin.specs.rider
  (:require
    [clojure.spec.alpha :as s]
    [yamfood.core.specs.core :as cs]))


(s/def ::name string?)

(s/def ::rider
  (s/keys :req-un [::cs/phone ::name]))
