package S2.Events.Airport;

import S2.Events.Event;
import S2.Objects.Traveller;
import S2.SimCores.Airport;
import S2.SimCores.EventSimCore;

public class CratesTakeEvent extends Event {
    private final int workplace;
    private final Traveller t;

    public CratesTakeEvent(double timeOfTheEvent, EventSimCore simCore, int workplace, Traveller t) {
        super(timeOfTheEvent, simCore);
        this.workplace = workplace;
        this.t = t;
    }

    @Override
    public void execute() {
        Airport sim = (Airport) getSimCore();
        double now = getTimeOfTheEvent();

        if (!sim.isTravellerReadyToTakeCrates(t, workplace)) {
            if (!sim.isTravellerAlreadyInCheckedQueue(t, workplace)) {
                sim.enqueueCheckedTraveller(t, workplace);
            }
            return;
        }

        sim.removeTravellerFromCheckedQueue(t, workplace);
        sim.takeAllTravellerCratesFromOutput(t, workplace, now);
        sim.finishTraveller(t, now);

        // Uvolnenie vystupu moze odblokovat RTG.
        if (!sim.isXrayBusy(workplace) && sim.hasWaitingCratesForXray(workplace) && !sim.isXrayOutputFull(workplace)) {
            sim.scheduleEvent(new XrayStartEvent(now, sim, workplace));
        }

        // Mozno moze pokracovat unload.
        if (sim.hasWaitingUnloadTraveller(workplace) && sim.canPutCrateOnXrayInput(workplace)) {
            sim.scheduleEvent(new CratesUnloadEvent(now, sim, workplace));
        }

        // Skus dalsieho cakajuceho na odber.
        Traveller next = sim.peekCheckedTraveller(workplace);
        if (next != null) {
            sim.scheduleEvent(new CratesTakeEvent(now, sim, workplace, next));
        }

    }
}
