# Сетевая игра «2048»

Курсовой проект — клиент-серверное веб-приложение (БГУИР, кафедра ПОИТ).

## Стек

- **Клиент:** HTML, CSS, JavaScript
- **Сервер:** Java 17, Spring Boot 3.2
- **БД:** PostgreSQL (или MySQL по профилю `mysql`)
- **Аутентификация:** JWT

## Требования

- **JDK 17** (рекомендуется; с JDK 24 возможны ошибки сборки из-за Lombok)
- Maven
- PostgreSQL 14+ (или MySQL 8+)
- Браузер с поддержкой ES6

Если в IntelliJ IDEA сборка падает с ошибкой `TypeTag :: UNKNOWN`, выберите JDK 17: **File → Project Structure → SDKs → + → Download JDK** → Version **17**, Vendor **Eclipse Temurin**. Подробнее: `backend/INTELLIJ_JDK17.md`.

## Запуск

### 1. База данных

PostgreSQL:

```bash
createdb game2048
# пользователь/пароль — по умолчанию postgres/postgres (см. application.yml)
```

Или через Docker:

```bash
docker run -d --name pg2048 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=game2048 -p 5432:5432 postgres:15
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Сервер: `http://localhost:8080`, контекст API: `/api`.

Переменные окружения (опционально): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `SERVER_PORT`.

Для MySQL:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

(предварительно создать БД и при необходимости задать `DB_*`.)

### 3. Игра в браузере

Фронтенд встроен в backend (файлы в `backend/src/main/resources/static/`). После запуска приложения откройте в браузере:

**http://localhost:8080/** или **http://localhost:8080/index.html**

Игра и API работают с одного порта — отдельно поднимать фронтенд не нужно. Удобно запускать приложение в IntelliJ IDEA (Run `Game2048Application`) и сразу открывать игру по этому адресу.

**Важно:** открывайте игру именно по адресу `http://localhost:8080/` (после запуска backend). Если открыть файл `index.html` напрямую с диска (file://), стили и скрипты могут не подгрузиться и поле 4×4 не отобразится.

Папка `frontend/` — для разработки или запуска через Live Server / `npx serve`; тогда задайте `window.API_BASE_URL = 'http://localhost:8080/api'`.

## Функциональность

- Регистрация и вход (JWT).
- Игра 2048: стрелки или свайпы, счёт, лучший результат (локально).
- Сохранение результата игры в таблицу рекордов (для авторизованных).
- Таблица рекордов (топ по счёту).
- История своих игр в профиле.
- Адаптивная вёрстка под разные разрешения.

### Интеграция с лабораторными работами (КСИС)

| Лаба | Что добавлено в курсовой |
|------|--------------------------|
| **4** | Журналирование HTTP API (`http.access`: метод, URI, код, время, пользователь) |
| **4 (blacklist)** | `security-access.json`: чёрный список IP/путей, rate limit на `/api/auth/*` |
| **5** | REST-хранилище файлов: аватар (`PUT/GET /api/files/avatars/...`), сохранение партии (`PUT/GET /api/files/saves/me`), защита путей |
| **4 (прокси)** | **nginx** в `docker-compose` как reverse proxy перед Spring Boot |

## Запуск через Docker (рекомендуется)

```bash
cd курсач
docker compose up --build
```

- Игра через nginx: **http://localhost/**
- Backend напрямую: **http://localhost:8080/**
- PostgreSQL: порт `5432`

Файлы хранилища сохраняются в Docker volume `storage_data`.

### Настройка доступа (лаба 4)

Файл `backend/src/main/resources/security-access.json`:

```json
{
  "blocked_ips": ["203.0.113.10"],
  "blocked_paths": ["/api/admin"],
  "auth_rate_limit_per_minute": 30
}
```

При блокировке возвращается `403` (HTML или JSON).

## Структура проекта

```
Ксис/
├── backend/                 # Spring Boot
│   ├── src/main/java/...    # контроллеры, сервисы, сущности, безопасность
│   └── src/main/resources/  # application.yml, static/ (игра)
├── frontend/                # HTML, CSS, JS (копия для разработки)
│   ├── index.html
│   ├── styles.css
│   ├── game.js              # логика 2048
│   ├── api.js               # вызовы API
│   └── app.js               # UI и обработчики
└── README.md
```

## API (кратко)

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/auth/register` | Регистрация |
| POST | `/api/auth/login` | Вход |
| GET | `/api/records/top?limit=20` | Топ рекордов (без авторизации) |
| POST | `/api/records` | Сохранить результат (Bearer JWT) |
| GET | `/api/records/my` | Мои записи (Bearer JWT) |
| GET | `/api/profile` | Профиль (Bearer JWT) |
| GET | `/api/files/avatars/{userId}` | Аватар пользователя |
| PUT | `/api/files/avatars/me` | Загрузить аватар (multipart `file`) |
| PUT | `/api/files/saves/me` | Сохранить партию (JSON тела игры) |
| GET | `/api/files/saves/me` | Загрузить партию с сервера |

Срок сдачи: 26.05.2026. Пояснительная записка — по СТП 01-2024.
