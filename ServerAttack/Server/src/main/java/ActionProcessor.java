import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class ActionProcessor {

    private final GameState state;
    private final MainServer server;

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

        state.addPlayer(
                Player.builder()
                        .id(src.getId())
                        .name(name)
                        .build()
        );

        JsonNode ackData = JsonUtils.toJsonNode(Map.of(
                "playerId",
                src.getId(),
                "progress",
                state.getProgress()
        ));

        Message ack = Message.of(
                MessageType.JOIN_ACK,
                msg.getSeq(),
                System.currentTimeMillis(),
                ackData);

        try {
            src.send(ack);
        } catch (IOException ignored) {}

        state.addLog(
                "MonitoringCenter",
                "JOIN",
                src.getId(),
                String.format("player joined: %s", name),
                false
        );
    }

    private void handleAction(Message msg, ClientHandler src) {
        JsonNode msgData = msg.getData();

        if (msgData == null || !msgData.has("action")) return;

        String actionType = msgData.get("actionType").asText();

        if ("COMPLETE_MINIGAME".equals(actionType)) {
            state.increaseProgress(10);
            state.addLog(
                    "Zone",
                    "MINIGAME_COMPLETE",
                    src.getId(),
                    "complete minigame",
                    false
            );
            JsonNode logData = JsonUtils.toJsonNode(Map.of(
                    "zone",
                    "Zone",
                    "eventType",
                    "MINIGAME_COMPLETE",
                    "actor",
                    src.getId()
            ));
            Message logMsg = Message.of(
                    MessageType.LOG_EVENT,
                    msg.getSeq(),
                    System.currentTimeMillis(),
                    logData
            );
            server.broadcast(logMsg);
            JsonNode snapshot = createSnapshot();
            Message upd = Message.of(
                    MessageType.STATE_UPDATE,
                    msg.getSeq(),
                    System.currentTimeMillis(),
                    snapshot
            );
            server.broadcast(upd);
        } else if ("START_MINIGAME".equals(actionType)) {
            state.addLog(
                    "Zone",
                    "MINIGAME_START",
                    src.getId(),
                    "started minigame",
                    false
            );
            JsonNode snapshot = createSnapshot();
            Message upd = Message.of(
                    MessageType.STATE_UPDATE,
                    msg.getSeq(),
                    System.currentTimeMillis(),
                    snapshot
            );
            server.broadcast(upd);
        }
    }

    public JsonNode createSnapshot() {
        return JsonUtils.toJsonNode(Map.of(
           "progress",
           state.getProgress(),
           "zones",
                state.listZones().stream()
                        .map(z -> Map.of(
                                "id",
                                z.getId(),
                                "load",
                                z.getLoad()
                        ))
                        .toList(),
           "players",
                state.listPlayers().stream()
                        .map(p -> Map.of(
                                "id",
                                p.getId(),
                                "name",
                                p.getName(),
                                "role",
                                p.getRole(),
                                "zone",
                                p.getZone()
                        ))
                        .toList()
        ));
    }
}
