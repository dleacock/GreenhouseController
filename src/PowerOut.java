import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
// Malfunction event sets status and throws an
// exception with it's integer error code.
public class PowerOut extends Event implements Serializable {
    private GreenhouseControls gc;
    public PowerOut(GreenhouseControls gc, long eventTime) {
        super (gc, eventTime);
        this.gc = gc;

    }
    public void action() {
       gc.addEventString(toString());
       gc.setVariable("Power", false);
       gc.emergencyShutdown(1, getDelayTime());
    }
    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() { return "Power has gone out!\n\r";   }

}
