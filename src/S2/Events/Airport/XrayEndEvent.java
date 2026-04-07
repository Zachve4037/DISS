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

        Crate processed = sim.pollCrateForXrayInput(workplace);
        if (processed != null) {
            sim.enqueueCrateForXrayDequeue(processed, workplace, now);
            sim.markCrateReadyForOwner(processed);
        }

        sim.setXrayBusy(workplace, false);

        if (sim.hasWaitingCratesForXray(workplace) && !sim.isXrayOutputFull(workplace)) {
            sim.scheduleEvent(new XrayStartEvent(now, sim, workplace));
        }

        if (sim.hasWaitingCheckedTraveller(workplace)) {
            sim.scheduleEvent(new CratesTakeEvent(now, sim, workplace, sim.peekCheckedTraveller(workplace)));
        }

        if (sim.hasWaitingUnloadTraveller(workplace) && sim.canPutCrateOnXrayInput(workplace)) {
            sim.scheduleEvent(new CratesUnloadEvent(now, sim, workplace));
        }
    }
}
