# HashCat. Сервис расшифровки хешей

## Описание
Сервис предоставляет API для отправки заявки (application) на расшифровку MD5 хешей.  
Заявка включает в себя email пользователя и массив из 1+ хешей.  
По завершению обработки всех хешей из заявки пользователь получает письмо на email с результатами.  
Чтобы не делать пустую работу, перед началом обработки заявки, нужно убедиться в корректности и работоспособности email.  
HashCat сам не выполняет расшифровку хешей, а делегирует эту операцию публичным бесплатным сервисам.  
Для улучшения производительности и уменьшения нагрузки на внешние сервисы, HashCat кеширует ответы от сервисов в своей базе.  
Сервис должен работать в распределённом режиме: 2+ экземпляра за примитивным балансировщиком нагрузки.

## Требования 

### REST API для обработки заявок
```http request
POST /api/applications
Content-Type: application/json
{
  "email": "test@mail.loc",
  "hashes": [
    "c4ca4238a0b923820dcc509a6f75849b",
    "c81e728d9d4c2f636f067f89cc14862c",
    "eccbc87e4b5ce2fe28308fd9f2a7baf3"
  ]
}
```
При приёме заявки производится синхронная валидация структуры заявки и формата данных: email, хеши.  
Если формат корректен - возвращается `200` с пустым телом, иначе `400` с текстовым описанием ошибки.  
Дальнейшая обработка заявки выполняется асинхронно.  

### Email
Перед началом расшифровки хешей заявки нужно убедиться в корректности email, на который будет отправлен результат обработки.  
Нужно реализовать типовую процедуру подтверждения email:
- генерация уникальной одноразовой ссылки
- включение ссылки в текст письма
- обработка обращения по этой ссылке

**Никакой UI после перехода по ссылке не нужен, достаточно показать пользователю `200` с текстом об успешном подтверждении ящика.   
Если ранее конкретный email уже был подтверждён - повторное подтверждение не требуется.  
По завершению расшифровки всех хешей из заявки - на email из заявки отправляется результат в произвольном текстовом виде.
  
### Расшифровка хешей**
Для расшифровки хешей нужно выбрать один из бесплатнх внешних сервисов в интернете, определить и использовать его API.  
Ответы от внешнего сервиса должны кешироваться в локальную базу, на один и тот же хеш не должно лететь более 1 запроса во внешний сервис.  
Если внешний сервис не смог расшифровать хеш, в качестве результата по этому хешу пользователю вернётся пустая строка. Хеш считается не расшифрованным.  

### Состояние
Для хранения состояния: заявки, email-ы, кеш хешей и т.д. - должно использоваться персистентное хранилище.  
Рекомендуется использовать PostgreSQL или MongoDB, можно любую другую базу - с обоснованием выбора.  
Для масштабируемости, сам сервис должен быть stateless, всё состояние - во внешнем хранилище.  
Во время проверки задания, будет одновремено запущено 2+ экземпляра сервиса HashCat.


## Окружение разработки
Для выполнения задания требуется Docker.  
Для начала работы представлен шаблон типичного микросервиса, инфраструктура сборки и развёртывания.  
Пользоваться шаблоном не обязательно - можно делать с нуля по собственному видению, ограничений нет.  
Изменение инфраструктуры не меняет условий проверки задания.  


## Проверка задания
Конфигурационные параметры для запуска HashCat задаются через переменные окружения в файле `docker-compose.yml`  
Сборка и запуск распределённого окружения одной командой:
```bash
docker-compose up --build --scale hashcat=3
```
Ручное тестирование API  
Наличие автоматизированных приёмочных тестов приветствуется.  


## Если базовое задание оказалось слишком простым
- добавить второй внешний сервис, слать запросы параллельно в оба внешних сервиса.
- добавить REST API для отчётов, доступный только администраторам с авторизацией:
    - список администраторов (логин, пароль) задаётся при запуске через переменную окружения
    - basic авторизация
    - отчёт по временному интервалу: дата начала, дата окончания
    - в отчёте: кол-во обработанных заявок, кол-во успешно расшифрованных хешей, кол-во нерасшифрованных хешей - в разрезе email-ов пользователей