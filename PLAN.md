
# NextFace — Project Plan & Technical Specification

> Главный документ проекта. Содержит описание архитектуры, технологий, взаимодействия компонентов, бизнес-логики и последовательности разработки.
>
> AI-разработчик должен использовать этот документ как основной источник требований и постепенно обновлять статусы выполнения задач.

---

# 1. Project Overview

## 1.1. Описание проекта

NextFace — учебный прототип централизованной системы биометрической аутентификации пользователей с использованием распознавания лица.

Основная идея заключается в том, чтобы предоставить сторонним веб-сайтам возможность использовать NextFace для авторизации своих пользователей.

Вместо реализации собственной системы регистрации и входа сторонний разработчик интегрирует NextFace в свой сайт с помощью небольшого JavaScript SDK.

Пользователь перенаправляется на официальный сайт NextFace, проходит аутентификацию по лицу, после чего возвращается на сайт, с которого начал авторизацию.

Все пользовательские данные, пароли и биометрические шаблоны хранятся и обрабатываются на стороне NextFace.

Сторонние сайты не должны самостоятельно:
- Регистрировать пользователей в NextFace.
- Хранить пароли пользователей NextFace.
- Получать фотографии лиц.
- Получать биометрические embeddings.
- Выполнять распознавание лиц.
- Обрабатывать биометрические данные.

## 1.2. Основные цели

- Создать централизованную систему пользовательской аутентификации.
- Реализовать регистрацию пользователей через email и пароль.
- Реализовать регистрацию биометрического профиля.
- Реализовать авторизацию посредством распознавания лица.
- Реализовать базовую проверку Liveness.
- Предоставить пользователям Dashboard.
- Предоставить разработчикам Developer Portal.
- Создать простой JavaScript SDK.
- Продемонстрировать интеграцию NextFace со сторонним веб-сайтом.

## 1.3. Ограничения проекта

Проект является учебным прототипом, а не коммерческой системой биометрической безопасности.

Не реализовывать:
- Kubernetes.
- Jenkins.
- CI/CD.
- Nginx.
- Redis.
- Микросервисы для каждой отдельной функции.
- Мобильные приложения.
- Собственную нейросеть для распознавания лиц.
- Полноценную коммерческую инфраструктуру.
- Сложную систему мониторинга.

Не усложнять архитектуру без необходимости.

---

# 2. Technology Stack

## 2.1. Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS
- shadcn/ui
- MediaDevices API для доступа к камере браузера.

Ответственность:
- Отображение пользовательского интерфейса.
- Регистрация и вход.
- Работа с камерой.
- Отображение процесса распознавания лица.
- Dashboard.
- Developer Portal.
- Управление пользовательскими настройками.
- Интеграция JavaScript SDK.

## 2.2. Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Validation
- JWT
- BCrypt
- PostgreSQL Driver

Ответственность:
- Регистрация пользователей.
- Авторизация пользователей.
- Генерация и проверка JWT.
- Управление пользовательскими профилями.
- Управление биометрическими профилями.
- Управление сторонними приложениями.
- Обработка запросов на интеграцию.
- Создание и проверка одноразовых authorization codes.
- Взаимодействие с Face Service.
- Работа с PostgreSQL.

## 2.3. Face Recognition Service

- Python 3
- FastAPI
- OpenCV
- MediaPipe
- ONNX Runtime
- NumPy
- Pydantic

Ответственность:
- Получение изображений лица.
- Обнаружение лица.
- Проверка качества изображения.
- Получение facial landmarks.
- Генерация face embeddings.
- Сравнение embeddings.
- Базовая проверка Liveness.

ВАЖНО:

MediaPipe используется для обнаружения лица и определения ключевых точек, но сам по себе не выполняет идентификацию личности.

Для идентификации необходимо использовать отдельную модель, которая генерирует face embeddings.

Не считать обычную RGB-камеру настоящим 3D-сканером. Проект использует оценку геометрии лица по обычному изображению.

## 2.4. Database

PostgreSQL — единственная основная база данных проекта.

