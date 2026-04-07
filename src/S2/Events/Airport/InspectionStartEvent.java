package S2.Events.Airport;

import S2.Events.Event;
import S2.Objects.Traveller;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class InspectionStartEvent extends Event {
    private final int workplace;
    private final Traveller t;
    private final double inspectionTime;
    public InspectionStartEvent(double timeOfTheEvent, EventSimCore simCore, int workplace, Traveller t, double inspectionTime) {
        super(timeOfTheEvent, simCore);
        this.workplace = workplace;
        this.t = t;
        this.inspectionTime = inspectionTime;
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        sim.setSecurityCheckBusy(workplace, true);
        sim.setMetalDetectorBusy(workplace, true);
        sim.scheduleEvent(new InspectionEndEvent(now + inspectionTime, sim, workplace, t));
    }
}
