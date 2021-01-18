/**
 * class Process
 *
 * @author Federico Cedolini
 * @version 1.0
 * @since 11/06/2020
 * Known issues: none
 */
import java.util.*;
import java.io.*;
public class Process{

  char name;
  LinkedList<Integer> BurstBlock = new LinkedList<Integer>();
  int priority;
  int numPreempted = 0;
  int arrivalTime;
  int exitTime;
  int readyWaiting = 0;

  /**
   * constructor
   * @param name name of the process
   * @param priority Priority level
   * @param arrivalTime time that the process enters they System
   */
  public Process (char name, int priority, int arrivalTime){
    this.name = name;
    this.priority = priority;
    this.arrivalTime = arrivalTime;
  }

  /**
   * Add burst and blocked time
   * @param time time of burst/blocked
   */
  public void addBurst(int time){
    BurstBlock.add(time);
  }

  /**
   * this method update current burst
   * @return true if process changes state
   */
  public boolean subTic(){
    int curr = BurstBlock.getFirst() - 1;
    if (curr == 0) {
      BurstBlock.removeFirst();
      numPreempted = 0;
      return true;
    }
    BurstBlock.set(0,curr);
    return false;
  }

  /**
   * This method checks if the process if done
   * @return true if done, false otherwise
   */
  public boolean isDone(){
    if (BurstBlock.size() == 0)
      return true;
    return false;
  }

  /**
   * Sets time of exit
   * @param clock is the current clock time of the system
   */
  public void setExitTime(int clock){
    exitTime = clock;
  }

  /**
   * exitTime accessor
   * @return exit time of process
   */
  public int getExitTime(){
    return exitTime;
  }

  /**
   * arrivalTime accessor
   * @return arrival time of process
   */
  public int getArrivalTime(){
    return arrivalTime;
  }

  /**
   * Increases the number of times the process was preempted
   * @return the number of times preempted
   */
  public int preempted(){
    return ++numPreempted;
  }

  /**
   * Priority accessor
   * @return priority level
   */
  public int getPriority(){
    return priority;
  }

  /**
   * Lowers the priority level of the process
   */
  public void lowerPriority(){
    priority++;
  }

  /**
   * Increases priority level of the process
   */
  public void increasePriority(){
    priority--;
  }

  /**
   * Accessor for readyWaiting
   * @return time spent on ready queue
   */
  public int getReadyWaiting(){
    return readyWaiting;
  }

  /**
   * updates the time waited on ready queue
   */
  public void waiting(){
    readyWaiting++;
  }

  /**
   * Name accessor
   * @return name of the process
   */
  public char getName(){
    return name;
  }
}
