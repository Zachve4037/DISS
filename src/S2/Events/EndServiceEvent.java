package S2.Events;

import S2.SimCores.Stanok;

public class EndServiceEvent extends Event {
    private final double serviceTime;
    public EndServiceEvent(double timeofEvent, Stanok simCore, double serviceTime) {
        super(timeofEvent, simCore);
        this.serviceTime = serviceTime;
    }

    @Override
    public void execute() {
        Stanok sim = (Stanok) getSimCore();
        double now = getTimeOfTheEvent();

        sim.customerServed(this.serviceTime);
        sim.setServiceBusy(false);

        if (sim.hasWaitingCustomer()) {
            sim.scheduleEvent(new StartServiceEvent(now, sim));
        }
    }
}
