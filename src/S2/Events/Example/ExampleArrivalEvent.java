package S2.Events.Example;

import S2.Events.Event;
import S2.SimCores.EventSimCore;
import S2.SimCores.Stanok;

public class ExampleArrivalEvent extends Event {
    public ExampleArrivalEvent(double timeOfEvent, EventSimCore simCore) {
        super(timeOfEvent, simCore);
    }

    @Override
    public void execute() {
        Stanok simCore = (Stanok) getSimCore();
        double now = getTimeOfTheEvent();

        simCore.enqueueCustomer(now);

        if (simCore.shouldStop()) {
            double nextArrivalTime = now + simCore.sampleInterArrivalSeconds();
            simCore.scheduleEvent(new ExampleArrivalEvent(nextArrivalTime, simCore));
        }

        if (!simCore.isServiceBusy()) {
            simCore.scheduleEvent(new ExampleStartServiceEvent(now, simCore));
        }
    }
}
