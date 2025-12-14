import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private String type;
    @Setter
    private long seq;
    private long timestamp;
    private JsonNode data;

    public static Message of(MessageType type, long seq, long timestamp, JsonNode data) {
        return new Message(type.name(), seq, timestamp, data);
    }

    public MessageType getTyped() {
        try {
            return MessageType.valueOf(type);
        } catch (Exception e) {
            return MessageType.ERROR;
        }
    }

}
