package S2.Events;

import S2.SimCores.EventSimCore;

public abstract class Event {
    private double timeOfTheEvent;
    private EventSimCore simCore;

    public Event(double timeOfTheEvent, EventSimCore simCore) {
        this.timeOfTheEvent = timeOfTheEvent;
        this.simCore = simCore;
    }

    public void execute() {

    }

    public double getTimeOfTheEvent() {
        return timeOfTheEvent;
    }

    public void setTimeOfTheEvent(double timeOfTheEvent) {
        this.timeOfTheEvent = timeOfTheEvent;
    }

    public EventSimCore getSimCore() {
        return simCore;
    }

    public void setSimCore(EventSimCore simCore) {
        this.simCore = simCore;
    }
}
