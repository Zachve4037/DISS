package S2.Events.Airport;

import S2.Events.Event;
import S2.SimCores.EventSimCore;

public class InspectionStartEvent extends Event {
    public InspectionStartEvent(double timeOfTheEvent, EventSimCore simCore) {
        super(timeOfTheEvent, simCore);
    }

    @Override
    public void execute() {

    }
}
