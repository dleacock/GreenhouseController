import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
public class Terminate extends Event implements Serializable {
    GreenhouseControls gc;
    public Terminate(GreenhouseControls gc,long eventTime) { super(gc, eventTime);
    this.gc= gc;
    }
    public void action() {
        gc.addEventString(toString());
        gc.shutdown();
    }
    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() { return "Terminating";  }
}