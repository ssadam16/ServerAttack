import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogManager {

    private final List<Log> allLogs = new CopyOnWriteArrayList<>();
    private final Map<String, List<Log>> zoneLogs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final GameState gameState;

    private long lastBroadcastedLogId = 0;
    private static final long DELETE_LOG_COOLDOWN = 45000; // 45 секунд
    private static final long TAMPER_LOG_COOLDOWN = 60000; // 60 секунд

    private final Map<String, Long> playerCooldowns = new ConcurrentHashMap<>();

    public LogManager(GameState gameState) {
        this.gameState = gameState;
        initializeZones();
        startAutoLogGeneration();
    }

    private void initializeZones() {
        String[] zones = {"BackupStorage", "FirewallControl", "RouterBay", "CoolingStation", "MonitoringCenter"};
        for (String zone : zones) {
            zoneLogs.put(zone, new ArrayList<>());
        }
    }

    private void startAutoLogGeneration() {
        // Генерируем логи каждые 5 секунд
        scheduler.scheduleAtFixedRate(this::generateSystemLogs, 5, 5, TimeUnit.SECONDS);
    }

    private void generateSystemLogs() {
        if (gameState.listPlayers().isEmpty()) return;

        String[] zoneIds = {"BackupStorage", "FirewallControl", "RouterBay", "CoolingStation", "MonitoringCenter"};
        String randomZone = zoneIds[new Random().nextInt(zoneIds.length)];
        boolean isSuspicious = Math.random() < 0.2;

        String eventType = isSuspicious ? "WARNING" : "INFO";
        String actorId = null;

        // 50% шанс связать с реальным игроком
        if (Math.random() < 0.5 && !gameState.listPlayers().isEmpty()) {
            List<Player> players = gameState.listPlayers();
            Player randomPlayer = players.get(new Random().nextInt(players.size()));
            actorId = randomPlayer.getId();
        }

        String message = generateRandomLogMessage(randomZone, isSuspicious, actorId);
        long logId = System.currentTimeMillis();

        Log newLog = new Log(logId, randomZone, eventType, actorId, message, isSuspicious);
        addLog(newLog);
    }

    private String generateRandomLogMessage(String zoneId, boolean isSuspicious, String playerId) {
        if (isSuspicious) {
            String[] suspiciousEvents = {
                    "Обнаружен неавторизованный доступ",
                    "Аномальная сетевая активность",
                    "Попытка обхода фаервола",
                    "Подозрительный трафик с внешнего IP",
                    "Неудачные попытки входа",
                    "Изменение системных файлов"
            };
            String event = suspiciousEvents[new Random().nextInt(suspiciousEvents.length)];
            return event + (playerId != null ? " (игрок: " + playerId.substring(0, 8) + "...)" : "");
        } else {
            Map<String, String[]> zoneEvents = new HashMap<>();
            zoneEvents.put("BackupStorage", new String[]{
                    "Запуск резервного копирования",
                    "Проверка целостности бэкапов",
                    "Очистка старых резервных копий",
                    "Успешное завершение бэкапа"
            });
            zoneEvents.put("FirewallControl", new String[]{
                    "Сканирование входящих подключений",
                    "Блокировка подозрительного IP",
                    "Обновление правил фаервола"
            });
            zoneEvents.put("RouterBay", new String[]{
                    "Маршрутизация пакетов: стабильно",
                    "Обновление таблиц маршрутизации",
                    "Проверка сетевых интерфейсов"
            });
            zoneEvents.put("CoolingStation", new String[]{
                    "Температура CPU: в норме",
                    "Автоматическая регулировка вентиляторов",
                    "Проверка системы охлаждения"
            });
            zoneEvents.put("MonitoringCenter", new String[]{
                    "Сбор метрик системы",
                    "Анализ сетевой активности",
                    "Генерация отчета о производительности"
            });

            String[] events = zoneEvents.get(zoneId);
            return events != null ? events[new Random().nextInt(events.length)] : "Системное событие";
        }
    }

    public synchronized void addLog(Log logEntry) {
        allLogs.add(logEntry);
        zoneLogs.computeIfAbsent(logEntry.getZoneId(), k -> new ArrayList<>()).add(logEntry);

        // Ограничиваем количество логов
        if (allLogs.size() > 1000) {
            allLogs.remove(0);
        }

        List<Log> zoneList = zoneLogs.get(logEntry.getZoneId());
        if (zoneList.size() > 200) {
            zoneList.remove(0);
        }
    }

    public boolean canDeleteLog(String playerId) {
        Long lastDelete = playerCooldowns.get("DELETE_" + playerId);
        return lastDelete == null ||
                System.currentTimeMillis() - lastDelete > DELETE_LOG_COOLDOWN;
    }

    public boolean canTamperLog(String playerId) {
        Long lastTamper = playerCooldowns.get("TAMPER_" + playerId);
        return lastTamper == null ||
                System.currentTimeMillis() - lastTamper > TAMPER_LOG_COOLDOWN;
    }

    public Log deleteLastLog(String playerId, String zoneId) {
        if (!canDeleteLog(playerId)) return null;

        List<Log> logs = zoneLogs.get(zoneId);
        if (logs == null || logs.isEmpty()) return null;

        Log lastLog = logs.get(logs.size() - 1);
        if (lastLog.isDeleted()) return null;

        Log deletedLog = lastLog.toBuilder()
                .deleted(true)
                .tampered(true)
                .actorId(playerId)
                .build();

        logs.set(logs.size() - 1, deletedLog);
        playerCooldowns.put("DELETE_" + playerId, System.currentTimeMillis());

        // Лог о удалении
        Log deletionRecord = new Log(
                System.currentTimeMillis(),
                "MonitoringCenter",
                "ALERT",
                playerId,
                String.format("Запись лога удалена в зоне %s", zoneId),
                true
        );
        addLog(deletionRecord);

        return deletedLog;
    }

    public Log tamperLog(String playerId, String zoneId, String newMessage) {
        if (!canTamperLog(playerId)) return null;

        List<Log> logs = zoneLogs.get(zoneId);
        if (logs == null || logs.isEmpty()) return null;

        Log logToTamper = logs.get(logs.size() - 1);
        if (logToTamper.isDeleted() || logToTamper.isTampered()) return null;

        Log tamperedLog = logToTamper.toBuilder()
                .message(newMessage)
                .tampered(true)
                .actorId(playerId)
                .metadata(Map.of("originalMessage", logToTamper.getMessage()))
                .build();

        logs.set(logs.size() - 1, tamperedLog);
        playerCooldowns.put("TAMPER_" + playerId, System.currentTimeMillis());

        return tamperedLog;
    }

    public List<Log> getRecentLogs(String zoneId, int count) {
        List<Log> logs = zoneLogs.get(zoneId);
        if (logs == null) return new ArrayList<>();

        int from = Math.max(0, logs.size() - count);
        return new ArrayList<>(logs.subList(from, logs.size()));
    }

    public List<Log> getRecentLogsForBroadcast() {
        // Возвращаем логи, которые еще не отправлялись
        return allLogs.stream()
                .filter(l -> l.getId() > lastBroadcastedLogId)
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    public void markLogsAsBroadcasted(long lastId) {
        this.lastBroadcastedLogId = lastId;
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}