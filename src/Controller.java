
/***********************************************************************
 * Adapated for COMP308 Java for Programmer, 
 *		SCIS, Athabasca University
 *
 * Assignment: TME4
 * @author: David Leacock
 * @date : Nov, 2017
 */

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;

public class Controller  {

  // The controllers internal collection of Runnable eventsCollection.
  private List<Event> eventsCollection = new ArrayList<>();
  // The thread pool from which event threads are created.
  private  ExecutorService eventExecutor = Executors.newCachedThreadPool();
  // The thread executor service.
  private  ThreadPoolExecutor eventThreadPoolExecutor = (ThreadPoolExecutor) eventExecutor;


  // In order to determine whether or not to popup a close/exit confirmation dialog
  private boolean isRunning = false;
  public boolean isRunning() {
      return isRunning;
  }


  // Events are added via reflection. The Event constructor is called
  // through a string and a event duration.
  public void addEvent(String className, long duration){
    try {
      Class<?> eventClass = Class.forName(className);
      Constructor<?> eventClassConstructor = eventClass.getConstructor(this.getClass(), long.class);
      Object instance = eventClassConstructor.newInstance(this, duration);

      addEventToExecutor((Event)instance);
      eventsCollection.add((Event)instance);

    } catch (Exception e){
        e.printStackTrace();
    }
  }
  private void addEventToExecutor(Event event){
      eventThreadPoolExecutor.execute(event);
  }
  public void start(){
    for(Event event : eventsCollection){
       event.startEvent(true);
    }
    isRunning = true;
  }

  // Before we restart we must let the events die out first.
  // Then re add them to the executor
  public void restart(){
    for(Event event : eventsCollection){
        event.eventRunOut();
    }
    for(Event event : eventsCollection){
        eventExecutor = Executors.newCachedThreadPool();
        eventThreadPoolExecutor = (ThreadPoolExecutor) eventExecutor;
        eventThreadPoolExecutor.execute(event);
        event.startEvent(false);
    }
      isRunning = true;
  }

  public void resume(){
    for(Event event : eventsCollection){
      event.startEvent(false);
    }
  }
  // Shut off the events by letting them die out via
  // letting their run() methods return.
  public void shutdownController(){
    for(Event event : eventsCollection){
            event.eventRunOut();
    }
    try {
      eventExecutor.shutdownNow();
    } catch (Exception e){
      e.printStackTrace();
    }
    isRunning = false;
  }

} ///:~
