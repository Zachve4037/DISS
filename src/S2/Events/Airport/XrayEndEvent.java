package S2.Events.Airport;

import S2.Events.Event;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class XrayEndEvent extends Event {
    private final int workplace;
    public XrayEndEvent(double timeOfTheEvent, EventSimCore simCore, int workplace) {
        super(timeOfTheEvent, simCore);
        this.workplace = workplace;
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        sim.enqueueCrateForXrayDequeue(sim.pollCrateForXray(workplace), workplace);

        if (sim.hasWaitingCratesForXray(workplace)) {
            sim.scheduleEvent(new XrayStartEvent(now, sim, workplace));
        }
    }
}
