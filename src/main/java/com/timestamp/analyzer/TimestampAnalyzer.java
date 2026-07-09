package com.timestamp.analyzer;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimestampAnalyzer {

    // Поставщик временных меток (имитация источника данных)
    public static List<LocalDateTime> provideTimestamps() {
        List<LocalDateTime> timestamps = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Генерируем 20 меток с разными интервалами
        timestamps.add(now.minusSeconds(10));
        timestamps.add(now.minusSeconds(8));
        timestamps.add(now.minusSeconds(5));
        timestamps.add(now.minusSeconds(1));
        timestamps.add(now.plusSeconds(0));
        timestamps.add(now.plusSeconds(3));
        timestamps.add(now.plusSeconds(7));
        timestamps.add(now.plusSeconds(12));
        timestamps.add(now.plusSeconds(15));
        timestamps.add(now.plusSeconds(20));

        return timestamps;
    }

    // Метод для вычисления разницы между двумя метками (в секундах)
    public static long calculateDifference(LocalDateTime t1, LocalDateTime t2) {
        return Duration.between(t1, t2).getSeconds();
    }

    // Основной метод для выполнения расчетов с использованием CompletableFuture
    public static CompletableFuture<List<Long>> calculateDifferencesAsync(List<LocalDateTime> timestamps) {
        if (timestamps == null || timestamps.size() < 2) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        // Создаем список задач для всех пар меток (i, i+1)
        List<CompletableFuture<Long>> futures = IntStream.range(0, timestamps.size() - 1)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    LocalDateTime t1 = timestamps.get(i);
                    LocalDateTime t2 = timestamps.get(i + 1);

                    // Имитация "тяжелых" вычислений (для демонстрации параллельности)
                    try {
                        Thread.sleep(100); // небольшая задержка для наглядности
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    long diff = calculateDifference(t1, t2);
                    System.out.printf("Поток %s: обработал пару [%d, %d] -> разница: %d сек%n",
                            Thread.currentThread().getName(), i, i + 1, diff);
                    return diff;
                }))
                .toList();

        // Объединяем все CompletableFuture в один
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    // Метод для вычисления средней разницы
    public static double calculateAverage(List<Long> differences) {
        if (differences == null || differences.isEmpty()) {
            return 0.0;
        }
        return differences.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    // Главный метод
    public static void main(String[] args) {
        System.out.println("=== Анализ временных меток ===\n");

        // 1. Чтение данных от поставщика
        List<LocalDateTime> timestamps = provideTimestamps();
        System.out.println("Получено временных меток: " + timestamps.size());
        System.out.println("Метки: " + timestamps + "\n");

        // 2. Параллельные вычисления с CompletableFuture
        try {
            CompletableFuture<List<Long>> futureDifferences = calculateDifferencesAsync(timestamps);

            // 3. Ожидание завершения всех вычислений и получение результатов
            List<Long> differences = futureDifferences.get();

            // 4. Вывод результатов
            System.out.println("\n=== Результаты вычислений ===");
            System.out.println("Разницы между соседними метками (сек): " + differences);

            // 5. Вычисление и вывод средней разницы
            double average = calculateAverage(differences);
            System.out.printf("Средняя разница между временными метками: %.2f сек%n", average);

            // Дополнительная статистика
            if (!differences.isEmpty()) {
                long max = differences.stream().max(Long::compareTo).orElse(0L);
                long min = differences.stream().min(Long::compareTo).orElse(0L);
                System.out.println("Максимальная разница: " + max + " сек");
                System.out.println("Минимальная разница: " + min + " сек");
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Ошибка при выполнении параллельных вычислений: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}