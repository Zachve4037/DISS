package S2.SimCores;

import S2.Events.Event;

import java.util.PriorityQueue;

public class EventSimCore extends SimCore {
    private PriorityQueue<Event> eventsCalendar = new PriorityQueue<>();
    private long counter;
    private double currentTime;
    private volatile boolean paused = false;

    @Override
    protected void beforeSimulation() {
        this.counter = 0;
        this.currentTime = 0;
    }

    @Override
    protected void beforeReplication() {

    }

    @Override
    protected void doReplication() {

    }

    @Override
    protected void afterReplication() {

    }

    @Override
    protected void afterSimulation() {

    }

    public PriorityQueue<Event> getEventsCalendar() {
        return eventsCalendar;
    }

    public void setEventsCalendar(PriorityQueue<Event> eventsCalendar) {
        this.eventsCalendar = eventsCalendar;
    }
}
