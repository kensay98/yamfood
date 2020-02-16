(ns yamfood.telegram.handlers.client.start
  (:require
    [environ.core :refer [env]]
    [yamfood.telegram.dispatcher :as d]
    [yamfood.core.users.core :as users]
    [yamfood.telegram.handlers.utils :as u]
    [yamfood.core.users.core :as usr]))


(def menu-markup
  {:inline_keyboard
   [[{:text                             "Что поесть?"
      :switch_inline_query_current_chat ""}]
    [{:text "Куда доставляете?"
      :url  u/map-url}]]})


(defn menu-handler
  [ctx]
  (let [user (:user ctx)
        message (:message (:update ctx))
        chat-id (:id (:from message))]
    {:send-text {:chat-id chat-id
                 :options {:reply_markup menu-markup}
                 :text    "Готовим и бесплатно доставляем за 30 минут"}
     :run       {:function usr/update-payload!
                 :args     [(:id user)
                            (assoc (:payload user) :step u/menu-step)]}}))


(def registration-markup
  {:resize_keyboard true
   :keyboard        [[{:text            "Отправить контакт"
                       :request_contact true}]]})


(defn init-registration
  [message]
  {:send-text {:chat-id (:id (:chat message))
               :options {:reply_markup registration-markup}
               :text    "Отправьте, пожалуйста, свой контакт"}})


(defn parse-int [s]
  (bigdec (re-find #"\d+" s)))


(defn contact-handler
  [ctx]
  (let [update (:update ctx)
        message (:message update)
        contact (:contact message)
        phone (parse-int (:phone_number contact))
        from (:from message)
        tid (:id from)
        name (str (:first_name from) " " (:last_name from))]
    {:run       {:function users/create-user!
                 :args     [tid phone name]}
     :send-text [{:chat-id (:id (:chat message))
                  :options {:reply_markup {:remove_keyboard true}}
                  :text    "Принято!"}
                 (:send-text (menu-handler message))]}))


(defn start-handler
  [ctx]
  (let [update (:update ctx)
        message (:message update)]
    (if (:user ctx)
      {:dispatch {:args [:c/menu]}}
      (init-registration message))))


(d/register-event-handler!
  :c/start
  start-handler)


(d/register-event-handler!
  :c/menu
  menu-handler)


(d/register-event-handler!
  :c/contact
  contact-handler)
