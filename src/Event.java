import java.io.Serializable;

// Make Event implement Runnable so that each event handles its own timing. Removed
// the "start()" and "ready()" functions from previous iteration of this code. The run method
// handles both.
// The Event class has two distinct loops running within it.  The run() method represents
// the thread itself and begins the moment it is created. Although we want the thread to be
// running the moment it's created we don't want the event itself as a controller to be running.
// Within the run() method it checks to see if the user has hit the start button and we can then
// then assume the greenhouse controller system has begun and now the clock can start. In order
// to allow events to be suspended, restarted and resumed we place a lock on the event to use
// the boolean values that represent the different states an event can be.

public abstract class Event implements Runnable, Serializable{
    private boolean eventStarted;
    private boolean eventThreadRunning;
    private long eventTime;
    private long delayTime;
    private boolean newEvent = true;
    private GreenhouseControls greenhouseControls;

    public Event( GreenhouseControls greenhouseControls, long delayTime){
        this.greenhouseControls = greenhouseControls;
        this.delayTime = delayTime;
        this.eventStarted = false;
        this.eventThreadRunning = true;
    }

    public abstract void updateController(GreenhouseControls gc);

    public long getDelayTime() {
        return delayTime;
    }

    public void run(){

        synchronized (this){
            // Ensure that even though the thread is running at creation we
            // want the events to begin with the click of the 'Start' button.
            // Wait until we are notified to begin run() again.
            if(newEvent){
                try {
                    newEvent = false;
                    wait();
                } catch (Exception e){
                    System.out.println("Exception called in event wait()" + e.toString());
                }
            }
            // This is the run within the run() loop.
            // The logic of what the event is supposed to do.
            // This loop will carry on as long as the thread is active.
            // If the thread shuts down this is passed and run() returns
            // and the thread ends.
            while(eventThreadRunning){
                // If the controller client is suspended then wait.
                if(greenhouseControls.isSuspended()){
                    try {
                        wait();
                    } catch (Exception e){
                        System.out.println("Exception called in event wait()" + e.toString());
                    }
                }

                if(greenhouseControls.isAborted()){
                    return;
                }

                // Check to see if start button has been clicked.
                // If so the clock has started and we can now check if
                // the event is ready to be active and run the action()
                // method. If we are a repeating event then update the
                // event time based on the interval. Otherwise set
                // the variables to allow the run() method to return and
                // the thread to die.
                if(eventStarted){
                    if(eventReady()){
                        try {
                            action();
                            stopEvent();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // Starting an event we need to know if this is a new
    // event or we are resuming an old event.
    public void startEvent(boolean isNewEvent){
        synchronized (this) {
            eventTime = System.currentTimeMillis() + delayTime;
            eventStarted = true;
            eventThreadRunning = true;
            if(isNewEvent){
                this.newEvent = true;
            } else {
                this.newEvent = false;
            }
            notifyAll();
        }
    }

    // Helper method to ensure the variables that
    // determine what stage a thread is in are set to
    // allow the run() method to return.
    private void stopEvent(){
        eventThreadRunning = false;
        eventStarted = false;
        notifyAll();

    }

    // Helper method to determine if event is ready
    // to call action()
    private boolean eventReady(){
        if(System.currentTimeMillis() >= eventTime)
            return true;
        return false;
    }


    // Let the threads die off by allowing the run() method to return.
    public void eventRunOut(){
        synchronized (this){
            stopEvent();
            notifyAll();
        }
    }

    // The Event-inherited class must implement it's own action.
    public abstract void action();
}



