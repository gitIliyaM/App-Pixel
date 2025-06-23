#  User Management System

<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.1.0-green?logo=spring" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-21-red?logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-6-red?logo=redis" alt="Redis">
</div>

##  Описание проекта

Современная система управления пользователями с расширенными возможностями:

-  **Безопасная аутентификация** через JWT токены
-  **Финансовые операции** между пользователями
-  **Управление контактами** (email и телефоны)
-  **Гибкий поиск** пользователей с фильтрацией
-  **Мониторинг** работы системы

##  Технологический стек

### Основные технологии
| Компонент | Назначение | Версия |
|-----------|------------|--------|
| Java | Основной язык разработки | 21 |
| Spring Boot | Бэкенд-фреймворк | 3.1.0 |
| PostgreSQL | Основная база данных | 15 |
| Redis | Кэширование и сессии | 6 |

### Дополнительные компоненты

 Дополнительные технологии
    "Spring Security"
    "Liquibase"
    "MapStruct"
    "JJWT"
    "Testcontainers"
    "SpringDoc"
    "Lombok"

 Особенности реализации

 Безопасность
+ Двухфакторная аутентификация (Access + Refresh токены)
+ Хеширование паролей через BCrypt
+ Пессимистичная блокировка при денежных переводах

 Быстрый старт
Вариант 1: Docker

docker-compose up --build -d

Вариант 2: Локальный запуск

1. Настройте окружение:
   cp .env.example .env
2. Соберите и запустите:
   mvn spring-boot:run

 API Документация
Метод	Путь	          Описание
POST	/api/auth/login	  Аутентификация пользователя
GET	    /api/users	      Поиск пользователей
POST	/api/transfers	  Перевод средств между счетами

 Интерактивная документация: /swagger-ui.html

 Тестовые данные
{
"email": "user@mail.ru",
"phone": "79201234567",
"password": "test123",
"balance": 1000.00
}

 Мониторинг
 /actuator/health - Проверка состояния

 /actuator/metrics - Метрики производительности

 /actuator/cache - Статистика кэширования

 Архитектурные решения
Миграции БД:

Liquibase для управления схемой

XML-файлы в src/main/resources/db/changelog

Конфигурация:

Настройки в application.yaml и .env

Поддержка Docker и локального окружения

Логирование:

Запись в logs/application.log

Гибкая настройка уровней



 
