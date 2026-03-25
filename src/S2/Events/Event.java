package S2.Events;

import S2.SimCores.EventSimCore;

public abstract class Event implements Comparable<Event> {
    private double timeOfTheEvent;
    private final EventSimCore simCore;

    public Event(double timeOfTheEvent, EventSimCore simCore) {
        this.timeOfTheEvent = timeOfTheEvent;
        this.simCore = simCore;
    }

    public abstract void execute();

    public double getTimeOfTheEvent() {
        return timeOfTheEvent;
    }

    public EventSimCore getSimCore() {
        return simCore;
    }

    @Override
    public int compareTo(Event o) {
        return Double.compare(this.timeOfTheEvent, o.timeOfTheEvent);
    }
}
