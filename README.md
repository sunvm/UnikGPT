# 🎮 UnikGPT - Чат с ИИ в Minecraft

## 📝 Описание
UnikGPT - это инновационный плагин для Minecraft, который интегрирует возможности искусственного интеллекта Google Gemini прямо в игровой чат. Общайтесь с ИИ, получайте ответы на вопросы и делитесь знаниями с другими игроками!

## ⚡ Основные возможности

### 🔒 Приватные вопросы
- **Формат:** `? ваш_вопрос`
- **Пример:** `? Какая сегодня погода в Москве?`
- **Результат:** Ответ приходит лично через `/w`
- **Применение:** Идеально для личных запросов

### 📢 Публичные вопросы
- **Формат:** `?! ваш_вопрос`
- **Пример:** `?! Расскажи интересный факт о Minecraft`
- **Результат:** Ответ виден всем игрокам
- **Применение:** Отлично подходит для общих обсуждений

## 🛠 Технические требования

### Зависимости
- ✅ OkHttp3 (HTTP запросы)
- ✅ Gson (обработка JSON)
- ✅ Google Cloud AI Language
- ✅ Плагины для команд `/w` и `!`

### ⚙️ Настройка
1. Получите API ключ Google Cloud
2. Укажите API_KEY в config.yml
3. Проверьте наличие необходимых плагинов

## ⚠️ Ограничения
- Требуется интернет-соединение
- Необходим API ключ Google Cloud
- Поддерживаются запросы на всех основных языках

## 🛡️ Безопасность
- ✅ Асинхронные API запросы
- ✅ Фильтрация команд из общего чата
- ✅ Безопасное хранение API ключа в config.yml

## 💡 Советы по использованию
1. Формулируйте вопросы чётко и конкретно
2. Используйте публичные вопросы для общей информации
3. Приватные вопросы - для личных запросов

## 🔧 Обработка ошибок
- Сообщение об ошибке: "Произошла ошибка при получении ответа от ИИ."
- Логирование ошибок в консоль сервера

---

# 🎮 UnikGPT - AI Chat in Minecraft

## 📝 Description
UnikGPT is an innovative Minecraft plugin that integrates Google Gemini's artificial intelligence capabilities directly into game chat. Chat with AI, get answers to your questions, and share knowledge with other players!

## ⚡ Key Features

### 🔒 Private Questions
- **Format:** `? your_question`
- **Example:** `? What's the weather like in London?`
- **Result:** Answer comes personally via `/w`
- **Usage:** Perfect for personal queries

### 📢 Public Questions
- **Format:** `?! your_question`
- **Example:** `?! Tell me an interesting fact about Minecraft`
- **Result:** Answer visible to all players
- **Usage:** Great for general discussions

## 🛠 Technical Requirements

### Dependencies
- ✅ OkHttp3 (HTTP requests)
- ✅ Gson (JSON processing)
- ✅ Google Cloud AI Language
- ✅ Plugins for `/w` and `!` commands

### ⚙️ Setup
1. Get a Google Cloud API key
2. Set API_KEY in config.yml
3. Verify required plugins are installed

## ⚠️ Limitations
- Internet connection required
- Google Cloud API key required
- Supports queries in all major languages

## 🛡️ Security
- ✅ Asynchronous API requests
- ✅ Command filtering from general chat
- ✅ Secure API key storage in config.yml

## 💡 Usage Tips
1. Formulate questions clearly and specifically
2. Use public questions for general information
3. Private questions for personal queries

## 🔧 Error Handling
- Error message: "An error occurred while getting AI response."
- Errors are logged to server console

---
*Created with ❤️ for the Minecraft community*