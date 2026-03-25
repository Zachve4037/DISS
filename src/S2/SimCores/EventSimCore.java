package S2.SimCores;

import S2.Events.Event;
import S2.Statistics.ArithmeticalStatistic;
import S2.Statistics.WeightedStatistic;

import java.util.PriorityQueue;

public abstract class EventSimCore extends SimCore {
    private PriorityQueue<Event> eventCalendar = new PriorityQueue<>();
    private double currentTime;
    private volatile boolean paused = false;
    private int servedCustomers = 0;

    //global
    private ArithmeticalStatistic avgTimeInQueueOverReps;
    private ArithmeticalStatistic avgQueueLengthOverReps;
    private ArithmeticalStatistic avgServiceTimeOverReps;

    //replikacie
    private ArithmeticalStatistic repTimeInQueue;
    private ArithmeticalStatistic repServiceTime;
    private WeightedStatistic repQueueLengthTimeWeighted;
    private double lastQueueLenChangeTime;
    private int queueLength;


    private boolean shouldStop() {
        return this.servedCustomers < 1000;
    }

    @Override
    protected void beforeSimulation() {
        this.avgTimeInQueueOverReps = new ArithmeticalStatistic();
        this.avgQueueLengthOverReps = new ArithmeticalStatistic();
        this.avgServiceTimeOverReps = new ArithmeticalStatistic();
    }

    @Override
    protected void beforeReplication() {
        this.currentTime = 0;
        this.eventCalendar.clear();

        repTimeInQueue = new ArithmeticalStatistic();
        repServiceTime = new ArithmeticalStatistic();
        repQueueLengthTimeWeighted = new WeightedStatistic();

        this.lastQueueLenChangeTime = 0;
        this.queueLength = 0;
    }

    @Override
    protected void doReplication() {
        while (!this.eventCalendar.isEmpty() && !shouldStop()) {
            Event event = this.eventCalendar.poll();
            if (event == null) break;
            this.currentTime = event.getTimeOfTheEvent();
            event.execute();
            //todo pridat nove eventy
        }
        double dt = currentTime - lastQueueLenChangeTime;
        if (dt > 0) {
            repQueueLengthTimeWeighted.addValue(queueLength, dt);
        }
    }

    @Override
    protected void afterReplication() {
        this.avgTimeInQueueOverReps.addValue(repTimeInQueue.getValue(), 1);
        this.avgServiceTimeOverReps.addValue(repServiceTime.getValue(), 1);
        this.avgQueueLengthOverReps.addValue(repQueueLengthTimeWeighted.getValue(), 1);
    }

    @Override
    protected void afterSimulation() {
        System.out.println("Simulation finished after " + this.getReplications() + " replications.");
        System.out.println("Average time in queue: " + this.avgTimeInQueueOverReps.getValue());
        System.out.println("Average queue length: " + this.avgQueueLengthOverReps.getValue());
        System.out.println("Average service time: " + this.avgServiceTimeOverReps.getValue());
    }

    protected void scheduleEvent(Event e) {
        this.eventCalendar.add(e);
    }

    protected void queueLengthChange(int newQueueLength) {
        double dt = currentTime - lastQueueLenChangeTime;
        if (dt > 0) {
            repQueueLengthTimeWeighted.addValue(queueLength, dt);
        }
        this.queueLength = newQueueLength;
        this.lastQueueLenChangeTime = currentTime;
    }

    protected void customerLeftQueue(double seconds) {
        this.repTimeInQueue.addValue(seconds, 1.0);
    }

    protected void customerServed(double serviceTime) {
        this.repServiceTime.addValue(serviceTime, 1.0);
        this.servedCustomers++;
    }

    public PriorityQueue<Event> getEventCalendar() {
        return eventCalendar;
    }

}