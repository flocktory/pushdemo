Документация firebase: [ссылка](https://firebase.google.com/docs/cloud-messaging/android/client)

### Чтобы убедиться, что все работает:
* Авторизовать приложение в firebase (обратите внимание на файл google-services.json)
* В файле `FlocktoryApiClient.java` указать site-id в системе flocktory , токен для взаимодействия с [API](http://flocktory.com/help/client-api)  , gcm_sender_id
* Передать Flocktory данные для отправки пушей, используя ваш firebase проект ([скриншот](https://monosnap.com/file/ZAsZMARL7JGYe7NK5LNGLxxXNok498))

### На что обратить внимание
* C каждым запросом (за исключением тестового пуша) надо передавать куку flocktory web session и в body - параметр site-session-id. Если они еще не получены, надо выполнить запрос номер 7 из [этой инструкции](https://flocktory.github.io/ru/push/mobile-integration/), сохранить данные и только потом делать какой-либо запрос, в том числе передача токена