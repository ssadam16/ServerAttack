import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;

public class ActionProcessor {
    private final GameState state;
    private final MainServer server;

    public ActionProcessor(GameState state, MainServer server) {
        this.state = state;
        this.server = server;
    }

    public void process(Message msg, ClientHandler src) {
        MessageType type = msg.getTyped();

        if (type == MessageType.JOIN) {
            handleJoin(msg, src);
        } else if (type == MessageType.ACTION) {
            handleAction(msg, src);
        }
    }

    private void handleJoin(Message msg, ClientHandler src) {
        JsonNode msgData = msg.getData();
        String name = "player";

        if (msgData != null && msgData.has("name")) {
            name = msgData.get("name").asText();
        }

        state.addPlayer(src.getId(), name);
        Player player = state.getPlayer(src.getId());

        JsonNode ackData = JsonUtils.toJsonNode(Map.of(
                "playerId", src.getId(),
                "role", player.getRole().name(),
                "progress", state.getProgress()
        ));

        Message ack = Message.of(
                MessageType.JOIN_ACK,
                msg.getSeq(),
                System.currentTimeMillis(),
                ackData);

        try {
            src.send(ack);
        } catch (IOException ignored) {}
    }

    private void handleAction(Message msg, ClientHandler src) {
        JsonNode msgData = msg.getData();

        if (msgData == null || !msgData.has("actionType")) return;

        String actionType = msgData.get("actionType").asText();
        Player player = state.getPlayer(src.getId());

        if (player == null) return;

        if (player.getRole() == Role.ENGINEER) {
            handleEngineerAction(actionType, msg, src, player);
        } else if (player.getRole() == Role.HACKER) {
            handleHackerAction(actionType, msg, src, player);
        }
    }

    private void handleEngineerAction(String actionType, Message msg, ClientHandler src, Player player) {
        if ("COMPLETE_MINIGAME".equals(actionType)) {
            state.increaseProgress(10);
            state.addLog(
                    player.getZone(),
                    "MINIGAME_COMPLETE",
                    src.getId(),
                    "Инженер выполнил задание",
                    false
            );
            broadcastStateUpdate(msg.getSeq());

        } else if ("START_MINIGAME".equals(actionType)) {
            state.addLog(
                    player.getZone(),
                    "MINIGAME_START",
                    src.getId(),
                    "Инженер начал задание",
                    false
            );
            broadcastStateUpdate(msg.getSeq());
        }
    }

    private void handleHackerAction(String actionType, Message msg, ClientHandler src, Player player) {
        JsonNode data = msg.getData();

        if ("SABOTAGE".equals(actionType)) {
            String zoneId = data.has("zone") ? data.get("zone").asText() : player.getZone();
            ZoneState zone = state.getZone(zoneId);
            if (zone != null) {
                zone.increaseLoad(30 + Math.random() * 40);
                state.addLog(
                        zoneId,
                        "SABOTAGE",
                        src.getId(),
                        "Обнаружена подозрительная активность (саботаж)",
                        false
                );
                broadcastStateUpdate(msg.getSeq());
            }

        } else if ("DELETE_LOG".equals(actionType)) {
            String zoneId = data.has("zone") ? data.get("zone").asText() : player.getZone();
            if (state.getLogManager().canDeleteLog(src.getId())) {
                Log deletedLog = state.getLogManager().deleteLastLog(src.getId(), zoneId);
                if (deletedLog != null) {
                    broadcastStateUpdate(msg.getSeq());
                }
            }

        } else if ("TAMPER_LOG".equals(actionType)) {
            String zoneId = data.has("zone") ? data.get("zone").asText() : player.getZone();
            String newMessage = data.has("newMessage") ? data.get("newMessage").asText() : "Системная проверка завершена успешно";

            if (state.getLogManager().canTamperLog(src.getId())) {
                Log tamperedLog = state.getLogManager().tamperLog(src.getId(), zoneId, newMessage);
                if (tamperedLog != null) {
                    broadcastStateUpdate(msg.getSeq());
                }
            }

        } else if ("START_HACK".equals(actionType)) {
            state.addLog(
                    player.getZone(),
                    "HACK_STARTED",
                    src.getId(),
                    "Обнаружена попытка несанкционированного доступа",
                    false
            );
            broadcastStateUpdate(msg.getSeq());
        }
    }

    private void broadcastStateUpdate(long seq) {
        JsonNode snapshot = createSnapshot();
        Message upd = Message.of(
                MessageType.STATE_UPDATE,
                seq,
                System.currentTimeMillis(),
                snapshot
        );
        server.broadcast(upd);
    }

    public JsonNode createSnapshot() {
        return JsonUtils.toJsonNode(Map.of(
                "progress", state.getProgress(),
                "engineers", state.getEngineerCount(),
                "hackers", state.getHackerCount(),
                "zones", state.listZones().stream()
                        .map(z -> Map.of("id", z.getId(), "load", z.getLoad()))
                        .toList(),
                "players", state.listPlayers().stream()
                        .map(p -> Map.of(
                                "id", p.getId(),
                                "name", p.getName(),
                                "role", p.getRole().name(),
                                "zone", p.getZone()
                        ))
                        .toList(),
                "recentLogs", state.getRecentLogs(10).stream()
                        .map(l -> Map.of(
                                "id", l.getId(),
                                "time", l.getTimestamp(),
                                "zone", l.getZoneId(),
                                "event", l.getEventType(),
                                "message", l.getMessage(),
                                "tampered", l.isTampered()
                        ))
                        .toList()
        ));
    }
}