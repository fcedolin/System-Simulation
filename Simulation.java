/**
 * class Simulation
 *
 * @author Federico Cedolini
 * @version 1.0
 * @since 11/09/2020
 * Known issues: none
 */
import java.util.*;
import java.io.*;
import java.lang.String;

public class Simulation{

  static LinkedList<Process> inputList = new LinkedList<Process>();
  static LinkedList<Process> HPQueue = new LinkedList<Process>();
  static LinkedList<Process> L2Queue = new LinkedList<Process>();
  static LinkedList<Process> L3Queue = new LinkedList<Process>();
  static LinkedList<Process> BlockedQueue = new LinkedList<Process>();
  static LinkedList<Process> outList = new LinkedList<Process>();
  static LinkedList<Character> ganttOutput = new LinkedList<Character>();
  static final int MAXPREEMTED = 3;

  public static void main(String[] args) {
    int clock = -1;
    char processName;
    String processPriority;
    int priority;
    int arrival;
    Process workingProcess = new Process('I', 0, 0);
    int burst;
    boolean idle = true;
    int queueWithReady;
    int workingQuant = 0;
    String ganttOutput = new String();

    Scanner s = new Scanner(System.in);
    int L2quant = s.nextInt();
    int L3quant = s.nextInt();

    //Get input from file
    System.out.println("Starting input processing...");
    while(s.hasNext()){
      processName = s.next().charAt(0);
      processPriority = s.next();
      if (processPriority.contains("HP"))
        priority = 1;
      else
        priority = 2;
      arrival = s.nextInt();
      workingProcess = new Process(processName, priority, arrival);
      while((burst = s.nextInt()) != -1)
        workingProcess.addBurst(burst);
      inputList.add(workingProcess);
    }

    System.out.println("Starting Simulation...");
    while(thingsLeft(idle) != 0){
      clock++;
      advanceTimeReady();
      NewJob(clock);
      advanceTimeBlock();
      queueWithReady = anyProcessReady();
      //if there is a job in the CPU
      if(!idle){
        if(workingProcess.subTic()){
            //check i f process is fully done
            if(workingProcess.isDone())
              exitProcess(workingProcess, clock);
            else
              blockProcess(workingProcess);
            //choose new process if any ready process, idle otherwise
            if(queueWithReady != 0){
              workingProcess = pickProcess(queueWithReady);
              idle = false;
              workingQuant = 0;
            }
            else
              idle = true;
        }
        //preempt process if necesary and get new process
        else if(workingProcess.getPriority() != 1 && checkPreempted(workingProcess, L2quant, L3quant, workingQuant)){
          queueWithReady = anyProcessReady();
          if(queueWithReady != 0){
            workingProcess = pickProcess(queueWithReady);
            idle = false;
            workingQuant = 0;
          }
          else
            idle = true;
        }
        else
          workingQuant++;
      }
      //get new processes if any ready, idle otherwise
      else if(queueWithReady != 0){
        workingProcess = pickProcess(queueWithReady);
        idle = false;
        workingQuant = 0;
      }
      else
        idle = true;

      ganttChart(idle, workingProcess);
    }

    printer();
  }//main

  /**
   * This method updates the waiting time of each process in ready any ready queue
   */
  public static void advanceTimeReady(){
    for(Process p: HPQueue)
      p.waiting();
    for(Process p: L2Queue)
      p.waiting();
    for(Process p: L3Queue)
      p.waiting();
  }//advanceTimeReady

  /**
   * This process check if there are new process and adds new jobs them to the ready queue
   * @param clock is the current clock time of the system
   */
  public static void NewJob(int clock){
    while(inputList.size() != 0 && inputList.getFirst().getArrivalTime() == clock){
      returnToQueue(inputList.removeFirst());
    }
  }//NewJob
/**
 * This time updates the blocked time of each blocked processing
 */
 public static void advanceTimeBlock(){
   Process p;
   if (BlockedQueue.size() != 0)
      for(int i = 0; i < BlockedQueue.size(); i++){
        p = BlockedQueue.get(i);
        if(p.subTic()){
          BlockedQueue.remove(p);
          returnToQueue(p);
          i--;
        }
      }
  }//advanceTimeBlock

/**
 * This process returns a processes to its queue
 * @param p is the process to be returned to the queue
 */
 public static void returnToQueue(Process p){
   switch (p.getPriority()){
     case 1:
        HPQueue.add(p);
        break;
        case 2:
        L2Queue.add(p);
        break;
        case 3:
        L3Queue.add(p);
        break;
    }
  }//returnToQueue

