import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
public class WaterOn extends Event implements Serializable  {
    private GreenhouseControls gc;
    public WaterOn(GreenhouseControls gc, long eventTime) {
        super (gc, eventTime);
        this.gc = gc;
    }
    public void action() {
       gc.addEventString(toString());
       gc.setVariable("Water", true);
    }
    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() {
        return "Greenhouse water is on\n\r";
    }
}