Используется для хранения:
- Пользователей.
- Паролей в виде хэшей.
- Биометрических шаблонов.
- Сторонних приложений.
- Authorization codes.
- Истории авторизаций.

Redis не использовать.

## 2.5. Общие принципы

- REST API для коммуникации компонентов.
- JSON для передачи данных.
- JWT для пользовательских сессий NextFace.
- HTTPS обязателен при реальном размещении.
- Все секреты хранить в переменных окружения.
- Не хранить пароли в открытом виде.
- Не передавать embeddings сторонним сайтам.
- Не помещать Client Secret во frontend.
- Не хранить биометрические данные в localStorage.

---

# 3. System Architecture

## 3.1. Общая структура

```text
NextFace/
│
├── frontend/
│   ├── app/
│   ├── components/
│   ├── hooks/
│   ├── services/
│   ├── lib/
│   └── public/
│
├── backend/
│   └── src/main/
│       ├── java/com/nextface/
│       │   ├── auth/
│       │   ├── user/
│       │   ├── face/
│       │   ├── application/
│       │   ├── integration/
│       │   ├── audit/
│       │   ├── config/
│       │   └── common/
│       │
│       └── resources/
│           └── application.yml
│
├── face-service/
│   ├── app/
│   │   ├── detection/
│   │   ├── recognition/
│   │   ├── liveness/
│   │   ├── schemas/
│   │   └── routes/
│   ├── models/
│   ├── main.py
│   └── requirements.txt
│
├── sdk/
│   └── javascript/
│       ├── src/
│       ├── package.json
│       └── tsconfig.json
│
├── demo-client/
│   └── Next.js test application
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

## 3.2. Компоненты системы

### Frontend

Единое веб-приложение NextFace.

Содержит:
- Landing Page.
- Registration.
- Login.
- Face Scanner.
- Dashboard.
- Profile Settings.
- Developer Portal.
- Application Details.

### Backend

Одно Spring Boot приложение с модульной архитектурой.

Модули:

1. Auth Module.
2. User Module.
3. Face Module.
4. Application Module.
5. Integration Module.
6. Audit Module.

Не разделять эти модули на отдельные Spring Boot приложения.

### Face Service

Одно Python-приложение FastAPI.

Внутри него находятся:
- Face Detection.
- Face Embedding Generation.
- Face Matching.
- Liveness Detection.

Не создавать отдельные Python-сервисы для каждой функции.

### Database

Одна PostgreSQL база данных для всех компонентов NextFace.

Face Service не должен напрямую подключаться к PostgreSQL. Все операции сохранения и получения пользовательских биометрических шаблонов выполняются через Spring Boot Backend.

---

# 4. Component Communication

## 4.1. Frontend → Backend

Frontend взаимодействует с Backend через REST API.

Пример:

```text
Frontend
   |
   | POST /api/auth/register
   | Content-Type: application/json
   |
   v
Spring Boot Backend
   |
   v
PostgreSQL
```

Frontend не должен напрямую обращаться к PostgreSQL.

## 4.2. Backend → Face Service

Backend взаимодействует с Python Face Service через HTTP REST API.

Пример:

```text
Spring Boot
    |
    | POST /face/embedding
    | Image data
    |
    v
FastAPI
    |
    v
Face Recognition Model
    |
    v
Embedding
    |
    v
Spring Boot
```

Face Service возвращает результаты анализа, но не управляет пользователями и не выдаёт JWT.

## 4.3. Backend → PostgreSQL

Spring Boot использует Spring Data JPA для работы с базой данных.

Все операции с пользователями, приложениями, биометрическими профилями и authorization codes выполняются через Backend.

## 4.4. Общая схема

```text
                  USER
                   |
                   v
             NEXTFACE FRONTEND
                   |
                   | REST API
                   v
             SPRING BOOT
              /         \
             /           \
            v             v
       POSTGRESQL     FACE SERVICE
                           |
                           v
                    FACE RECOGNITION
