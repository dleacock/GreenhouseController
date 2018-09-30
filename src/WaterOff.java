import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
public class WaterOff extends Event  implements Serializable {
    private GreenhouseControls gc;
    public WaterOff(GreenhouseControls gc, long eventTime) {
        super (gc, eventTime);
        this.gc = gc;
    }
    public void action() {
       gc.addEventString(toString());
       gc.setVariable("Water", false);
    }
    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() {
        return "Greenhouse water is off\n\r";
    }
}