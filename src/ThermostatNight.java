import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
public class ThermostatNight extends Event implements Serializable {
    private GreenhouseControls gc;
    public ThermostatNight(GreenhouseControls gc, long eventTime) { super (gc, eventTime);
    this.gc = gc;}
    public void action() {
      gc.addEventString(toString());
     gc.setVariable("Thermostat", "night");
    }

    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }

    public String toString() {
        return "Thermostat on night setting\n\r";
    }
}