import lombok.*;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Log {

    private long seq;
    private long timestamp;
    private String zoneId;
    private String eventType;
    private String actorId;
    private String message;
    private boolean tampered;

}
