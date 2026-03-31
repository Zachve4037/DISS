package S2.Events.Airport;

import S2.Events.Event;
import S2.SimCores.EventSimCore;

public class CratesTakeEvent extends Event {
    public CratesTakeEvent(double timeOfTheEvent, EventSimCore simCore) {
        super(timeOfTheEvent, simCore);
    }

    @Override
    public void execute() {

    }
}
