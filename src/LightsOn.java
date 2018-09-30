import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
public class LightsOn extends Event implements Serializable  {
 GreenhouseControls gc;
    public LightsOn(GreenhouseControls gc, long eventTime) {
        super (gc, eventTime);
        this.gc = gc;
    }
    public void action() {
        gc.addEventString(toString());
        gc.setVariable("Lights", true);
    }

    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() { return "Light is on\n\r"; }
}