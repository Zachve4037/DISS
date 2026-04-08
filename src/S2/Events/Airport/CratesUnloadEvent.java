package S2.Events.Airport;

import S2.Events.Event;
import S2.Objects.Crate;
import S2.Objects.Traveller;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class CratesUnloadEvent extends Event {
    private final int workplace;

    public CratesUnloadEvent(double timeOfTheEvent, EventSimCore simCore, int workplace) {
        super(timeOfTheEvent, simCore);
        this.workplace = workplace;
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        if (!sim.hasWaitingUnloadTraveller(workplace)) {
            return;
        }

        Traveller head = sim.peekUnloadTraveller(workplace);
        if (head == null) {
            return;
        }

        while (head.hasRemainingCratesToUnload() && sim.canPutCrateOnXrayInput(workplace)) {
            Crate c = head.unloadNextCrate();
            sim.enqueueCrateForXrayEnqueue(c, workplace, now);
        }

        if (!head.hasRemainingCratesToUnload()) {
            sim.pollUnloadTraveller(workplace);
            sim.enqueueTravellerForMetal(head, workplace, now);
        }

        if (!sim.isXrayBusy(workplace)) {
            sim.scheduleEvent(new XrayStartEvent(now, sim, workplace));
        }
        if (!sim.isMetalDetectorBusy(workplace) && !sim.isSecurityCheckBusy(workplace)) {
            sim.scheduleEvent(new MetalStartEvent(now, sim, workplace));
        }

        if (sim.hasWaitingUnloadTraveller(workplace) && sim.canPutCrateOnXrayInput(workplace)) {
            sim.scheduleEvent(new CratesUnloadEvent(now, sim, workplace));
        }
    }
}