```

---

# 5. Database Design

Создать следующие таблицы.

## 5.1. users

Хранит пользовательские аккаунты.

Поля:

- id: UUID, Primary Key.
- email: VARCHAR, Unique, Not Null.
- password_hash: VARCHAR, Not Null.
- created_at: TIMESTAMP.
- updated_at: TIMESTAMP.
- enabled: BOOLEAN.

## 5.2. face_profiles

Хранит биометрические шаблоны.

Поля:

- id: UUID, Primary Key.
- user_id: UUID, Foreign Key, Unique.
- embedding: BYTEA.
- model_version: VARCHAR.
- created_at: TIMESTAMP.
- updated_at: TIMESTAMP.

Не сохранять фотографии лица без необходимости.

Embedding является чувствительными биометрическими данными. Ограничить доступ и защитить данные при хранении.

## 5.3. applications

Хранит сторонние приложения, использующие NextFace.

Поля:

- id: UUID, Primary Key.
- owner_id: UUID, Foreign Key.
- name: VARCHAR.
- client_id: VARCHAR, Unique.
- client_secret_hash: VARCHAR.
- redirect_url: VARCHAR.
- created_at: TIMESTAMP.
- enabled: BOOLEAN.

Client Secret генерируется при создании приложения.

Показывать его владельцу только при создании или при явном перевыпуске.

## 5.4. authorization_codes

Хранит временные коды авторизации.

Поля:

- id: UUID, Primary Key.
- user_id: UUID, Foreign Key.
- application_id: UUID, Foreign Key.
- code_hash: VARCHAR.
- expires_at: TIMESTAMP.
- used: BOOLEAN.
- created_at: TIMESTAMP.

Коды:
- Одноразовые.
- Имеют короткий срок действия.
- Хранятся в виде хэша.
- Привязаны к конкретному приложению.
- Не могут использоваться повторно.

## 5.5. audit_logs

Хранит историю событий.

Поля:

- id: UUID, Primary Key.
- user_id: UUID, Foreign Key, Nullable.
- application_id: UUID, Foreign Key, Nullable.
- event_type: VARCHAR.
- success: BOOLEAN.
- created_at: TIMESTAMP.

Не записывать в логи пароли, JWT, Client Secret, изображения лица или embeddings.

---

# 6. Authentication Logic

## 6.1. Registration

Пользователь регистрируется через email и пароль.

Последовательность:

1. Пользователь открывает Registration Page.
2. Вводит email и пароль.
3. Frontend отправляет POST /api/auth/register.
4. Backend проверяет корректность данных.
5. Backend проверяет уникальность email.
6. Пароль хэшируется через BCrypt.
7. Создаётся пользователь в PostgreSQL.
8. Backend возвращает результат регистрации.
9. Пользователь переходит к регистрации лица.

После успешной регистрации можно выдать JWT либо предложить пользователю выполнить вход.

## 6.2. Password Login

1. Пользователь вводит email и пароль.
2. Frontend отправляет POST /api/auth/login.
3. Backend получает пользователя из PostgreSQL.
4. Backend проверяет пароль через BCrypt.
5. При успешной проверке генерирует JWT.
6. Frontend получает токен и создаёт пользовательскую сессию.

## 6.3. JWT

Использовать JWT для авторизации запросов к NextFace Backend.

Требования:

- Подписывать токены безопасным ключом.
- Указывать expiration.
- Проверять подпись и срок действия.
- Не помещать секретную информацию в payload.
- Защищать API через Spring Security.
- Использовать короткоживущий access token.

Для учебного прототипа можно реализовать refresh token отдельно, если он действительно понадобится.

---

# 7. Face Recognition Logic

## 7.1. Face Detection

Использовать MediaPipe для:
- Обнаружения лица.
- Определения ключевых точек.
- Проверки положения лица в кадре.
- Проверки того, что в кадре находится одно лицо.

Если лицо не найдено, вернуть понятную ошибку.

## 7.2. Face Embedding

Использовать отдельную модель распознавания лиц, совместимую с ONNX Runtime.

Алгоритм:

1. Получить изображение.
2. Найти лицо.
3. Выполнить выравнивание лица.
4. Подготовить изображение для модели.
5. Получить embedding.
6. Нормализовать embedding.
7. Вернуть embedding Backend.

Использовать одну и ту же модель и одинаковую предобработку при регистрации и авторизации.

Зафиксировать версию модели.

## 7.3. Face Matching

Для сравнения использовать cosine similarity или другой метод, предусмотренный выбранной моделью.

Результат сравнения должен содержать:

- similarity score.
- match result.
- model version.

Порог совпадения вынести в конфигурацию.

Не выбирать порог произвольно и не считать его универсальным: проверить на тестовых примерах.

## 7.4. Liveness Detection

Реализовать простую демонстрационную проверку живого присутствия.

Возможные действия:

- Повернуть голову влево.
- Повернуть голову вправо.
- Моргнуть.

Использовать MediaPipe landmarks для анализа движения.

ВАЖНО:

Это базовая демонстрация Liveness, а не полноценная защита от профессиональных атак с фотографиями, видео или deepfake.

---

# 8. Face Enrollment Flow

Регистрация биометрического профиля.

```text
USER
 |
 v
