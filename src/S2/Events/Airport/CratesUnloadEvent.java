package S2.Events.Airport;

import S2.Events.Event;
import S2.Objects.Traveller;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class CratesUnloadEvent extends Event {
    private final int workplace;
    private final Traveller t;
    public CratesUnloadEvent(double timeOfTheEvent, EventSimCore simCore, int workplace, Traveller t) {
        super(timeOfTheEvent, simCore);
        this.workplace = workplace;
        this.t = t;
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        sim.enqueueTravellerForMetal(t, workplace);

        //TODO vykladanie prepraviek po jednej

        if (!sim.isXrayBusy(workplace)) {
            sim.scheduleEvent(new XrayStartEvent(now, sim, workplace));
        }

        if (!sim.isMetalDetectorBusy(workplace)) {
            sim.scheduleEvent(new MetalStartEvent(now, sim, workplace, t));
        }
    }
}
