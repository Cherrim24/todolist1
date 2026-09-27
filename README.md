# ToDo List API

REST-сервис для управления задачами на **Spring Boot 4** с хранением в **PostgreSQL**,
валидацией входных данных и автодокументацией через **Swagger UI**.

Полноценный CRUD: создание, чтение, обновление и удаление задач.

---

## Содержание

- [Стек технологий](#стек-технологий)
- [Быстрый старт](#быстрый-старт)
- [Документация API](#документация-api)
- [Спецификация эндпоинтов](#спецификация-эндпоинтов)
- [Формат ответов](#формат-ответов)
- [Правила валидации](#правила-валидации)
- [Тесты](#тесты)
- [Конфигурация](#конфигурация)
- [Если не запускается](#если-не-запускается)
- [Структура проекта](#структура-проекта)
- [Принятые решения](#принятые-решения)
- [Что не реализовано](#что-не-реализовано)

---

## Стек технологий

| Компонент | Версия |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.1 |
| Spring Web MVC | 7.0.2 |
| Spring Data JPA / Hibernate | 7.2.0.Final |
| PostgreSQL | 13+ (проверено на 18) |
| Bean Validation (Hibernate Validator) | 9.0.1.Final |
| springdoc-openapi (Swagger UI) | 2.8.5 |
| JUnit 5 + Mockito | 6.0.1 / 5.20.0 |
| Lombok | 1.18.42 |
| Сборка | Maven |

---

## Быстрый старт

### 1. Требования

- JDK 17 или новее
- Maven 3.9+ (либо встроенный `mvnw`)
- PostgreSQL, запущенный локально

### 2. Создайте базу данных

```sql
CREATE DATABASE todolist;
```

Пользователя базы данных создайте сами, например:

```sql
CREATE USER todolist WITH PASSWORD 'ваш_пароль';
GRANT ALL PRIVILEGES ON DATABASE todolist TO todolist;
```

Схему таблиц создавать не нужно: включён `spring.jpa.hibernate.ddl-auto=update`,
таблица `tasks` создастся автоматически при первом запуске.

### 3. Задайте пароль от базы данных

Приложение читает настройки подключения из переменных окружения.
Обязательная переменная — `DB_PASSWORD`:

**PowerShell**
```powershell
$env:DB_PASSWORD = "ваш_пароль"
```

**Linux / macOS**
```bash
export DB_PASSWORD="ваш_пароль"
```

По умолчанию приложение подключается к базе как пользователь `postgres`
(`DB_USER=postgres`). Если на шаге 2 вы создали отдельного пользователя
`todolist`, задайте и его имя:

```powershell
$env:DB_USER = "todolist"
```

Либо сделайте `postgres` владельцем базы, чтобы `DB_USER` не пришлось задавать:

```sql
GRANT ALL PRIVILEGES ON DATABASE todolist TO postgres;
```

### 4. Запустите приложение

```bash
mvn spring-boot:run
```

или соберите и запустите исполняемый jar:

```bash
mvn clean package
java -jar target/todolist-0.0.1-SNAPSHOT.jar
```

Сервис поднимется на `http://localhost:8080`.

> **Примечание.** Если в пути к проекту есть кириллица или другие не-ASCII символы,
> Maven Wrapper (`mvnw.cmd`) может не запуститься — это известная проблема самого
> wrapper-скрипта. Используйте системный `mvn` или склонируйте репозиторий в путь
> только с латинскими символами.

### 5. Проверьте, что всё работает

```bash
curl http://localhost:8080/tasks
```

```json
{"success":true,"message":"Tasks found","data":[]}
```

---

## Документация API

После запуска доступна интерактивная документация:

| Адрес | Что открывается |
|---|---|
| <http://localhost:8080/swagger-ui/index.html> | Swagger UI — можно выполнять запросы прямо из браузера |
| <http://localhost:8080/v3/api-docs> | OpenAPI 3 JSON/YAML |

---

## Спецификация эндпоинтов

Базовый путь: `/tasks`

| Метод | Путь | Назначение | Успех | Ошибки |
|---|---|---|---|---|
| `GET` | `/tasks` | Список всех задач | `200` | — |
| `GET` | `/tasks/{id}` | Задача по идентификатору | `200` | `404`, `400` |
| `POST` | `/tasks` | Создать задачу | `201` | `400` |
| `PUT` | `/tasks/{id}` | Обновить задачу | `200` | `404`, `400` |
| `DELETE` | `/tasks/{id}` | Удалить задачу | `200` | `404` |

### Модель задачи

```json
{
  "id": 1,
  "title": "Купить молоко",
  "description": "В магазине у дома",
  "status": "NEW",
  "completed": false
}
```

| Поле | Тип | Описание |
|---|---|---|
| `id` | `Long` | Идентификатор, генерируется базой данных (автоинкремент) |
| `title` | `String` | Название задачи, обязательное, 5–20 символов |
| `description` | `String` | Описание, обязательное, 5–100 символов |
| `status` | `String` | Статус, необязательное поле (например `NEW`, `IN_PROGRESS`, `DONE`) |
| `completed` | `Boolean` | Признак выполнения. Если поле не передать или передать `null`, сервер сохранит `false` |

### Примеры запросов

**Создать задачу**
```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Купить молоко","description":"В магазине у дома","status":"NEW","completed":false}'
```
```json
{
  "success": true,
  "message": "Task created successfully",
  "data": {"id":1,"title":"Купить молоко","description":"В магазине у дома","status":"NEW","completed":false}
}
```

**Получить все задачи**
```bash
curl http://localhost:8080/tasks
```

**Получить задачу по id**
```bash
curl http://localhost:8080/tasks/1
```

**Обновить задачу** (передаются все изменяемые поля)
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Купить молоко и хлеб","description":"В магазине у дома","status":"DONE","completed":true}'
```

**Удалить задачу**
```bash
curl -X DELETE http://localhost:8080/tasks/1
```
```json
{"success":true,"message":"Task deleted successfully","data":{"deletedId":1}}
```

---

## Формат ответов

Все ответы — JSON. Успешные имеют единую обёртку:

```json
{
  "success": true,
  "message": "human readable message",
  "data": {}
}
```

Ошибочные — тоже в обёртке, с `"success": false` и HTTP-кодом ошибки:

```json
{
  "success": false,
  "message": "Task with id 99 not found",
  "data": null
}
```

При ошибке валидации дополнительно возвращается карта `errors` с сообщениями по каждому полю:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": {
    "title": "Название должно иметь минимум 5 символов и не больше 20"
  }
}
```

| HTTP-код | Когда возвращается |
|---|---|
| `400 Bad Request` | Не пройдена валидация, некорректный JSON, нечисловой `id` |
| `404 Not Found` | Задача с указанным `id` не существует |
| `500 Internal Server Error` | Непредвиденная ошибка сервера |

---

## Правила валидации

Валидация реализована аннотациями Bean Validation на сущности `Task`
и срабатывает на `POST` и `PUT`:

| Поле | Правило | Сообщение |
|---|---|---|
| `title` | не `null` | Название не должно быть пустым |
| `title` | длина 5–20 | Название должно иметь минимум 5 символов и не больше 20 |
| `description` | не `null` | Описание не должно быть пустым |
| `description` | длина 5–100 | Описание должно иметь хотя бы 5 букв |

Минимальная длина в 5 символов — авторское решение; при необходимости
значения меняются аннотациями `@NotNull` и `@Size` в `entity/Task.java`.

---

## Тесты

```bash
mvn test
```

Покрыто 16 тестами:

| Класс | Что проверяет | Тестов |
|---|---|---|
| `TestTask` | `TaskService` на моках репозитория: получение по id, список, сохранение, обновление, удаление, ошибка «не найдено» | 6 |
| `TaskControllerTest` | HTTP-слой через `MockMvc`: все 5 эндпоинтов, коды `201/200/404`, значение по умолчанию для `completed`, ошибки валидации, некорректный `id` | 10 |

Тесты не требуют запущенной базы данных — `TaskService` работает с моками,
контроллер тестируется в срезе `@WebMvcTest`.

---

## Конфигурация

Все настройки читаются из переменных окружения, значения по умолчанию указаны ниже.

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/todolist` | JDBC-URL базы данных |
| `DB_USER` | `postgres` | Пользователь БД |
| `DB_PASSWORD` | **обязательная** | Пароль БД |
| `SERVER_PORT` | `8080` | Порт приложения |
| `API_SERVER_URL` | `http://localhost:8080` | URL, указываемый в Swagger |
| `LOG_FILE` | `log/todolist.log` | Файл логов (профиль `dev`) |

Режим разработки с SQL-запросами, форматированным выводом и отладочным логированием:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Профиль `dev` выносит `debug=true`, `spring.jpa.show-sql=true` и трассировку
bind-параметров Hibernate в отдельный файл `application-dev.properties`,
поэтому в основной конфигурации логи чистые.

---

## Если не запускается

Самая частая причина — незаданная или неверная `DB_PASSWORD`. Приложение
подключается к базе на старте, поэтому при ошибке авторизации оно не
запустится вовсе. В логе ищите строку с кодом `SQLState: 28P01`:

```
WARN  ... org.hibernate.orm.jdbc.error : HHH000247: ErrorCode: 0, SQLState: 28P01
WARN  ... hibernate.orm.jdbc.error : password authentication failed for user "postgres"
ERROR ... Application run failed
```

`28P01` означает одно из двух: переменная `DB_PASSWORD` не задана в том
окне, где запущено приложение, либо в ней неверный пароль. Проверьте
значение и имя пользователя:

```sql
SELECT current_user, current_database();
```

| Симптом | Причина | Что делать |
|---|---|---|
| `28P01` / `password authentication failed` | неверный пароль или `DB_PASSWORD` не задана | задайте `DB_PASSWORD` заново |
| `Connection refused` | PostgreSQL не запущен | запустите службы PostgreSQL |
| `database "todolist" does not exist` | база не создана | `CREATE DATABASE todolist;` |
| `port 8080 is already in use` | приложение уже запущено | завершите прежний процесс или задайте `SERVER_PORT=8081` |
| `Unable to determine Dialect without JDBC metadata` | следствие ошибки подключения к БД | смотрите исходную ошибку выше по логу, обычно `28P01` |

Важно: переменная окружения задаётся только в том терминале, где запускается
приложение, и не наследуется другими окнами. Если запускаете из IntelliJ IDEA —
пропишите её в `Run/Debug Configurations → Environment variables`.

---

## Структура проекта

```
src/
├── main/
│   ├── java/com/example/todolist/
│   │   ├── TodolistApplication.java          # точка входа
│   │   ├── Config.java                       # описание OpenAPI для Swagger
│   │   ├── GlobalExceptionHandler.java       # единый обработчик ошибок
│   │   ├── ResourceNotFoundException.java    # 404 «объект не найден»
│   │   ├── Controllers/
│   │   │   └── TaskController.java           # REST-эндпоинты /tasks
│   │   ├── entity/
│   │   │   └── Task.java                     # JPA-сущность + валидация
│   │   ├── repository/
│   │   │   └── TaskRepository.java           # Spring Data JPA
│   │   └── servises/
│   │       └── TaskService.java              # бизнес-логика
│   └── resources/
│       ├── application.properties            # основная конфигурация
│       ├── application-dev.properties        # профиль разработки
│       └── templates/                        # заготовки Thymeleaf (не подключены, см. ниже)
└── test/java/com/example/todolist/
    ├── TaskControllerTest.java               # тесты HTTP-слоя
    └── TestTask.java                         # тесты сервисного слоя
```

Поток запроса: `TaskController` → `TaskService` → `TaskRepository` → PostgreSQL.
Ошибки из сервиса (`ResourceNotFoundException`) и ошибки валидации
преобразуются в JSON в `GlobalExceptionHandler`.

---

## Принятые решения

- **Идентификаторы генерирует БД.** У поля `id` стоит
  `@GeneratedValue(strategy = GenerationType.IDENTITY)`, поэтому клиент
  не присылает `id` при создании, а база выдаёт `bigserial`.
- **`completed` не может стать `null`.** Сеттер приводит отсутствующее
  значение к `false`, а колонка объявлена `NOT NULL`, поэтому в базе всегда
  лежит `true` или `false`.
- **Единый формат ответов.** Контроллер не смешивает «голый» объект с данными
  и ошибку: любой ответ — это обёртка `{success, message, data}`.
- **Ошибки обрабатываются централизованно** в `@RestControllerAdvice`, а не
  `try/catch` внутри каждого метода контроллера. Коды статусов выставляет
  обработчик: `400` для валидации и некорректного запроса, `404` для
  отсутствующей задачи.
- **Никакой авторизации.** `spring-security` намеренно не подключён —
  сервис рассчитан на доверенную сеть. Если API станет публичным,
  добавьте `spring-boot-starter-security` (см. раздел «Что не реализовано»).
- **Минимальный `pom.xml`.** Убраны конфликтующие транзитивные зависимости:
  устаревшие `hibernate-core 4.x` и `hibernate-entitymanager 5.x` (на `javax`),
  несовместимый с Spring Boot 4 `spring-security-config 3.2.x`, TestNG,
  JUnit 4 и дублирующиеся объявления. Все версии берутся из
  `spring-boot-starter-parent`.

---

## Что не реализовано

- **HTML-интерфейс.** В `src/main/resources/templates` лежат заготовки
  Thymeleaf, но контроллер — `@RestController` и отдаёт JSON, поэтому эти
  шаблоны не используются. Либо подключите их отдельным `@Controller`,
  либо удалите вместе с зависимостью `spring-boot-starter-thymeleaf`.
- **Авторизация и роли.** Любой, кто имеет доступ к порту 8080, может
  читать и изменять все задачи.
- **Пагинация, сортировка и фильтрация** списка задач — `GET /tasks`
  всегда возвращает все записи.
- **Поле `status`** — это свободная строка без справочника и enum,
  поэтому опечатки в нём не проверяются.
- **Docker и CI** — Dockerfile и пайплайн сборки не настроены.

---

## Безопасность

Пароль от базы данных **не хранится в репозитории** — он передаётся
через переменную окружения `DB_PASSWORD`. Не добавляйте `.env` с секретами
в систему контроля версий (он уже в `.gitignore`) и не коммитьте
`application-local.properties` с реальными реквизитами.

Если пароль вашей БД уже попадал в репозиторий, смените его в PostgreSQL
и введите новый пароль в `DB_PASSWORD` при следующем запуске:

```sql
ALTER USER postgres WITH PASSWORD 'новый_пароль';
```

После смены пароля все ранее запущенные экземпляры приложения перестанут
подключаться к базе — это ожидаемо, нужно просто перезапустить их с новым
значением переменной окружения.
