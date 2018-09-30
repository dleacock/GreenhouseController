import java.io.Serializable;

/**
 * Created by David Leacock on 11/26/2017.
 */
public class ThermostatDay extends Event implements Serializable  {
    private GreenhouseControls gc;
    public ThermostatDay(GreenhouseControls gc,long eventTime) { super (gc,  eventTime);
        this.gc = gc;}
    public void action() {
        gc.addEventString(toString());
        gc.setVariable("Thermostat", "Day");
    }
    public void updateController(GreenhouseControls gc){
        this.gc = gc;
    }
    public String toString() {
        return "Thermostat on day setting\n\r";
    }
}