Login / Registration
 |
 v
Open Face Scanner
 |
 v
Request Camera Permission
 |
 v
Capture Face
 |
 v
Frontend sends image to Backend
 |
 v
Backend calls Face Service
 |
 v
Face Detection
 |
 v
Face Alignment
 |
 v
Generate Embedding
 |
 v
Backend receives Embedding
 |
 v
Save Embedding in PostgreSQL
 |
 v
Enrollment Successful
```

Правила:

- Требовать авторизованную пользовательскую сессию.
- Разрешать регистрацию только одного лица.
- Не принимать кадры без лица.
- Не принимать несколько лиц.
- Проверять качество изображения.
- Не перезаписывать существующий embedding без явного подтверждения.
- При повторной регистрации корректно обновлять биометрический профиль.

---

# 9. Face Login Flow

```text
USER
 |
 v
Open NextFace Login
 |
 v
Select Face Login
 |
 v
Request Camera Permission
 |
 v
Capture Face
 |
 v
Send Image to Backend
 |
 v
Backend calls Face Service
 |
 v
Generate Embedding
 |
 v
Load Stored Embedding
 |
 v
Compare Embeddings
 |
 +---- No Match ----> Authentication Failed
 |
 +---- Match
         |
         v
   Generate JWT
         |
         v
   Authentication Successful
```

Правила:

- Пользователь должен существовать.
- У пользователя должен быть зарегистрирован Face Profile.
- При отсутствии лица возвращать ошибку.
- При низком качестве изображения предлагать повторить сканирование.
- При несовпадении не выдавать JWT.
- Ограничить количество попыток.
- Записывать результат в audit_logs.

---

# 10. Third-party Integration

## 10.1. Основная идея

Разработчик стороннего сайта должен иметь возможность интегрировать NextFace максимально просто.

Все пользовательские аккаунты и биометрические данные находятся на стороне NextFace.

Сторонний сайт не должен получать:
- Пароли.
- Фотографии лица.
- Face embeddings.
- Внутренние пользовательские данные NextFace.

Он получает только разрешённые данные пользователя и подтверждение успешной авторизации.

## 10.2. Developer Portal

Создать раздел Developer Portal.

Функции:

- Создание приложения.
- Просмотр Client ID.
- Генерация Client Secret.
- Настройка Redirect URL.
- Просмотр подключённых приложений.
- Удаление приложения.
- Перевыпуск Client Secret.

При регистрации приложения обязательно проверять Redirect URL.

Не разрешать произвольные redirect адреса.

## 10.3. Authorization Flow

```text
THIRD-PARTY WEBSITE
        |
        | User clicks Login with NextFace
        |
        v
NEXTFACE HOSTED LOGIN PAGE
        |
        v
USER AUTHENTICATION
        |
        v
FACE RECOGNITION
        |
        v
AUTHENTICATION SUCCESS
        |
        v
GENERATE ONE-TIME AUTHORIZATION CODE
        |
        v
REDIRECT TO REGISTERED REDIRECT URL
        |
        v
THIRD-PARTY BACKEND
        |
        | Exchange authorization code
        |
        v