  /**
   * This process checks if there are things left in the system
   * @param idleStatus true if the system is in idle state, false otherwise
   * @return true if there are things left, false if the system is empty
   */
  public static int thingsLeft(boolean idleStatus){
    if(!idleStatus)
      return 1;
    return anyProcessReady() + inputList.size() + BlockedQueue.size();
  }//thingsLeft

  /**
   * Checks if there any ready processes
   * @return queue numbeer of highest priority with ready job
   */
  public static int anyProcessReady(){
    if (HPQueue.size() != 0)
      return 1;
    else if (L2Queue.size() != 0)
      return 2;
    else if (L3Queue.size() != 0)
      return 3;
    else
      return 0;
  }//anyProcessReady

  /**
   * This process gets a ready job from the ready queues
   * @param ReadyQueue is the queue number with the ready processes
   * @return selected process
   */
  public static Process pickProcess(int ReadyQueue){
    switch (ReadyQueue){
      case 1:
        return HPQueue.removeFirst();
      case 2:
        return L2Queue.removeFirst();
      case 3:
        return L3Queue.removeFirst();
      default:
        throw new IllegalArgumentException("No Ready Process");
    }
  }//pickProcess

  /**
   * Actions taken when a process is done in the system
   * @param p process that is done in the sysstem
   * @param clock current clock time of the system
   */
  public static void exitProcess(Process p, int clock){
    p.setExitTime(clock);
    outList.add(p);
  }//exitProcess

  /**
   * This method updates the list with the Gantt Chart
   * @param idleStatus true if the system is in idle state, false otherwise
   * @param p process on CPU if any
   */
  public static void ganttChart(boolean idleStatus, Process p){
    if(idleStatus)
      ganttOutput.add('*');
    else
      ganttOutput.add(p.getName());
  }//ganttChart

  /**
   * checks if current process needs to be preempted. Lowers priority when needed
   * @param p current process in the CPU
   * @param L2quant max quantum on L2 queue
   * @param L3quant max quantum on L3 queue
   * @param burst current tics p has spent on the CPU
   * @return true if preempted, false otherwise
   */
  public static boolean checkPreempted(Process p, int L2quant, int L3quant, int burst){
    int quant;
    if(p.getPriority() == 2)
      quant = L2quant;
    else
      quant = L3quant;

    if(burst+1 == quant){
      if(p.preempted() == MAXPREEMTED){
        p.lowerPriority();
      }
      returnToQueue(p);
      return true;
    }
    return false;
  }//checkPreempted

  /**
   * This method moves process to block queue and changes priority when needed
   * @param p process to be blocked
   */
  public static void blockProcess(Process p){
    if(p.getPriority() == 3)
      p.increasePriority();
    BlockedQueue.add(p);
  }//blockProcess

  /**
   * Prints final output
   */
  public static void printer(){
    int averageTurnAround = 0;
    double numberOfProcesses = (double)outList.size();
    int averageWaitTime = 0;
    int turnAround;
    double utilization;
    double throughput;
    double idleTimes = 0.0;
    double outputSize = (double) ganttOutput.size()-1.0;

    System.out.print(ganttOutput.get(0));
    for(int i = 1; i < outputSize; i++){
      if(i % 5 == 0)
        System.out.print('|');
      if(i==40)
        System.out.print('\n');
      if(ganttOutput.get(i) == '*')
        idleTimes++;
      System.out.print(ganttOutput.get(i));
    }
    System.out.println("");
    for(Process p: outList){
      turnAround = p.getExitTime() - p.getArrivalTime();
      averageWaitTime += p.getReadyWaiting();
      averageTurnAround += turnAround;
      System.out.println("Process: " + p.getName() + "\tTurnaround: " + turnAround + "\tWaited: " + p.getReadyWaiting());
    }

    averageTurnAround = (int) Math.ceil((double)averageTurnAround/numberOfProcesses);
    averageWaitTime = (int) Math.ceil((double)averageWaitTime/numberOfProcesses);
    utilization = (outputSize - idleTimes)/outputSize;
    throughput = numberOfProcesses/outputSize;

    System.out.println("\nAverage turnaround: " + averageTurnAround);
    System.out.println("Average Time waited: " + averageWaitTime);
    System.out.printf("CPU utilization: %.0f%%", utilization*100);
    System.out.printf("\nThroughput: %.3f \n", throughput);
  }//printer
}//Simulation
