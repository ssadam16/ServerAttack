import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@UtilityClass
public class LogUtils {

    private final Random random = new Random();
    private final Map<String, AtomicLong> zoneLogCounters = new ConcurrentHashMap<>();

    // Типы событий для каждой зоны
    private final Map<String, List<String>> ZONE_EVENTS = Map.of(
            "BackupStorage", List.of(
                    "Запуск резервного копирования",
                    "Проверка целостности бэкапов",
                    "Очистка старых резервных копий",
                    "Ошибка записи на backup-носитель",
                    "Успешное завершение бэкапа"
            ),
            "FirewallControl", List.of(
                    "Сканирование входящих подключений",
                    "Блокировка подозрительного IP",
                    "Обновление правил фаервола",
                    "Обнаружена попытка сканирования портов",
                    "Автоматическая настройка правил доступа"
            ),
            "RouterBay", List.of(
                    "Маршрутизация пакетов: стабильно",
                    "Обновление таблиц маршрутизации",
                    "Проверка сетевых интерфейсов",
                    "Высокая нагрузка на маршрутизаторе",
                    "Перенаправление трафика на резервный канал"
            ),
            "CoolingStation", List.of(
                    "Температура CPU: в норме",
                    "Автоматическая регулировка вентиляторов",
                    "Проверка системы охлаждения",
                    "Предупреждение: температура выше нормы",
                    "Стабилизация температурного режима"
            ),
            "MonitoringCenter", List.of(
                    "Сбор метрик системы",
                    "Анализ сетевой активности",
                    "Генерация отчета о производительности",
                    "Мониторинг пользовательских сессий",
                    "Проверка системных логов"
            )
    );

    // Подозрительные события (будут вызывать тревогу)
    private final List<String> SUSPICIOUS_EVENTS = List.of(
            "Неавторизованный доступ к системным файлам",
            "Попытка отключения системы мониторинга",
            "Подозрительная активность в сетевом трафике",
            "Множественные неудачные попытки входа",
            "Изменение конфигурационных файлов без авторизации",
            "Аномальная нагрузка на процессор",
            "Попытка доступа к защищенным разделам"
    );

    public String generateLogEntry(String zoneId, String playerId, boolean isSuspicious) {
        if (isSuspicious) {
            // Генерируем подозрительное событие
            String event = SUSPICIOUS_EVENTS.get(random.nextInt(SUSPICIOUS_EVENTS.size()));
            return String.format("[%s] %s (Игрок: %s)",
                    getCurrentTimestamp(),
                    event,
                    playerId != null ? playerId.substring(0, 8) + "..." : "неизвестен"
            );
        } else {
            // Генерируем нормальное событие для зоны
            List<String> zoneEvents = ZONE_EVENTS.get(zoneId);
            if (zoneEvents == null) return "";

            String event = zoneEvents.get(random.nextInt(zoneEvents.size()));
            return String.format("[%s] %s", getCurrentTimestamp(), event);
        }
    }

    public boolean shouldGenerateSuspiciousEvent() {
        // 15% шанс на подозрительное событие
        return random.nextDouble() < 0.15;
    }

    public String getRandomZone() {
        String[] zones = {"BackupStorage", "FirewallControl", "RouterBay", "CoolingStation", "MonitoringCenter"};
        return zones[random.nextInt(zones.length)];
    }

    public String getCurrentTimestamp() {
        return java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public long getNextLogId(String zoneId) {
        return zoneLogCounters
                .computeIfAbsent(zoneId, k -> new AtomicLong(0))
                .incrementAndGet();
    }

    public String getPlayerShortId(String playerId) {
        return playerId != null && playerId.length() > 8
                ? playerId.substring(0, 8) + "..."
                : playerId;
    }
}