NEXTFACE BACKEND
        |
        | Validate code and application
        |
        v
RETURN USER IDENTITY DATA
        |
        v
THIRD-PARTY BACKEND CREATES ITS OWN SESSION
```

Authorization code должен:
- Быть случайным и криптографически стойким.
- Иметь короткий срок действия.
- Использоваться только один раз.
- Быть связанным с конкретным приложением.
- Храниться в БД в виде хэша.

Client Secret используется только серверной частью стороннего сайта.

Не передавать Client Secret в браузер.

## 10.4. JavaScript SDK

Создать небольшой SDK:

```typescript
import { NextFace } from "@nextface/sdk";

const nextface = new NextFace({
    clientId: "YOUR_CLIENT_ID"
});

nextface.login();
```

SDK выполняет только:
- Проверку конфигурации.
- Формирование URL авторизации.
- Перенаправление на NextFace.
- При необходимости обработку возврата пользователя.

Не выполнять распознавание лица внутри SDK.

Не хранить биометрические данные на стороне стороннего сайта.

Для прототипа SDK может быть минимальным JavaScript/TypeScript-пакетом.

---

# 11. REST API

## 11.1. Spring Boot API

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Registration |
| POST | /api/auth/login | Password login |
| GET | /api/auth/me | Current user |
| POST | /api/auth/logout | Logout |

### Face

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/face/enroll | Register face |
| POST | /api/face/login | Login using face |
| DELETE | /api/face/profile | Delete face profile |

### Applications

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/applications | List applications |
| POST | /api/applications | Create application |
| GET | /api/applications/{id} | Application details |
| DELETE | /api/applications/{id} | Delete application |

### Integration

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/integration/authorize | Start authorization |
| POST | /api/integration/token | Exchange authorization code |
| GET | /api/integration/userinfo | Get authorized user data |

### User

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/users/me | User profile |
| PUT | /api/users/me | Update profile |
| DELETE | /api/users/me | Delete account |

## 11.2. Python Face Service API

| Method | Endpoint | Description |
|---|---|---|
| POST | /face/detect | Detect face |
| POST | /face/embedding | Generate embedding |
| POST | /face/compare | Compare embeddings |
| POST | /face/liveness | Basic liveness check |

Face Service не должен выдавать JWT или самостоятельно создавать пользователей.

---

# 12. Frontend Pages

## 12.1. Public Pages

### Landing Page

- Описание NextFace.
- Основные преимущества.
- Как работает система.
- Кнопка Register.
- Кнопка Login.
- Кнопка Developers.

### Registration Page

- Email.
- Password.
- Confirm Password.
- Validation.
- Link to Login.

### Login Page

- Email and Password Login.
- Face Login.
- Link to Registration.

## 12.2. Face Scanner

Основной экран биометрической авторизации.

Элементы:

- Camera Preview.
- Face Frame.
- Scanning Animation.
- Face Detection Status.
- Liveness Instructions.
- Success State.
- Error State.
- Cancel Button.

Возможные состояния:

1. Waiting for camera.
2. Camera permission requested.
3. Camera active.
4. Face not detected.
5. Face detected.
6. Adjust your position.
7. Liveness check.
8. Recognition in progress.
9. Authentication successful.
10. Authentication failed.

## 12.3. Dashboard

- User Information.
- Account Status.
- Face Profile Status.
- Connected Applications.
- Recent Login Activity.
- Security Settings.

## 12.4. Developer Portal

- Applications List.
- Create Application.
- Application Details.
- Client ID.
- Client Secret.
- Redirect URL.
- Integration Instructions.

## 12.5. Profile Settings

- Change Email.
- Change Password.
- Delete Face Profile.
- Delete Account.
- Logout.

---

# 13. UI/UX Requirements

Проект является прототипом для предмета Human-Computer Interaction.

Особое внимание уделить взаимодействию пользователя с интерфейсом.

Обязательные принципы:

- Простая навигация.
- Понятные названия элементов.
- Минимальное количество шагов.
- Чёткая обратная связь.
- Видимые состояния загрузки.
- Понятные ошибки.
- Подтверждение важных действий.
- Возможность отменить сканирование.
- Адаптивный дизайн.
- Доступность интерфейса с клавиатуры.

Особое внимание уделить Face Scanner.

Пользователь всегда должен понимать:
- Что сейчас делает система.
- Видит ли камера его лицо.
- Правильно ли он расположен.
- Идёт ли проверка.
- Успешна ли авторизация.
- Почему произошла ошибка.

---

# 14. Security Requirements

Даже для учебного прототипа соблюдать следующие правила:

- Пароли хранить только в виде BCrypt-хэшей.
- JWT подписывать секретным ключом.
- Проверять expiration JWT.
- Не хранить Client Secret в frontend.
- Не передавать embeddings сторонним сайтам.
- Проверять Redirect URL.
- Использовать одноразовые authorization codes.
- Ограничивать срок действия кодов.
- Ограничивать количество попыток Face Login.
- Проверять входные данные.
- Не записывать секреты и биометрию в логи.
- Ограничить доступ к Face Service.
- Не разрешать прямой доступ браузера к внутреннему Face Service.

Биометрические embeddings необходимо защищать при хранении и передаче.

Для реального использования необходимо учитывать GDPR и требования к обработке биометрических данных. Учебный прототип не следует представлять как production-ready систему.

---

# 15. Development Roadmap

Все задачи должны выполняться последовательно.

AI-разработчик должен самостоятельно обновлять чек-листы в этом документе после реализации и проверки каждой задачи.

Статусы:
- [ ] Not Started.
- [x] Completed and tested.
- [~] In Progress.
- [!] Blocked / Needs attention.

Не ставить [x], если:
- Код только написан, но не проверен.
- Endpoint не протестирован.
- Компонент не запускается.
- Интеграция не проверена.
- Функция реализована только частично.

## Phase 1 — Project Setup

- [ ] Создать структуру проекта.
- [ ] Инициализировать Next.js.
- [ ] Инициализировать Spring Boot.
- [ ] Инициализировать FastAPI.
- [ ] Настроить PostgreSQL.
- [ ] Настроить конфигурацию окружения.
- [ ] Настроить CORS.
- [ ] Проверить запуск всех компонентов.
- [ ] Создать базовый README.

**Definition of Done:** все приложения запускаются и могут взаимодействовать.

## Phase 2 — Backend Authentication

- [ ] Создать сущность User.
- [ ] Создать UserRepository.
- [ ] Создать Registration DTO.
- [ ] Создать Login DTO.
- [ ] Реализовать регистрацию.
- [ ] Добавить BCrypt.
- [ ] Реализовать Login.
- [ ] Реализовать JWT generation.
- [ ] Реализовать JWT validation.
- [ ] Настроить Spring Security.
- [ ] Реализовать GET /api/auth/me.
- [ ] Проверить регистрацию и вход через API.

**Definition of Done:** пользователь может зарегистрироваться и авторизоваться через JWT.

## Phase 3 — Face Service

- [ ] Создать FastAPI приложение.
- [ ] Подключить OpenCV.
- [ ] Подключить MediaPipe.
- [ ] Настроить Face Detection.
- [ ] Проверять количество лиц в кадре.
- [ ] Проверять качество изображения.
- [ ] Выбрать модель распознавания лиц.
- [ ] Проверить лицензию модели.
- [ ] Настроить ONNX Runtime.
- [ ] Реализовать Face Alignment.
- [ ] Реализовать Embedding Generation.
- [ ] Реализовать Embedding Normalization.
- [ ] Реализовать Face Matching.
- [ ] Реализовать базовый Liveness.
- [ ] Добавить обработку ошибок.
- [ ] Протестировать API Face Service.

**Definition of Done:** Face Service способен обнаруживать лицо, создавать embedding и сравнивать два шаблона.

## Phase 4 — Face Enrollment

- [ ] Создать FaceProfile entity.
- [ ] Создать FaceProfileRepository.
- [ ] Реализовать POST /api/face/enroll.
- [ ] Проверять JWT пользователя.
- [ ] Получать изображение от frontend.
- [ ] Передавать изображение в Face Service.
- [ ] Получать embedding.
- [ ] Сохранять embedding в PostgreSQL.
- [ ] Реализовать обновление Face Profile.
- [ ] Реализовать удаление Face Profile.
- [ ] Проверить полный сценарий регистрации лица.

**Definition of Done:** зарегистрированный пользователь может привязать своё лицо к аккаунту.

## Phase 5 — Face Authentication

- [ ] Реализовать POST /api/face/login.
- [ ] Получать изображение лица.
- [ ] Генерировать embedding.
- [ ] Получать сохранённый Face Profile.
- [ ] Выполнять Face Matching.
- [ ] Настроить threshold.
- [ ] Обрабатывать отсутствие совпадения.
- [ ] Обрабатывать отсутствие Face Profile.
- [ ] Реализовать ограничение попыток.
- [ ] Генерировать JWT при успешном распознавании.
- [ ] Записывать результат в audit_logs.
- [ ] Проверить успешную и неуспешную авторизацию.

**Definition of Done:** пользователь может войти в NextFace посредством распознавания лица.

## Phase 6 — Frontend

- [ ] Создать Landing Page.
- [ ] Создать Registration Page.
- [ ] Создать Login Page.
- [ ] Реализовать формы регистрации.
- [ ] Реализовать формы входа.
- [ ] Реализовать JWT session handling.
- [ ] Создать Camera Component.
- [ ] Реализовать получение разрешения камеры.
- [ ] Создать Face Scanner UI.
- [ ] Реализовать состояния сканирования.
- [ ] Подключить Face Enrollment.
- [ ] Подключить Face Login.
- [ ] Создать Dashboard.
- [ ] Создать Profile Settings.
- [ ] Создать Connected Applications.
- [ ] Создать Developer Portal.
- [ ] Проверить адаптивность интерфейса.
- [ ] Проверить обработку ошибок.

**Definition of Done:** основные функции доступны через графический интерфейс.

## Phase 7 — Third-party Integration

- [ ] Создать Application entity.
- [ ] Создать ApplicationRepository.
- [ ] Реализовать регистрацию приложений.
- [ ] Генерировать Client ID.
- [ ] Генерировать Client Secret.
- [ ] Хранить Client Secret в виде хэша.
- [ ] Реализовать Redirect URL validation.
- [ ] Создать AuthorizationCode entity.
- [ ] Реализовать генерацию authorization code.
- [ ] Реализовать expiration.
- [ ] Реализовать одноразовое использование кода.
- [ ] Реализовать проверку Client ID.
- [ ] Реализовать проверку Client Secret.
- [ ] Реализовать обмен authorization code.
- [ ] Реализовать выдачу разрешённых данных пользователя.
- [ ] Создать минимальный JavaScript SDK.
- [ ] Реализовать метод login().
- [ ] Создать demo-client.
- [ ] Проверить полный сценарий интеграции.

**Definition of Done:** сторонний сайт может использовать NextFace для авторизации пользователя.

## Phase 8 — Testing and Finalization

- [ ] Проверить регистрацию пользователя.
- [ ] Проверить вход по паролю.
- [ ] Проверить JWT.
- [ ] Проверить регистрацию лица.
- [ ] Проверить вход по лицу.
- [ ] Проверить отсутствие лица.
- [ ] Проверить несколько лиц в кадре.
- [ ] Проверить низкое качество изображения.
- [ ] Проверить Liveness.
- [ ] Проверить неправильный пароль.
- [ ] Проверить истёкший JWT.
- [ ] Проверить неправильный authorization code.
- [ ] Проверить повторное использование кода.
- [ ] Проверить неправильный Redirect URL.
- [ ] Проверить интеграцию demo-client.
- [ ] Проверить защиту API.
- [ ] Исправить обнаруженные ошибки.
- [ ] Обновить README.
- [ ] Добавить инструкцию запуска проекта.

**Definition of Done:** все основные пользовательские сценарии работают и проверены.

---

# 16. AI Developer Instructions

Этот раздел обязателен для AI-разработчика.

При работе над NextFace следовать следующим правилам.

## 16.1. General Rules

1. Перед началом работы изучить текущую структуру проекта.
2. Прочитать этот PROJECT_PLAN.md полностью.
3. Проверить, какие задачи уже выполнены.
4. Не переписывать существующий рабочий код без необходимости.
5. Не создавать дублирующие классы и компоненты.
6. Соблюдать существующую архитектуру.
7. Не добавлять технологии, которых нет в Technology Stack, без необходимости.
8. Не усложнять проект.
9. Не реализовывать несколько крупных этапов одновременно.
10. После завершения каждого этапа проверить его работоспособность.

## 16.2. Task Management

После выполнения каждой задачи:

1. Проверить результат.
2. Запустить необходимые тесты.
3. Проверить взаимодействие компонентов.
4. Обновить соответствующий checkbox в PROJECT_PLAN.md.
5. При необходимости добавить краткое примечание о реализации.

Не отмечать задачу выполненной, если она не была проверена.

Если задача частично выполнена, использовать [~].

Если задача заблокирована, использовать [!] и написать причину.

## 16.3. Architecture Rules

- Frontend общается с Backend через REST API.
- Backend общается с Face Service через HTTP.
- Backend является единственным компонентом, который работает с PostgreSQL.
- Face Service отвечает только за обработку и анализ изображений.
- JWT выдаётся только Spring Boot Backend.
- Все пользовательские данные принадлежат NextFace.
- SDK не выполняет распознавание лиц.
- Сторонние сайты не получают биометрические данные.
- Не использовать OAuth Server или дополнительные инфраструктурные компоненты.

Для интеграции использовать простой собственный механизм одноразовых authorization codes, описанный в разделе 10.

## 16.4. Code Quality

- Использовать понятные имена классов и методов.
- Разделять Controller, Service и Repository.
- Использовать DTO для API.
- Не помещать бизнес-логику в Controller.
- Добавлять обработку ошибок.
- Валидировать входящие запросы.
- Не дублировать бизнес-логику.
- Соблюдать TypeScript typing.
- Использовать Pydantic schemas в FastAPI.
- Добавлять тесты для критических сценариев.

## 16.5. Work Process

Работать небольшими итерациями.

Для каждой итерации:

1. Определить конкретную задачу.
2. Изучить связанные файлы.
3. Реализовать задачу.
4. Проверить результат.
5. Обновить чек-лист.
6. Сообщить, что сделано и что осталось.

Не переходить к следующему крупному этапу, если текущий содержит критические ошибки.

Если обнаружено противоречие между этим документом и существующей реализацией, сначала описать проблему и предложить решение, а не молча менять архитектуру.

---

# 17. Project Completion Criteria

Проект считается завершённым, когда:

- [ ] Пользователь может зарегистрировать аккаунт.
- [ ] Пользователь может войти через email и пароль.
- [ ] Пользователь может зарегистрировать лицо.
- [ ] Система может распознать зарегистрированного пользователя.
- [ ] Система выдаёт JWT после успешной авторизации.
- [ ] Пользователь может управлять своим профилем.
- [ ] Разработчик может зарегистрировать стороннее приложение.
- [ ] Разработчик может получить Client ID и Client Secret.
- [ ] Сторонний сайт может перенаправить пользователя в NextFace.
- [ ] NextFace может вернуть одноразовый authorization code.
- [ ] Сторонний сервер может обменять код на разрешённые данные пользователя.
- [ ] SDK обеспечивает простой запуск процесса авторизации.
- [ ] Demo Client демонстрирует успешную интеграцию.
- [ ] Основные сценарии протестированы.
- [ ] README содержит инструкцию запуска.

---

# 18. Current Project Status

AI Developer должен поддерживать этот раздел в актуальном состоянии.

**Current Phase:** Not Started

**Last Completed Task:** None

**Current Task:** None

**Known Issues:** None

**Next Recommended Action:** Phase 1 — Project Setup

**Last Updated:** Not Started

После каждой рабочей сессии обновлять этот раздел.

Не удалять историю выполненных задач и не сбрасывать уже отмеченные checkbox.

---

# END OF PROJECT PLAN
