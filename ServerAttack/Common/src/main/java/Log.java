import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Log {
    private long id;
    private long timestamp;
    private String zoneId;
    private String eventType;
    private String actorId;
    private String message;
    private boolean tampered;
    private boolean deleted;
    private boolean suspicious;
    private Map<String, Object> metadata;

    public Log(long id, String zoneId, String eventType, String actorId, String message, boolean suspicious) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
        this.zoneId = zoneId;
        this.eventType = eventType;
        this.actorId = actorId;
        this.message = message;
        this.tampered = false;
        this.deleted = false;
        this.suspicious = suspicious;
        this.metadata = new HashMap<>();
    }

    public String toDisplayString() {
        String time = java.time.Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        String actor = actorId != null ? " [" + actorId.substring(0, Math.min(8, actorId.length())) + "..." + "]" : "";
        String status = tampered ? " [ИЗМЕНЕНО]" : deleted ? " [УДАЛЕНО]" : "";

        return String.format("[%s] %s%s: %s%s",
                time, zoneId, actor, message, status);
    }
}