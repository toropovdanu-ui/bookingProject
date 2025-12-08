## Event Booking Backend

Java / Spring Boot backend для системы бронирования мест на мероприятия. 
Проект отвечает за регистрацию и авторизацию пользователей, управление мероприятиями и бронированиями, а также за настройки уведомлений.

### Технологии

- **Java 21**
- **Spring Boot 3 (Web, Security, Data JPA, Validation, Mail)**
- **PostgreSQL + Liquibase**
- **JWT-аутентификация**
- **MapStruct**
- **springdoc-openapi (Swagger UI)**

### Сборка и запуск (локально, без Docker)

- **Требования**: установлен JDK 21, Gradle wrapper уже в проекте.

1. Перейти в каталог бэкенда:

   ```bash
   cd eventify/booking-backend
   ```

2. Настроить доступ к БД (по умолчанию используется PostgreSQL на `localhost:5432`): 
   параметры заданы в `application.yaml`:
   - **url**: `jdbc:postgresql://localhost:5432/eventdb`
   - **username**: `postgres`
   - **password**: `postgres`

3. Запустить приложение:

   ```bash
   ./gradlew bootRun
   ```

4. Приложение поднимется на `http://localhost:8080`.

### Запуск всего приложения через Docker

Полный стек (PostgreSQL + Mailhog + backend + frontend) поднимается из корня проекта `eventify` файлом `docker-compose.full.yaml`.

- **Шаги**:

  ```bash
  cd eventify
  docker compose -f docker-compose.full.yaml up --build
  ```

- **Что поднимется**:
  - `postgres` – БД `eventdb`
  - `mailhog` – тестовая почта (`http://localhost:8025`)
  - `app` – backend (`http://localhost:8080` внутри Docker-сети, проброшен на хост)
  - `frontend` – React UI (`http://localhost:3000`)

### Документация API (Swagger)

После запуска backend’а документация доступна по адресу:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

В Swagger UI доступна кнопка **Authorize** – она принимает JWT токен формата:

```text
Bearer <JWT>
```

Публичные эндпоинты (`/auth/**`, `/events/**`) работают без токена, остальные требуют авторизации.

### Обзор основных эндпоинтов

- **Аутентификация** (`/auth`)
  - **POST `/auth/register`** – регистрация пользователя. 
    - **Тело**: `UserCredentialRequest` (email, пароль и т. д.).
    - **Ответ**: `AuthUserResponse` с ролью пользователя и токеном (если предусмотрено).
  - **POST `/auth/login`** – вход пользователя.
    - **Тело**: `UserCredentialRequest`.
    - **Ответ**: `AuthUserResponse` с полем `token` (JWT) и `role`.

- **Мероприятия** (`/events`) – публичный доступ
  - **GET `/events`** – получение списка мероприятий с фильтрацией.
    - **Параметры**: поля `EventFilterRequest` (передаются как query-params) и `Pageable` (например, `page`, `size`).
    - **Ответ**: `Page<EventResponse>`.
  - **GET `/events/{id}`** – получить одно мероприятие по ID.

- **Бронирования пользователя** (`/bookings`) – требуется JWT
  - **GET `/bookings`** – список всех бронирований текущего пользователя.
  - **GET `/bookings/{id}`** – получить конкретное бронирование.
  - **POST `/bookings`** – создать бронирование.
    - **Тело**: `CreateBookingRequest` (ID события, количество билетов и т. д.).
    - **Ответ**: `BookingResponse`.
  - **PUT `/bookings/{id}`** – обновить бронирование (например, количество билетов).
    - **Тело**: `UpdateBookingRequest`.
  - **DELETE `/bookings/{id}`** – отменить бронирование, вернуть билеты в пул.

- **Панель администратора** (`/admin`) – требуется роль `ADMIN`
  - **POST `/admin/events`** – создать мероприятие (`UpsertEventRequest` → `EventResponse`).
  - **PUT `/admin/events/{id}`** – обновить мероприятие.
  - **DELETE `/admin/events/{id}`** – удалить мероприятие.
  - **GET `/admin/bookings`** – поиск/фильтрация всех бронирований (`BookingFilterRequest` + `Pageable`).
  - **PUT `/admin/bookings/{id}/confirm`** – подтвердить бронирование.
  - **DELETE `/admin/bookings/{id}`** – удалить бронирование.

- **Настройки уведомлений пользователя** (`/user/notifications`) – требуется JWT
  - **GET `/user/notifications`** – получить текущие настройки уведомлений (`NotificationSettingsResponse`).
  - **PUT `/user/notifications`** – обновить настройки (`UpdateNotificationSettingsRequest`).
  - **DELETE `/user/notifications`** – отключить уведомления.

- **Служебные эндпоинты**
  - **GET `/actuator/health`** – проверка живости сервиса (без авторизации).
  - **GET `/actuator/info`** – базовая информация о приложении.

### Безопасность и авторизация

- Аутентификация реализована с помощью **JWT** (Bearer-токен в заголовке `Authorization`).
- Открытые маршруты настроены в `SecurityConfiguration`:
  - `/auth/**`, `/events/**`, `/actuator/**`, Swagger и статические ресурсы.
- Все остальные пути требуют валидного JWT-токена.

### Миграции БД

- Схема БД и начальные данные управляются через **Liquibase**:
  - master-файл: `db.changelog/db.changelog-master.xml`
  - SQL-скрипты: `db.changelog/changes/001-init-db.sql`

При первом запуске Liquibase автоматически создаст таблицы и заполнит справочные данные.