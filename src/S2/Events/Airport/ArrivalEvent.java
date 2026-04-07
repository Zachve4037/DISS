package S2.Events.Airport;

import S2.Events.Event;
import S2.Objects.Traveller;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class ArrivalEvent extends Event {
    public ArrivalEvent(double currentTime, EventSimCore airport) {
        super(currentTime, airport);
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        Traveller t = sim.createNewTraveller(now);
        int workplace = sim.enqueTravellerForCratesUnload(t, now);

        if (!sim.shouldStop()) {
            sim.scheduleEvent(new ArrivalEvent(now + sim.sampleArrivalTime(), sim));
        }

        sim.scheduleEvent(new CratesUnloadEvent(now, sim, workplace));
    }
}
