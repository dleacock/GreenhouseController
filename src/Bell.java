/**
 * Created by David Leacock on 11/26/2017.
 */
import java.io.Serializable;

public class Bell extends Event implements Serializable {
    GreenhouseControls gc;
    public Bell(GreenhouseControls gc, long eventTime) {
        super (gc, eventTime);
        this.gc = gc;
    }
    public void action() {
        gc.addEventString(toString());
    }

    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() { return "Bing!\n\r"; }
}

