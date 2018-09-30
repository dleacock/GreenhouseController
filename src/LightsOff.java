import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
public class LightsOff extends Event implements Serializable  {

    GreenhouseControls gc;
    public LightsOff(GreenhouseControls gc, long eventTime) { super (gc,  eventTime);
    this.gc = gc;}
    public void action() {
        gc.addEventString(toString());
        gc.setVariable("Lights", false);
    }

    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() { return "Light is off\n\r"; }
}