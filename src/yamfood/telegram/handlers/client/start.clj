(ns yamfood.telegram.handlers.client.start
  (:require
    [environ.core :refer [env]]
    [yamfood.core.products.core :as p]
    [yamfood.telegram.dispatcher :as d]
    [yamfood.core.clients.core :as clients]
    [yamfood.telegram.handlers.utils :as u]
    [yamfood.telegram.handlers.client.core :as c]
    [yamfood.telegram.translation.core :refer [translate]]))


(defn start-handler
  [ctx]
  (if (and (:client ctx)
           (:phone (:client ctx)))
    {:dispatch {:args [:c/menu]}}
    {:dispatch {:args [:c/registration]}}))


(defn categories-reducer
  [lang]
  (fn [r category]
    (let [current (last r)
          c (count current)
          emoji (:emoji category)
          text (str emoji " " (u/translated lang (:name category)))
          next {:text text :switch_inline_query_current_chat emoji}]
      (cond
        (= c 1) (conj (pop r) [(first current) next])
        :else (conj r [next])))))


(defn categories-in-markup
  [lang categories]
  (reduce (categories-reducer lang) [] categories))


(defn menu-markup
  [lang categories]
  (let []
    {:inline_keyboard
     (conj (apply conj []
                  (if (empty? categories)
                    [[{:text                             (translate lang :menu-button)
                       :switch_inline_query_current_chat ""}]]
                    (categories-in-markup lang categories)))
           [{:text          (translate lang :invalid-location-basket-button)
             :callback_data "basket"}]
           [{:text          (translate lang :settings-button)
             :callback_data "settings"}])}))


(defn menu-handler
  ([ctx]
   (let [client (:client ctx)
         location (get-in client [:payload :location])]
     (if location
       {:run {:function   p/categories-with-products!
              :args       [(:id (:bot ctx))]
              :next-event :c/menu}}
       {:run      {:function clients/update-payload!
                   :args     [(:id client)
                              (assoc (:payload client) :step u/menu-step)]}
        :dispatch {:args [:c/request-location]}})))
  ([ctx categories]
   (let [client (:client ctx)
         lang (:lang ctx)
         update (:update ctx)
         chat-id (u/chat-id update)
         query (:callback_query update)]
     (merge
       {:send-text {:chat-id chat-id
                    :options {:reply_markup (menu-markup lang categories)}
                    :text    (translate lang :hello-message)}
        :run       {:function clients/update-payload!
                    :args     [(:id client)
                               (assoc (:payload client) :step u/menu-step)]}}
       (when query
         {:delete-message {:chat-id    chat-id
                           :message-id (:message_id (:message query))}})))))


(defn registration-handler
  [ctx]
  (let [update (:update ctx)
        tid (u/chat-id update)
        bot-id (get-in ctx [:bot :id])
        from (:from (:message update))
        name (str (:first_name from) " " (:last_name from))
        utm (u/utm update)]
    (if (:client ctx)
      {:dispatch {:args [:c/request-phone]}}

      {:run      {:function clients/create-client!
                  :args     [tid bot-id name (if utm {:utm utm} {})]}
       :dispatch {:args        [:c/request-phone]
                  :rebuild-ctx {:function c/build-ctx!
                                :update   (:update ctx)
                                :token    (:token ctx)}}})))


(d/register-event-handler!
  :c/start
  start-handler)


(d/register-event-handler!
  :c/registration
  registration-handler)


(d/register-event-handler!
  :c/menu
  menu-handler)
