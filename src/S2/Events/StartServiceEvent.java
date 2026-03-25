package S2.Events;

import S2.SimCores.Stanok;

public class StartServiceEvent extends Event {
    public StartServiceEvent(double timeOfEvent, Stanok simCore) {
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
        sim.scheduleEvent(new EndServiceEvent(now + serviceTime, sim, serviceTime));
    }
}
