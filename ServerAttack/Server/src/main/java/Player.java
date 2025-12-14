import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class Player {

    private final String id;
    @Setter
    private String name;
    @Setter
    private String role;
    @Setter
    private String zone;
    @Setter
    private boolean connected;

}
