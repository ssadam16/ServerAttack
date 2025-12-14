import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    private final ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ZoneState> zones = new ConcurrentHashMap<>();
    private final List<Log> logs = Collections.synchronizedList(new ArrayList<>());
    @Getter
    private volatile int progress = 0;
    private volatile long lastLogSeq = 0;

    public  GameState() {
        zones.put("BackupStorage", new ZoneState("BackupStorage"));
        zones.put("FirewallControl", new ZoneState("FirewallControl"));
        zones.put("RouterBay", new ZoneState("RouterBay"));
        zones.put("CoolingStation", new ZoneState("CoolingStation"));
        zones.put("MonitoringCenter", new ZoneState("MonitoringCenter"));
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public Player removePlayer(String id) {
        return players.remove(id);
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
        progress += i;
        if (progress > 100) progress = 100;
        if (progress < 0) progress = 0;
    }

    public Log addLog(String zoneId, String eventType, String actorId, String message, boolean tampered) {
        long seq = ++lastLogSeq;
        long time = System.currentTimeMillis();
        Log log = new Log(seq, time, zoneId, eventType, actorId, message, tampered);
        logs.add(log);
        if (logs.size() > 500) logs.remove(0);
        return log;
    }

    public List<Log> getRecentLogs(int limit) {
        int size = logs.size();
        if (size == 0) return new ArrayList<>();
        int from = Math.max(0, size - limit);
        return new ArrayList<>(logs.subList(from, size));
    }

}
