import java.io.*;
import java.util.*;
import java.util.List;

/**
 *  David Leacock
 *  TME 4 Programs
 *
 *  A Program to that controls the status of a Greenhouse.  GreenhouseController
 *  is a child of Controller and implements Serializable for debugging and log file
 *  purposes.  The controller is passed events from the gui containing the Event
 *  and it's start time. Also contains ability to handle errors in the form of a power outage
 *  and windows malfunctioning.
 *
 */

public class GreenhouseControls extends Controller implements Serializable {

    // The controller has a copy of the gui. They are coupled together so
    // they can communicate controller state information and the controller
    // can receive requests from the user via the gui.
    private transient GreenhouseGui gui;

    // These will be read by gui elements to
    // ascertain the state of the controller
    // If it's running or suspended or has been
    // given a file to read.
    private volatile boolean isSuspended;

    private int errorCode;

    // Log the time that a crash has happened.  When the crashed object is
    // reloaded this time can be used to not run the event that caused
    // the crash.
    private long crashErrorTimeMSec = 0;
    private long repeatingEventDelayMSec = 1000;

    private boolean aborted;

    // Constructor
    public GreenhouseControls(GreenhouseGui gui){
        this.isSuspended = false;
        this.gui = gui;
        this.aborted = false;
    }

    // Add the event to the parents event collection
    public void addEvent(String event, long duration){
        rawEventMap.put(event, duration);
        super.addEvent(event, duration);
    }

    // If the event is a repeating event then we add the events but
    // loop through the number of times its supposed to repeat based on
    // the greenhouse controller field repeating event delay msec.
    public void addRepeatingEvent(String event, long duration, int repeatAmount){
        for(int i = 0; i < repeatAmount; i++){
            rawEventMap.put(event, duration);
            super.addEvent(event, duration);
            duration += repeatingEventDelayMSec;
        }
    }

    // The Greenhouse gui and controller are coupled together
    // so they could send information back and forth. This
    // was a design choice. Its so the controller can update
    // the gui of new events/fixes/crashes.
    public void setGui(GreenhouseGui gui){
        this.gui = gui;
    }


    public boolean isAborted() {
        return aborted;
    }

    // Call parent class start() method.
    public void start(){
        super.start();
    }
    // Call parent class restart() method and resume greenhouse controller.
    public void restart(){
        aborted = false;
        isSuspended = false;
        super.restart();
    }
    // Getter and Setter for suspended boolean, used to suspend an event while keeping
    // run() method looping.
    public boolean isSuspended(){
         return isSuspended;
    }
    public void suspend(){
        isSuspended = true;
    }
    // Call parent class resume() and resume greenhouse controller.
    public void resume(){
        isSuspended = false;
        super.resume();
    }

    public void shutdown(){
        aborted = true;
        super.shutdownController();
    }

    // Outputs the current state of what is inside the greenhouse to the gui.
    public void outputGreenhouseStates(){
        Iterator<ControllerState> itr = getControllerState().iterator();
        while(itr.hasNext()){
            gui.updateEventText(itr.next().toString() + "\n\r");
        }
        gui.updateEventText("\n\r");
    }

    // Sets the state of a particular greenhouse item, or "event".
    public<E, S> void setVariable(E event, S state){
            Iterator<ControllerState> itr = getControllerState().iterator();
            while(itr.hasNext()){
                ControllerState cs = itr.next();
                if(cs.equals(event)){
                    cs.setState(state);
                    return;
                }
            }
            getControllerState().add(new ControllerState(event, state));
            return;
    }

    // Generic tuple class used to keep track of the state of
    // the items within a greenhouse. For example the Lights, or
    // Temperature setting.  Allows for the creation of any type
    // of greenhouse internal object and any type of state, whether
    // it be on/off, true/false, up/down, etc.
    public static class ControllerState <E, S> implements Serializable{
        // The event cannot change, it's the constant in the controller state.
        // Used for updating the state of the object as the controller moves across time.
        public final E event;
        public S state;

        public ControllerState(E e, S s){
            event = e;
            state = s;
        }
        // A Controller State is equal to another if their event types
        // are the same.
        public boolean equals(Object o){
            return event.equals(o);
        }
        // As time moves forward the object that is being controlled has it's value changed
        public void setState(S newState){
            state = newState;
        }

        // Output the state of the entire controller
        public String toString(){
            return "[" + event + " is " + state + "]";
        }

        public E getCSEvent() { return event; }
        public S getCSState() { return state; }
    }


    // A collection of states of the various objects within the greenhouse.
    private List<ControllerState> greenhouseControllerStates = new ArrayList<>();
    // An internal collection of the raw events as read from the event file. Used
    // to determine what events need to be re-added after an emergency crash.
    private Map<String, Long> rawEventMap = new HashMap<>();

