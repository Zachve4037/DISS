package S2.Events.Example;

import S2.Events.Event;
import S2.SimCores.EventSimCore;
import S2.SimCores.Stanok;

public class ExampleStartServiceEvent extends Event {
    public ExampleStartServiceEvent(double timeOfEvent, EventSimCore simCore) {
        super(timeOfEvent, simCore);
    }

    @Override
    public void execute() {
        Stanok sim = (Stanok) getSimCore();
        double now = getTimeOfTheEvent();

        if (sim.isServiceBusy() || !sim.hasWaitingCustomer()) {
            return;
        }

        double arrivalTime = sim.dequeueCustomer();
        double timeInQueue = now - arrivalTime;
        sim.customerLeftQueue(timeInQueue);

        sim.setServiceBusy(true);
        double serviceTime = sim.sampleServiceSeconds();
        sim.scheduleEvent(new ExampleEndServiceEvent(now + serviceTime, sim, serviceTime));
    }
}
