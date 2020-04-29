(ns yamfood.telegram.translation.core
  (:require
    [tongue.core :as tongue]
    [yamfood.telegram.handlers.emojies :as e]))


(def dictionary
  {:ru {; Registration
        :send-contact-button             "Отправить контакт"
        :change-phone-button             "Изменить номер"
        :request-code-message            "Отправьте 4х значный код отправленный на номер _+{1}_"
        :invalid-phone-message           "Неверный номер телефона, попробуйте еще раз"
        :phone-confirmed-message         "Номер успешно подтвержден!"
        :incorrect-code-message          "Неверный код, попробуйте еще раз"
        :request-contact-message         (str "Отправь свой контакт или номер телефона в формате _998901234567_\n\n"
                                              "Мы отправим СМС с кодом для подтверждения")

        ; Start
        :hello-message                   "С чего начнем?"
        :menu-button                     "\uD83C\uDF7D Что поесть?"
        :regions-button                  (str e/location-emoji " Зона покрытия")
        :settings-button                 (str e/settings-emoji " Настройки")

        ; Settings
        :settings-change-phone-button    "Изменить номер телефона"
        :settings-menu-button            (str e/back-emoji " Назад")
        :settings-message                (str e/settings-emoji " *Настройки* \n\n"
                                              "*Язык*: Русский\n"
                                              "*Номер телефона*: +{1}\n\n"
                                              "_Для смены языка нажмите на соответствующую кнопку_")

        ; Update location
        :update-location-inline-button   "Коснитесь чтобы обновить\n"
        :send-current-location-button    "Отправить текущее положение"
        :new-location-message            "Новый адрес: {1}"
        :request-location-message        (str "*куда доставить?*\n\n"
                                              "нажмите «Отправить текущее местоположение» или отправьте локацию вручную\n\n"
                                              "_не забудьте включить локацию на своем телефоне_")

        ; Invalid-location
        :invalid-location-message        "К сожалению, мы не обслуживаем данный регион"
        :invalid-location-regions-button "Карта обслуживания"
        :invalid-location-menu-button    (str e/back-emoji " Меню")
        :invalid-location-basket-button  (str e/basket-emoji " Корзина")

        ; Product Details
        :added-to-basket-message         "Добавлено в корзину"
        :more-button                     "\uD83C\uDF7D Еще?"
        :add-product-button              "Хочу"
        :product-basket-button           (str e/basket-emoji " Корзина ({1} сум)")
        :product-menu-button             (str e/back-emoji " Назад")
        :product-caption                 (str e/food-emoji " *{name}* \n\n"
                                              "{description}"
                                              e/money-emoji "{price} сум")

        ; Basket
        :basket-message                  "Ваша корзина:"
        :empty-basket-text               "К сожалению, ваша корзина пока пуста :("
        :basket-menu-button              (str e/back-emoji " В меню")
        :to-order-button                 "✅ Далее"

        ; Order Confirmation
        :oc-basket-button                (str e/back-emoji " Корзина")
        :oc-create-order-button          "✅ Подтвердить"
        :oc-empty-comment-text           "Пусто"
        :oc-message                      (str "*Детали вашего заказа:* \n\n"
                                              e/food-emoji " {price} сум\n"
                                              e/delivery-emoji " {delivery} сум\n"
                                              e/money-emoji " {total} сум ({payment})\n\n"
                                              e/comment-emoji " `{comment}` \n\n"
                                              e/location-emoji " {address}")

        ; Active Order
        :active-order-message            (str "*Заказ №{1}:*\n\n"
                                              "{2}"
                                              "\n"
                                              e/money-emoji " {3} сум ({4})\n\n"
                                              "Ваш заказ принят, ожидайте подтверждения")

        ; Payments
        :pay-button                      "Оплатить"
        :invoice-cancel-button           "Назад"
        :invoice-title                   "Оплатить заказ №{1}"

        ; Statuses
        :status-on-kitchen               "Ваш заказ готовится, курьер приедет через 60 минут или раньше!"
        :status-canceled                 "Заказ отменен :("
        :status-on-way                   "Курьер уже в пути!"

        ; Feedback
        :request-feedback-message        "Оцените, пожалуйста, заказ!"

        ; Other
        :unhandled-text                  "Не понял"
        :blocked-message                 "Вы заблокированы, обратитесь в службу поддержки"
        :accepted                        "Принято"
        :card                            "Картой"
        :cash                            "Наличными"
        :confirmation-code               "Ваш код подтверждения WOK {1}"}
   :en {; Registration
        :send-contact-button             "Send a contact"
        :change-phone-button             "Change phone number"
        :request-code-message            "Enter the code you've just received on your phone number _+{1}_"
        :invalid-phone-message           "Invalid phone number, please try again"
        :phone-confirmed-message         "The phone number was successfully confirmed!"
        :incorrect-code-message          "Invalid code, try again"
        :request-contact-message         (str "Send your contact or phone number in the format _998901234567_\n\n"
                                              "We will send an SMS with a confirmation code")

        ; Start
        :hello-message                   "Where do we start?"
        :menu-button                     "\uD83C\uDF7D What to eat?"
        :regions-button                  (str e/location-emoji " Delivery zone")
        :settings-button                 (str e/settings-emoji " Settings")

        ; Settings
        :settings-change-phone-button    "Change your phone number"
        :settings-menu-button            (str e/back-emoji " Back")
        :settings-message                (str e/settings-emoji " *Settings* \n\n"
                                              "*Language*: English\n"
                                              "*Phone number*: +{1}\n\n"
                                              "_To change the language, click the relevant button_")

        ; Update location
        :update-location-inline-button   "Tap to update\n"
        :send-current-location-button    "Send current location"
        :new-location-message            "New address: {1}"
        :request-location-message        (str "*Where to deliver?*\n\n"
                                              "Tap «Send current location» or send the location manually\n\n"
                                              "_don't forget to enable location on your phone_")

        ; Invalid-location
        :invalid-location-message        "Unfortunately, we do not serve this region"
        :invalid-location-regions-button "Service map"
        :invalid-location-menu-button    (str e/back-emoji " Menu")
        :invalid-location-basket-button  (str e/basket-emoji " Cart")

        ; Product Details
        :added-to-basket-message         "Added to cart"
        :more-button                     "\uD83C\uDF7D More?"
        :add-product-button              "Want"
        :product-basket-button           (str e/basket-emoji " Cart ({1} sum)")
        :product-menu-button             (str e/back-emoji " Back")
        :product-caption                 (str e/food-emoji " *{name}* \n\n"
                                              e/money-emoji "{price} sum")

        ; Basket
        :basket-message                  "Your Cart:"
        :empty-basket-text               "Unfortunately, your Cart is still empty :("
        :basket-menu-button              (str e/back-emoji " Into menu")
        :to-order-button                 "✅ Further"

        ; Order Confirmation
        :oc-basket-button                (str e/back-emoji " Cart")
        :oc-create-order-button          "✅ Confirm"
        :oc-empty-comment-text           "Empty"
        :oc-message                      (str "*Details of your order:* \n\n"
                                              e/food-emoji " {price} sum\n"
                                              e/delivery-emoji " {delivery} sum\n"
                                              e/money-emoji " {total} sum ({payment})\n"
                                              e/comment-emoji " `{comment}` \n\n"
                                              e/location-emoji " {address}")

        ; Active Order
        :active-order-message            (str "*Order №{1}:*\n\n"
                                              "{2}"
                                              "\n"
                                              e/money-emoji " {3} sum ({4})\n\n"
                                              "Your order is accepted, wait for confirmation")

        ; Payments
        :pay-button                      "Pay"
        :invoice-cancel-button           "Back"
        :invoice-title                   "Pay order №{1}"

        ; Statuses
        :status-on-kitchen               "Your order is preparing, rider will arrive in 60 minutes or earlier"
        :status-canceled                 "The order has been cancelled :("
        :status-on-way                   "Rider is on its way!"

        ; Feedback
        :request-feedback-message        "Please rate the order!"

        ; Other
        :unhandled-text                  "I didn't understand"
        :blocked-message                 "You are blocked, please contact the support service"
        :accepted                        "Accepted"
        :card                            "By card"
        :cash                            "In cash"
        :confirmation-code               "Your WOK confirmation code is {1}"}
   :uz {; Registration
        :send-contact-button             "Telefon raqamini yuborish"
        :change-phone-button             "Telefon raqamini o'zgartirish"
        :request-code-message            "Telefon raqamingizga yuborilgan kodni yuboring _+{1}_"
        :invalid-phone-message           "Noto'g'ri telefon raqami, yana urinib ko'ring"
        :phone-confirmed-message         "Telefon raqami tasdiqlangan!"
        :incorrect-code-message          "Noto'g'ri kod, yana urinib ko'ring"
        :request-contact-message         (str "Kontaktingizni yoki telefon raqamingizni formatda yuboring _998901234567_\n\n"
                                              "Tasdiqlash uchun kod bilan SMS yuboramiz")

        ; Start
        :hello-message                   "Nimadan boshlaymiz?"
        :menu-button                     "\uD83C\uDF7D Nima ovqatlar bor?"
        :regions-button                  (str e/location-emoji " Qoplama maydoni")
        :settings-button                 (str e/settings-emoji " Sozlamalar")

        ; Settings
        :settings-change-phone-button    "Telefon raqamini o'zgartirish"
        :settings-menu-button            (str e/back-emoji " Orqaga")
        :settings-message                (str e/settings-emoji " *Sozlamalar* \n\n"
                                              "*Til*: O'zbekcha\n"
                                              "*Telefon raqami*: +{1}\n\n"
                                              "_Tilni o'zgartirish uchun tegishli tugmani bosing_")

        ; Update location
        :update-location-inline-button   "Yangilash uchun bosing\n"
        :send-current-location-button    "Joriy manzilni yuborish"
        :new-location-message            "Yangi manzil: {1}"
        :request-location-message        (str "*qayerga yetkazib berish kerak?*\n\n"
                                              "«Joriy manzilni yuborish» tugmasini bosing yoki joyni qo'lda yuboring\n\n"
                                              "_telefoningizdagi joyni yoqishni unutmang_")

        ; Invalid-location
        :invalid-location-message        "Afsuski, biz ushbu manzilda xizmat qilmaymiz"
        :invalid-location-regions-button "Xizmat kartasi"
        :invalid-location-menu-button    (str e/back-emoji " Menyu")
        :invalid-location-basket-button  (str e/basket-emoji " Savat")

        ; Product Details
        :added-to-basket-message         "Savatga qo'shilgan"
        :more-button                     "\uD83C\uDF7D Yana?"
        :add-product-button              "Istayman"
        :product-basket-button           (str e/basket-emoji " Savat ({1} so'm)")
        :product-menu-button             (str e/back-emoji " Назад")
        :product-caption                 (str e/food-emoji " *{name}* \n\n"
                                              "{description}"
                                              e/money-emoji "{price} so'm")

        ; Basket
        :basket-message                  "Sizning savatingiz:"
        :empty-basket-text               "Afsuski, sizning savatingiz hali bo'sh :("
        :basket-menu-button              (str e/back-emoji " Menyuga")
        :to-order-button                 "✅ Keyingi"

        ; Order Confirmation
        :oc-basket-button                (str e/back-emoji " Savat")
        :oc-create-order-button          "✅ Tasdiqlash"
        :oc-empty-comment-text           "Bo'sh"
        :oc-message                      (str "*Buyurtmaning tafsilotlari:* \n\n"
                                              e/food-emoji " {price} so'm\n"
                                              e/delivery-emoji " {delivery} so'm\n"
                                              e/money-emoji " {total} so'm ({payment})\n"
                                              e/comment-emoji " `{comment}` \n\n"
                                              e/location-emoji " {address}")

        ; Active Order
        :active-order-message            (str "*Buyurtma №{1}:*\n\n"
                                              "{2}"
                                              "\n"
                                              e/money-emoji " {3} so'm ({4})\n\n"
                                              "Sizning buyurtmangiz qabul qilindi, tasdiqlashni kuting")

        ; Payments
        :pay-button                      "To'lash"
        :invoice-cancel-button           "Orqaga"
        :invoice-title                   "Buyurtmani to'lash №{1}"

        ; Statuses
        :status-on-kitchen               "Sizning buyurtmangiz tayyorlanyapti, kuryer 60 daqiqada yoki undan oldin keladi!"
        :status-canceled                 "Buyurtma bekor qilindi :("
        :status-on-way                   "Kuryer yo'lda!"

        ; Feedback
        :request-feedback-message        "Iltimos, buyurtmani baholang!"

        ; Other
        :unhandled-text                  "Tushunmadim"
        :blocked-message                 "Siz bloklangindiz, qo'llab-quvvatlash xizmatiga murojaat qiling"
        :accepted                        "Qabul qilindi"
        :card                            "Karta bilan"
        :cash                            "Naqd pul bilan"
        :confirmation-code               "Sizning WOK tasdiqlash kodi {1}"}})


(def translate
  (tongue/build-translate dictionary))
