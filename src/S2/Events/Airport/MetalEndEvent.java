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

        sim.setMetalDetectorBusy(workplace, false);

        double inspectionTime = 0;
        if (Math.random() <= 0.19) {
            inspectionTime = sim.sampleSecurityCheckTime(workplace);
            sim.scheduleEvent(new InspectionStartEvent(now, sim, workplace, t, inspectionTime));
            return;
        }

        if (sim.hasWaitingMetalTraveller(workplace)) {
            sim.scheduleEvent(new MetalStartEvent(now + inspectionTime, sim, workplace, sim.pollMetalTraveller(workplace)));
        }

        sim.enqueueCheckedTraveller(this.t, workplace);

        if (!sim.isXrayDequeueBusy(workplace)) {
            sim.scheduleEvent(new CratesTakeEvent(now, sim, workplace, t));
        }
    }
}
