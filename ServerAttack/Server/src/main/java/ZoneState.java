import lombok.Getter;
import lombok.Setter;

@Getter
public class ZoneState {

    private final String id;
    @Setter
    private double load;

    public ZoneState(String id) {
        this.id = id;
        this.load = 0;
    }

    public void increaseLoad(double d) {
        load += d;
        if (this.load < 0) this.load = 0;
    }

}
