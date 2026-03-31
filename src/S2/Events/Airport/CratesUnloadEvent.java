package S2.Events.Airport;

import S2.Events.Event;
import S2.SimCores.EventSimCore;

public class CratesUnloadEvent extends Event {
    public CratesUnloadEvent(double timeOfTheEvent, EventSimCore simCore) {
        super(timeOfTheEvent, simCore);
    }

    @Override
    public void execute() {

    }
}
