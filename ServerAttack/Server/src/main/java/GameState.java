import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private final ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ZoneState> zones = new ConcurrentHashMap<>();
    @Getter
    private final LogManager logManager;
    @Getter
    private volatile int progress = 0;

    public GameState() {
        zones.put("BackupStorage", new ZoneState("BackupStorage"));
        zones.put("FirewallControl", new ZoneState("FirewallControl"));
        zones.put("RouterBay", new ZoneState("RouterBay"));
        zones.put("CoolingStation", new ZoneState("CoolingStation"));
        zones.put("MonitoringCenter", new ZoneState("MonitoringCenter"));
        this.logManager = new LogManager(this);
    }

    private Role determineRoleForNewPlayer() {
        int totalPlayers = players.size();
        int hackers = (int) players.values().stream()
                .filter(p -> p.getRole() == Role.HACKER)
                .count();

        int requiredHackers = (int) Math.floor((totalPlayers + 1) / 3.0);
        return hackers < requiredHackers ? Role.HACKER : Role.ENGINEER;
    }

    public void addPlayer(String id, String name) {
        Role role = determineRoleForNewPlayer();
        Player player = Player.builder()
                .id(id)
                .name(name)
                .role(role)
                .zone("MonitoringCenter")
                .connected(true)
                .build();
        players.put(id, player);

        addLog("MonitoringCenter",
                role == Role.HACKER ? "HACKER_JOIN" : "ENGINEER_JOIN",
                id,
                String.format("Игрок %s присоединился как %s", name, role == Role.HACKER ? "ХАКЕР" : "ИНЖЕНЕР"),
                false);
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public List<Player> listPlayers() {
        return new ArrayList<>(players.values());
    }

    public ZoneState getZone(String id) {
        return zones.get(id);
    }

    public List<ZoneState> listZones() {
        return new ArrayList<>(zones.values());
    }

    public void increaseProgress(int i) {
        progress = Math.min(100, Math.max(0, progress + i));
    }

    public Log addLog(String zoneId, String eventType, String actorId, String message, boolean tampered) {
        long logId = System.currentTimeMillis();
        Log log = new Log(logId, zoneId, eventType, actorId, message,
                "WARNING".equals(eventType) || "ALERT".equals(eventType));
        logManager.addLog(log);
        return log;
    }

    public List<Log> getRecentLogs(int limit) {
        return logManager.getRecentLogs("MonitoringCenter", limit);
    }

    public int getHackerCount() {
        return (int) players.values().stream()
                .filter(p -> p.getRole() == Role.HACKER)
                .count();
    }

    public int getEngineerCount() {
        return (int) players.values().stream()
                .filter(p -> p.getRole() == Role.ENGINEER)
                .count();
    }

}