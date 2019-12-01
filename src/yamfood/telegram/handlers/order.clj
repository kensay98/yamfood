(ns yamfood.telegram.handlers.order
  (:require [yamfood.telegram.dispatcher :as d]
            [yamfood.core.users.core :as users]
            [yamfood.telegram.handlers.utils :as u]
            [yamfood.core.users.basket :as b]))


(def request-location-markup
  {:reply_markup
   {:resize_keyboard true
    :keyboard        [[{:text             "Отправить текущее положение"
                        :request_location true}]]}})


(defn request-location
  [_ update]
  (let [query (:callback_query update)
        chat-id (:id (:from query))
        message-id (:message_id (:message query))]
    {:send-text      {:chat-id chat-id
                      :text    "Куда доставить?"
                      :options request-location-markup}
     :delete-message {:chat-id    chat-id
                      :message-id message-id}}))


(defn make-order-state
  [ctx update]
  (let [user (:user ctx)]
    {:core {:function    #(assoc
                            (b/make-order-state! (:basket_id user))
                            :user user)
            :on-complete #(d/dispatch
                            ctx
                            [:send-order-detail update %])}}))


(defn handle-to-order
  [ctx update]
  (let [query (:callback_query update)
        chat-id (:id (:from query))
        user (:user ctx)
        message-id (:message_id (:message query))]
    (into
      (cond
        (:location user) (make-order-state ctx update)
        :else {:send-text {:chat-id chat-id
                           :text    "Куда доставить?"
                           :options request-location-markup}})
      {:delete-message {:chat-id    chat-id
                        :message-id message-id}})))


(def order-confirmation-markup
  {:inline_keyboard
   [[{:text u/location-emoji :callback_data "request-location"}
     {:text u/payment-emoji :callback_data "change-payment-type"}
     {:text u/comment-emoji :callback_data "change-comment"}]
    [{:text (str u/basket-emoji " Корзина") :callback_data "basket"}]
    [{:text "✅ Подтвердить" :callback_data "nothing"}]]})


(defn make-order-text
  [order-state]
  (format (str "*Детали вашего заказа:* \n\n"
               u/money-emoji " %s сум \n"
               u/payment-emoji " %s \n"
               u/comment-emoji " Без комментария \n\n"
               u/location-emoji " %s")
          (u/fmt-values (:total_cost order-state))
          "Наличными"
          "60, 1st Akkurgan Passage, Mirzo Ulugbek district, Tashkent"))


(defn get-chat-id-from-update
  "Use when you don't know input update type"
  [update]
  (let [message (:message update)
        query (:callback_query update)]
    (cond
      message (:id (:from message))
      query (:id (:from query)))))


(defn send-order-detail
  [_ update order-state]
  (let [chat-id (get-chat-id-from-update update)]
    {:send-text {:chat-id chat-id
                 :text    (make-order-text order-state)
                 :options {:reply_markup order-confirmation-markup
                           :parse_mode   "markdown"}}}))


(defn handle-location
  [ctx update]
  (let [message (:message update)
        chat-id (:id (:from message))
        location (:location message)]
    {:send-text {:chat-id chat-id
                 :text    "Локация обновлена"
                 :options {:reply_markup {:remove_keyboard true}}}
     :core      [(:core (make-order-state ctx update))
                 {:function #(users/update-location!
                               (:id (:user ctx))
                               (:longitude location)
                               (:latitude location))}]}))


(d/register-event-handler!
  :location
  handle-location)


(d/register-event-handler!
  :request-location
  request-location)


(d/register-event-handler!
  :send-order-detail
  send-order-detail)

