# DataForge AI - Интеллектуальная система формирования датасетов


## Сервисы

### Backend (Java Spring Boot)
- **База данных:** PostgreSQL 18 / H2
- **Основные функции:**
  - JWT аутентификация пользователей
  - CRUD операции с датазиториями (проектами)
  - Загрузка и хранение файлов (Cloudinary / S3 / MinIO)
  - Управление участниками и ролями
  - Интеграция с ML сервисом через REST

### ML Service (Python FastAPI)
- **Основные функции:**
  - Извлечение эмбеддингов
  - Оценка качества меток
  - Расчет энтропии и уверенности модели
  - Поиск дубликатов и аномалий
  - Генерация тегов и дорожной карты
  - Вычисление интегральной полезности объектов

### Frontend (React + TypeScript + Vite)
- **Основные функции:**
    - Регистрация и авторизация
    - Создание и управление датазиториями
    - Drag-and-drop загрузка данных
    - Визуализация аналитики (графики, таблицы)
    - Работа с очередью проверки объектов
    - Экспорт очищенного датасета

## Требования

- **Docker** 20.10+ (для запуска через Compose)
- **Java** 21 (для ручного запуска backend)
- **Python** 3.9+ (для ручного запуска ML сервиса)
- **Node.js** 18+ (для ручного запуска frontend)
- **PostgreSQL** 16 (опционально, при ручном запуске)

## Запуск

### 1. Через Docker Compose

```bash
# настроить .env файл
cp .env.example .env

# Запустить все сервисы
docker-compose up -d

# Остановить
docker-compose down
```

### 2. Ручной запуск (Не рекомендуется, только если Docker не подгружает зависимости)
```bash
# backend
./gradlew bootRunDev

# ml-service
cd .\ml-service\
python -m venv venv
venv/Scripts/activate
pip install -r requirements.txt
uvicorn main:app --reload

# frontend
cd .\frontend\
npm install
npm run dev
```

После этого UI будет доступен по адресу: http://localhost:5173/login