    // In order to ensure that the collection of controller states is accurate
    // and the wrong order of events wasn't recorded we use a object locking mechanism
    public List<ControllerState> getControllerState(){
            return greenhouseControllerStates;
    }


    private void notifyGuiEmergencyShutdown(){
        String emergencyShutdownText = "**********************************************************\n\r";
        emergencyShutdownText +=       "                      Emergency Shutdown                  \n\r";
        emergencyShutdownText +=       "**********************************************************\n\r";
        emergencyShutdownText += "\n\r";
        emergencyShutdownText += "Displaying Current Greenhouse Status\n\n\r";

        gui.updateEventText(emergencyShutdownText);
        outputGreenhouseStates();
    }


    // The crashing event calls this and gives it it's error code
    // and relative time in which the crash occurred. This method constructs
    // an error report. Calls the parent class to shutdown. Notifies the gui
    // event text window with information. This method also serializes this controller
    // class and writes that information to a .out file which can be read later.
    public void emergencyShutdown(int errorCode, long errorTime){
        Date errorDate = new Date(System.currentTimeMillis());
        String errorMessage = "Shutting down Greenhouse Controls. Contact System Administrator\n" +
                "Error code: " + errorCode + " - " +
                " occurred " + errorDate;
        // Call parent class to start shutdown procedure
        this.aborted = true;
        this.shutdownController();
        this.errorCode = errorCode;
        crashErrorTimeMSec = errorTime;

        notifyGuiEmergencyShutdown();
        generateDebugLog(errorMessage);
        try {
            File outputFile = new File("dump.out");
            outputFile.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile, false));
            oos.writeObject(this);
            oos.close();
        } catch (IOException e){
            System.out.println("Exception during serialized file creation: " + e.toString());
        }
    }

    public void generateDebugLog(String eventError){
        try {
            PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("error.log")));
            out.println(eventError);
            out.close();
        } catch (FileNotFoundException e){
            System.out.println("Exception during debug log generation: " + e.toString());
        }
    }

    // Helper function to send text to the gui's event text area.
    public void addEventString(String event){
        gui.updateEventText(event);
    }


    // The fix methods set a new value in the ControllerState collection and reset the error code.
    public void fixPower() {
        for(ControllerState cs : greenhouseControllerStates){
            if(cs.getCSEvent().equals("Power")){
                if(cs.getCSState().equals(false)){
                    cs.setState(true);
                    try{
                        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("fix.log")));
                        out.println( "Power Back On. Resetting error code.");
                        errorCode = 0;
                        out.close();

                    } catch (FileNotFoundException e){
                        System.out.println("Exception during Power Fix: " + e.toString());
                    }
                }
            }
        }
    }

    public void repairWindow(){
        for(ControllerState cs : greenhouseControllerStates){
            if(cs.getCSEvent().equals("Window")){
                if(cs.getCSState().equals("Malfunctioned")){
                    cs.setState("Functional");
                    try{
                        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("fix.log")));
                        out.println( "Window Repaired. Resetting error code.");
                        errorCode = 0;
                        out.close();

                    } catch (FileNotFoundException e){
                        System.out.println("Exception during Window Fix: " + e.toString());
                    }
                }
            }
        }
    }

    // Check the error code that was passed to the controller from the event.
    // Based off that it determines what needs to be fixed and creates a log item.
    // If an event has been created by the user that doesn't have a corresponding
    // fix event then an exception is thrown. This was done instead of creating a
    // Fixable class that requires a fix() method. That was a design choice.
    public void fixGreenhouseController(){
        if(errorCode == 1){
            fixPower();
            gui.updateEventText("Power Fixed.\n\r");
        } else if (errorCode == 2){
            repairWindow();
            gui.updateEventText("Window Fixed.\n\r");
        } else {
            try{
                PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("fix_fail.log")));
                out.println( "Unable to repair issue. Please contact system admin. The crashing Event does not have" +
                        "a corresponding error code and fix method.");
                errorCode = 0;
                out.close();
            } catch (FileNotFoundException e){
                System.out.println("Exception fix attempt: " + e.toString());
            }
        }
    }

    // Go through the raw event as read by the event file and see which
    // haven't happened yet because there was a crash. We determine this based off
    // what time a crash had occurred.
    public void resumeAfterCrash(){
        aborted = false;
        Iterator eventMapIterator = rawEventMap.entrySet().iterator();
        while(eventMapIterator.hasNext()) {
            Map.Entry<String, Long> eventPair = (Map.Entry)eventMapIterator.next();
            if(eventPair.getValue() > crashErrorTimeMSec){
                this.addEvent(eventPair.getKey(), eventPair.getValue() - crashErrorTimeMSec);
            }
        }
        resume();
    }

} ///:~