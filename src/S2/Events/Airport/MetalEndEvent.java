package S2.Events.Airport;

import S2.Events.Event;
import S2.Objects.Traveller;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class MetalEndEvent extends Event {
    private final int workplace;
    private final Traveller t;
    public MetalEndEvent(double timeOfTheEvent, EventSimCore simCore, int workplace, Traveller t) {
        super(timeOfTheEvent, simCore);
        this.workplace = workplace;
        this.t = t;
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        if (sim.needsInspection()) {
            sim.scheduleEvent(new InspectionStartEvent(now, sim, workplace, t, sim.sampleSecurityCheckTime(workplace)));
            return;
        }

        sim.setMetalDetectorBusy(workplace, false);

        sim.markTravellerAfterSecurity(t);
        sim.enqueueCheckedTraveller(t, workplace);

        if (sim.hasWaitingMetalTraveller(workplace)) {
            sim.scheduleEvent(new MetalStartEvent(now, sim, workplace));
        }

        sim.scheduleEvent(new CratesTakeEvent(now, sim, workplace, t));
    }
}
