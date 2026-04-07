package S2.Events.Airport;

import S2.Events.Event;
import S2.Objects.Traveller;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class MetalStartEvent extends Event {
    private final int workplace;
    private final Traveller t;
    public MetalStartEvent(double timeOfTheEvent, EventSimCore simCore, int workplace, Traveller t) {
        super(timeOfTheEvent, simCore);
        this.workplace = workplace;
        this.t = t;
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        if (sim.isMetalDetectorBusy(workplace) || !sim.hasWaitingMetalTraveller(workplace) || sim.isSecurityCheckBusy(workplace)) {
            return;
        }

        Traveller t = sim.pollMetalTraveller(workplace);
        sim.setMetalDetectorBusy(workplace, true);
        sim.scheduleEvent(new MetalEndEvent(now + sim.sampleMetalDetectorTime(workplace), sim, workplace, t));
    }
}