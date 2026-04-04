package S2.Events.Airport;

import S2.Events.Event;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class XrayStartEvent extends Event {
    private final int workplace;
    public XrayStartEvent(double timeOfTheEvent, EventSimCore simCore, int workplace) {
        super(timeOfTheEvent, simCore);
        this.workplace = workplace;
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        if (!sim.hasWaitingCratesForXray(workplace) || sim.isXrayBusy(workplace)) {
            return;
        }

        sim.setXrayBusy(workplace, true);
        sim.scheduleEvent(new XrayEndEvent(now + sim.sampleXrayTime(workplace), sim, workplace));
    }
}
