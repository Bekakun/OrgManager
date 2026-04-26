<div align="center">

# 🏢 OrgManager

**Веб-система управления организацией**  
HR-модуль · Электронный документооборот · RBAC · Аудит

---

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](https://getbootstrap.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)

---

### Запустить одной командой — Java не нужна, только Docker Desktop

[![▶ Запустить через Docker](https://img.shields.io/badge/▶%20%20Запустить%20через%20Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white&labelColor=1D63ED)](#-быстрый-запуск-через-docker)

</div>

---

## 📋 Содержание

- [О проекте](#-о-проекте)
- [Возможности](#-возможности)
- [Технологический стек](#-технологический-стек)
- [Архитектура](#-архитектура)
- [Быстрый запуск через Docker](#-быстрый-запуск-через-docker)
- [Ручная установка](#-ручная-установка-для-разработки)
- [Структура проекта](#-структура-проекта)
- [Доступы по умолчанию](#-доступы-по-умолчанию)
- [Матрица прав (RBAC)](#-матрица-прав-rbac)
- [Workflow документов](#-workflow-документов)
- [API документация](#-api-документация)
- [Безопасность](#-безопасность)

---

## 🎯 О проекте

**OrgManager** — корпоративная веб-система для автоматизации HR-процессов и электронного документооборота. Система позволяет управлять сотрудниками, организовывать процессы согласования документов и контролировать доступ через ролевую модель (RBAC).

**Целевая аудитория:** HR-отдел, руководители подразделений, сотрудники компании.

---

## ✨ Возможности

### 👥 HR-модуль
- Полный CRUD для сотрудников и отделов
- Фильтрация по отделу и роли
- Мягкое удаление (деактивация) без потери истории
- Привязка к отделам и назначение руководителей

### 📄 Документооборот
- Создание документов с прикреплением файлов (PDF, Word, Excel, изображения)
- Управление версиями документов
- Полный жизненный цикл документа

### 🔄 Workflow согласования
- Конечный автомат статусов: `ЧЕРНОВИК → СОГЛАСОВАНИЕ → СОГЛАСОВАН / ОТКЛОНЁН`
- Назначение нескольких согласующих
- Комментарии к решениям
- Возврат на доработку с инкрементом версии

### 🔐 Безопасность
- Ролевая модель RBAC (ADMIN / MANAGER / EMPLOYEE)
- Двойная аутентификация: JWT для REST API + сессии для веб-интерфейса
- BCrypt хеширование паролей (cost = 12)
- Защита от SQL-инъекций через параметризованные JPA-запросы
- CSRF-защита в веб-интерфейсе

### 📋 Аудит
- Автоматическое логирование всех критических действий через Spring AOP
- Неизменяемые записи журнала с пагинацией
- Отслеживание: кто / что / когда

---

## 🛠 Технологический стек

| Слой | Технология | Версия |
|------|-----------|--------|
| **Backend** | Java + Spring Boot | 21 / 3.3.4 |
| **ORM** | Spring Data JPA / Hibernate | 6.x |
| **База данных** | PostgreSQL | 15 |
| **Миграции БД** | Flyway | 10.x |
| **Безопасность** | Spring Security | 6.x |
| **JWT** | jjwt | 0.12.6 |
| **Шаблоны** | Thymeleaf + Security Extras | 3.x |
| **CSS-фреймворк** | Bootstrap | 5.3.2 |
| **Иконки** | Bootstrap Icons | 1.11.3 |
| **AOP / Аудит** | Spring AOP | 6.x |
| **Сборка** | Maven | 3.x |
| **Контейнеризация** | Docker + Docker Compose | — |
| **API-документация** | Springdoc OpenAPI (Swagger) | 2.6.0 |
| **Lombok** | Project Lombok | — |

---

## 🏗 Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                        БРАУЗЕР                              │
│                 (Thymeleaf + Bootstrap 5)                   │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP / Session cookie
┌───────────────────────▼─────────────────────────────────────┐
│                  SPRING BOOT APP (:8080)                    │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ Web Chain    │  │  API Chain   │  │  Spring Security  │  │
│  │ /login       │  │  /api/**     │  │  @PreAuthorize   │  │
│  │ Form Login   │  │  JWT Bearer  │  │  RBAC            │  │
│  │ Session      │  │  Stateless   │  └──────────────────┘  │
│  └──────┬───────┘  └──────┬───────┘                        │
│         │                 │                                 │
│  ┌──────▼─────────────────▼──────────────────────────────┐ │
│  │              Service Layer                             │ │
│  │  UserService │ DocumentService │ WorkflowService       │ │
│  │  AuditService (AOP @Auditable)                        │ │
│  └──────────────────────┬─────────────────────────────────┘ │
│                         │                                   │
│  ┌──────────────────────▼─────────────────────────────────┐ │
│  │         Spring Data JPA / Hibernate (Repositories)     │ │
│  └──────────────────────┬─────────────────────────────────┘ │
└─────────────────────────┼───────────────────────────────────┘
                          │ JDBC
┌─────────────────────────▼───────────────────────────────────┐
│                  PostgreSQL 15                               │
│  users │ departments │ documents │ document_approvals        │
│  audit_logs                                                  │
└─────────────────────────────────────────────────────────────┘
```

### Workflow статусов документа

```
  ┌─────────┐  submit()   ┌─────────┐  все одобрили  ┌──────────┐
  │  DRAFT  │ ──────────► │ PENDING │ ──────────────► │ APPROVED │
  └─────────┘             └────┬────┘                 └──────────┘
       ▲                       │ кто-то отклонил
       │  returnToDraft()       ▼
       │  (version++)    ┌──────────┐
       └──────────────── │ REJECTED │
                         └──────────┘
```

---

## 🐳 Быстрый запуск через Docker

> **Требования:** только [Docker Desktop](https://www.docker.com/products/docker-desktop/)  
> Java, Maven, PostgreSQL — **не нужны**

### Шаг 1 — Клонировать или скачать проект

```bash
git clone <url-репозитория>
cd OrgManager
```

### Шаг 2 — Запустить

```bash
docker compose up --build
```

> Первый запуск ~3–5 минут (скачивает образы и собирает JAR).  
> Последующие запуски — секунды.

### Шаг 3 — Открыть браузер

```
http://localhost:8080
```

---

### Другие команды Docker

```bash
# Запустить в фоне (detached mode)
docker compose up --build -d

# Посмотреть логи приложения
docker compose logs -f app

# Остановить (данные сохраняются)
docker compose down

# Полный сброс (удалить всё вместе с базой данных)
docker compose down -v

# Пересобрать образ после изменений в коде
docker compose up --build
```

### Что создаётся

```
┌─────────────────────────────────────────┐
│         docker compose up               │
│                                         │
│  orgmanager-db   (postgres:15-alpine)   │
│  orgmanager-app  (eclipse-temurin:21)   │
│                                         │
│  Volumes:                               │
│    postgres_data  — данные БД           │
│    uploads        — загруженные файлы   │
└─────────────────────────────────────────┘
```

---

## 💻 Ручная установка (для разработки)

### Требования

| Инструмент | Версия |
|-----------|--------|
| Java (JDK) | 21+ |
| Maven | 3.9+ |
| PostgreSQL | 15+ |

### Настройка

**1. Создать базу данных**

```sql
CREATE DATABASE orgmanager;
```

**2. Настроить подключение**

Отредактировать `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url:      jdbc:postgresql://localhost:5432/orgmanager
    username: postgres
    password: ваш_пароль
```

**3. Запустить**

```bash
./mvnw spring-boot:run
```

**4. Открыть**

```
http://localhost:8080
```

---

## 📁 Структура проекта

```
OrgManager/
├── Dockerfile                          # Многоэтапная сборка (JDK → JRE)
├── docker-compose.yml                  # PostgreSQL + App
├── pom.xml                             # Maven зависимости
│
└── src/main/
    ├── java/kz/masku/orgmanager/
    │   ├── OrgManagerApplication.java
    │   ├── audit/
    │   │   ├── Auditable.java          # Маркер-аннотация для AOP
    │   │   └── AuditAspect.java        # @AfterReturning advice
    │   ├── config/
    │   │   ├── DataInitializer.java    # Создаёт admin при старте
    │   │   ├── SecurityConfig.java     # Dual filter chain (JWT + Form)
    │   │   └── OpenApiConfig.java      # Swagger + Bearer auth
    │   ├── controller/
    │   │   ├── AuthController.java     # POST /api/auth/login
    │   │   ├── UserController.java     # REST /api/users
    │   │   ├── DocumentController.java # REST /api/documents
    │   │   ├── AuditLogController.java # REST /api/audit-logs
    │   │   └── web/                    # Thymeleaf контроллеры
    │   │       ├── WebAuthController.java
    │   │       ├── WebDashboardController.java
    │   │       ├── WebUserController.java
    │   │       ├── WebDocumentController.java
    │   │       ├── WebAuditLogController.java
    │   │       └── GlobalModelAttributeAdvice.java
    │   ├── model/
    │   │   ├── entity/    # JPA сущности (User, Department, Document…)
    │   │   ├── enums/     # Role, UserStatus, DocumentStatus, ApprovalDecision
    │   │   └── dto/       # Request / Response / View записи
    │   ├── repository/    # Spring Data JPA интерфейсы
    │   ├── security/      # JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
    │   ├── service/       # UserService, DocumentService, WorkflowService, AuditService
    │   └── exception/     # GlobalExceptionHandler, кастомные исключения
    │
    └── resources/
        ├── application.yml
        ├── db/migration/
        │   └── V1__init_schema.sql     # DDL + индексы (Flyway)
        ├── static/css/app.css          # Кастомные стили
        └── templates/
            ├── fragments/commons.html  # Sidebar, head, alerts (фрагменты)
            ├── auth/login.html
            ├── dashboard/index.html
            ├── users/{list,form}.html
            ├── documents/{list,form,detail}.html
            └── audit/list.html
```

---

## 🔑 Доступы по умолчанию

| | |
|---|---|
| **URL** | `http://localhost:8080` |
| **Email** | `admin@orgmanager.kz` |
| **Пароль** | `Admin123!` |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |
| **OpenAPI JSON** | `http://localhost:8080/v3/api-docs` |

### Требования к паролю

Пароль должен содержать:
- ✅ Минимум **8 символов**
- ✅ Хотя бы одну **заглавную букву** (A–Z)
- ✅ Хотя бы одну **цифру** (0–9)
- ✅ Хотя бы один **спецсимвол** (`@`, `$`, `!`, `%`, `*`, `?`, `&`, `_`, `-`, `#`, …)

---

## 👥 Матрица прав (RBAC)

| Действие | EMPLOYEE | MANAGER | ADMIN |
|---------|:--------:|:-------:|:-----:|
| Просмотр своего профиля | ✅ | ✅ | ✅ |
| Просмотр сотрудников | ❌ | ✅ (свой отдел) | ✅ |
| Создание сотрудника | ❌ | ❌ | ✅ |
| Редактирование сотрудника | ❌ | ❌ | ✅ |
| Деактивация сотрудника | ❌ | ❌ | ✅ |
| Создание документа | ✅ | ✅ | ✅ |
| Просмотр документов | Свои | Свой отдел | Все |
| Отправка на согласование | Свои | ✅ | ✅ |
| Согласование / отклонение | ❌ | ✅ | ✅ |
| Возврат на доработку | ❌ | ✅ | ✅ |
| Просмотр журнала аудита | ❌ | ❌ | ✅ |
| Swagger UI | ❌ | ❌ | ✅ |

---

## 🔄 Workflow документов

### Статусы

| Статус | Цвет | Описание |
|--------|------|---------|
| `DRAFT` | ⬜ Серый | Черновик — редактируется автором |
| `PENDING` | 🟡 Жёлтый | На согласовании — ожидает решений |
| `APPROVED` | 🟢 Зелёный | Согласован — все одобрили |
| `REJECTED` | 🔴 Красный | Отклонён — хотя бы один отказал |

### Правило пересчёта статуса

```
Если хоть один approver → REJECTED   ⟹  документ = REJECTED
Если все approvers      → APPROVED   ⟹  документ = APPROVED
Иначе (есть PENDING)                 ⟹  документ = PENDING (ждём)
```

---

## 📖 API документация

REST API задокументировано через **Swagger UI**.  
Доступен только для роли `ADMIN`.

```
http://localhost:8080/swagger-ui.html
```

### Аутентификация в Swagger

1. POST `/api/auth/login` → получить `token`
2. Нажать **Authorize** → ввести `Bearer <token>`

### Основные эндпоинты

```
POST   /api/auth/login                           — Получить JWT токен

GET    /api/users                                — Список сотрудников
POST   /api/users                                — Создать сотрудника [ADMIN]
GET    /api/users/{id}                           — Профиль сотрудника
PUT    /api/users/{id}                           — Обновить сотрудника [ADMIN]
DELETE /api/users/{id}                           — Деактивировать [ADMIN]
GET    /api/users/me                             — Текущий пользователь

POST   /api/documents                            — Создать документ (multipart)
GET    /api/documents                            — Список документов
GET    /api/documents/{id}                       — Детали документа
POST   /api/documents/{id}/submit                — Отправить на согласование
POST   /api/documents/{id}/approvals/{aid}/decide — Принять решение [MANAGER/ADMIN]
POST   /api/documents/{id}/return-to-draft       — Вернуть на доработку [MANAGER/ADMIN]

GET    /api/audit-logs                           — Журнал аудита [ADMIN]
GET    /api/audit-logs/user/{userId}             — Аудит по пользователю [ADMIN]
GET    /api/audit-logs/entity/{type}/{id}        — Аудит по объекту [ADMIN]
```

---

## 🔐 Безопасность

| Аспект | Реализация |
|--------|-----------|
| Хеширование паролей | BCrypt (cost = 12) |
| Веб-аутентификация | Spring Security Form Login + HTTP Session |
| API-аутентификация | JWT (HS256, 24 ч.) через jjwt 0.12.x |
| CSRF | Включён для веб-интерфейса, отключён для `/api/**` |
| SQL-инъекции | Параметризованные JPA-запросы (нет нативного SQL) |
| RBAC | `@PreAuthorize` на уровне методов |
| Аудит | Spring AOP `@AfterReturning` на `@Auditable`-методах |
| Пароли | Минимум 8 символов, заглавная буква, цифра, спецсимвол |

---