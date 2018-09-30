import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
public class FansOn extends Event implements Serializable  {
    GreenhouseControls gc;
    public FansOn(GreenhouseControls gc, long eventTime) { super (gc, eventTime);
    this.gc = gc;}
    public void action(){
        gc.addEventString(toString());
        gc.setVariable("Fan", true);
    }

    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() { return "Fan is on\n\r"; }
}