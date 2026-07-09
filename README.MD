# Timestamp Analyzer

Анализ временных меток с использованием CompletableFuture.

## Описание
Программа считывает временные метки, вычисляет разницу между соседними метками
параллельно с использованием CompletableFuture и выводит среднюю разницу.

## Технологии
- Java 17
- Maven
- CompletableFuture
- Git

## Запуск 
```bash
# Перейдите в корневую папку проекта
cd C:\Users\kalba\IdeaProjects\timestamp-analyzer

# Сборка проекта
mvn clean compile

# Запуск программы
mvn exec:java -Dexec.mainClass="com.timestamp.analyzer.TimestampAnalyzer"