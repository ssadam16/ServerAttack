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
    private Role role;  // Изменено с String на Role
    @Setter
    private String zone;
    @Setter
    private boolean connected;

    // Дефолтные значения для builder
    public static class PlayerBuilder {
        private Role role = Role.ENGINEER;  // По умолчанию инженер
        private String zone = "MonitoringCenter";
        private boolean connected = true;
    }
}