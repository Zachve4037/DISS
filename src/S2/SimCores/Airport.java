package S2.SimCores;

import S2.Events.Airport.ArrivalEvent;
import S2.Objects.Crate;
import S2.Objects.Traveller;
import S2.RNGs.empirical.DiscreteEmpiricalDistRNG;
import S2.RNGs.exponential.ContinuousExponentialDistRNG;
import S2.RNGs.intervals.EmpiricInterval;
import S2.RNGs.intervals.TriangleInterval;
import S2.RNGs.intervals.UniformInterval;
import S2.RNGs.triangle.ContinuousTriangularDistRNG;
import S2.RNGs.uniform.ContinuousUniformDistRNG;
import S2.Statistics.StatsForAirport;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class Airport extends EventSimCore {
    private final int TIME_LIMIT = 86_400; // 24 hodin v sekundach
    private int travellerIdCounter = 0;
    private int crateIdCounter = 0;
    //generatory
    private final ContinuousExponentialDistRNG arrivalTimeRNG;
    private final ContinuousUniformDistRNG[] metalDetectorTimeRNGs;
    private final ContinuousUniformDistRNG[] xrayTimeRNGs;
    private final ContinuousTriangularDistRNG[] securityCheckTimeRNG;
    private final DiscreteEmpiricalDistRNG numberOfCratesRNG;

    //statistiky
    private StatsForAirport stats;

    //rady
    private final ArrayBlockingQueue<Crate>[] xrayCratesQueue;
    private final ArrayBlockingQueue<Crate>[] xrayDequeueCratesQueue;
    private final LinkedList<Traveller>[] cratesUnloadPeopleQueue = new LinkedList[]{new LinkedList<Traveller>(), new LinkedList<Traveller>()};
    private final LinkedList<Traveller>[] metalPeopleQueue = new LinkedList[]{new LinkedList<Traveller>(), new LinkedList<Traveller>()};
    private final LinkedList<Traveller>[] checkedPeopleQueue = new LinkedList[]{new LinkedList<Traveller>(), new LinkedList<Traveller>()};

    //obsluhy
    private boolean[] xrayBusy = new boolean[]{false, false};
    private boolean[] xrayDequeue = new boolean[]{false, false};
    private boolean[] metalDetectorBusy = new boolean[]{false, false};
    private boolean[] securityCheckBusy = new boolean[]{false, false};

    //intervaly
    private final TriangleInterval securityCheckTimeInterval = new TriangleInterval(10, 35, 120);
    private final UniformInterval xrayTimeInterval = new UniformInterval(9, 46);
    private final UniformInterval metalTimeInterval = new UniformInterval(6, 27);

    public Airport(Random seed, int cratesIn, int cratesOut) {
        this.stats = new StatsForAirport();
        this.arrivalTimeRNG = new ContinuousExponentialDistRNG(seed, 1.0 / 60.0); // TODO: lambda
        this.metalDetectorTimeRNGs = new ContinuousUniformDistRNG[]{
                new ContinuousUniformDistRNG(seed, metalTimeInterval),
                new ContinuousUniformDistRNG(seed, metalTimeInterval)
        };
        this.xrayTimeRNGs = new ContinuousUniformDistRNG[]{
                new ContinuousUniformDistRNG(seed, xrayTimeInterval),
                new ContinuousUniformDistRNG(seed, xrayTimeInterval)
        };
        this.securityCheckTimeRNG = new ContinuousTriangularDistRNG[] {
                new ContinuousTriangularDistRNG(seed, securityCheckTimeInterval),
                new ContinuousTriangularDistRNG(seed, securityCheckTimeInterval)
        };
        this.numberOfCratesRNG = new DiscreteEmpiricalDistRNG(seed,
                List.of(
                        new EmpiricInterval(0, 1, 0.15),
                        new EmpiricInterval(1, 2, 0.68),
                        new EmpiricInterval(2, 3, 0.17)
                )
        );

        this.xrayCratesQueue = new ArrayBlockingQueue[]{new ArrayBlockingQueue<Crate>(cratesIn), new ArrayBlockingQueue<Crate>(cratesIn)}; //TODO kapacita radov
        this.xrayDequeueCratesQueue = new ArrayBlockingQueue[]{new ArrayBlockingQueue<Crate>(cratesOut), new ArrayBlockingQueue<Crate>(cratesOut)}; //TODO kapacita radov
    }

    @Override
    public boolean shouldStop() {
        return this.currentTime >= TIME_LIMIT;
    }

    @Override
    protected void initialEvents() {
        this.scheduleEvent(new ArrivalEvent(this.currentTime, this));
    }

    public StatsForAirport getStats() {
        return stats;
    }

    public int enqueTravellerForCratesUnload(Traveller traveller) {
        int workplace;
        if (this.cratesUnloadPeopleQueue[0].size() == this.cratesUnloadPeopleQueue[1].size()) {
            workplace = new Random().nextInt(2);
        } else {
            workplace = this.cratesUnloadPeopleQueue[0].size() < this.cratesUnloadPeopleQueue[1].size() ? 0 : 1;
        }
        this.cratesUnloadPeopleQueue[workplace].add(traveller);
        return workplace;
    }

    public void enqueueCheckedTraveller(Traveller traveller, int workplace) {
        this.checkedPeopleQueue[workplace].add(traveller);
    }

    public void enqueueCrateForXrayEnqueue(Crate crate, int workplace) {
        this.xrayCratesQueue[workplace].add(crate);
    }

    public void enqueueCrateForXrayDequeue(Crate crate, int workplace) {
        this.xrayDequeueCratesQueue[workplace].add(crate);
    }

    public void enqueueTravellerForMetal(Traveller traveller, int workplace) {
        this.metalPeopleQueue[workplace].add(traveller);
    }

    public Traveller createNewTraveller() {
        this.crateIdCounter += 3;
        return new Traveller(this.travellerIdCounter++, sampleNumberOfCrates(), this.crateIdCounter);
    }

    public boolean isXrayBusy(int workplace) {
        return this.xrayBusy[workplace];
    }

    public void setXrayBusy(int workplace, boolean busy) {
        this.xrayBusy[workplace] = busy;
    }

    public boolean isXrayDequeueBusy(int workplace) {
        return xrayDequeue[workplace];
    }

    public void setXrayDequeue(int workplace, boolean busy) {
        this.xrayDequeue[workplace] = busy;
    }

    public boolean isMetalDetectorBusy(int workplace) {
        return this.metalDetectorBusy[workplace];
    }

    public void setMetalDetectorBusy(int workplace, boolean busy) {
        this.metalDetectorBusy[workplace] = busy;
    }

    public boolean isSecurityCheckBusy(int workplace) {
        return this.securityCheckBusy[workplace];
    }

    public void setSecurityCheckBusy(int workplace, boolean busy) {
        this.securityCheckBusy[workplace] = busy;
    }

    public double sampleArrivalTime() {
        return this.arrivalTimeRNG.generate();
    }

    public double sampleMetalDetectorTime(int workplace) {
        return this.metalDetectorTimeRNGs[workplace].generate();
    }

    public double sampleXrayTime(int workplace) {
        return this.xrayTimeRNGs[workplace].generate();
    }

    public double sampleSecurityCheckTime(int workplace) {
        return this.securityCheckTimeRNG[workplace].generate();
    }

    public int sampleNumberOfCrates() {
        return this.numberOfCratesRNG.generate();
    }

    public double getCurrentTimeSeconds() {
        return this.currentTime;
    }

    public List<Integer> getUnloadQueueTravellerIds() {
        List<Integer> out = new ArrayList<>();
        for (LinkedList<Traveller> q : cratesUnloadPeopleQueue) {
            for (Traveller t : q) out.add(t.getId());
        }
        return out;
    }

    public List<Integer> getMetalQueueTravellerIds() {
        List<Integer> out = new ArrayList<>();
        for (LinkedList<Traveller> q : metalPeopleQueue) {
            for (Traveller t : q) out.add(t.getId());
        }
        return out;
    }

    public List<Integer> getXrayEnqueueCrateIds() {
        List<Integer> out = new ArrayList<>();
        for (ArrayBlockingQueue<Crate> q : xrayCratesQueue) {
            for (Crate c : q) out.add(c.getId());
        }
        return out;
    }

    public List<Integer> getXrayDequeueCrateIds() {
        List<Integer> out = new ArrayList<>();
        for (ArrayBlockingQueue<Crate> q : xrayDequeueCratesQueue) {
            for (Crate c : q) out.add(c.getId());
        }
        return out;
    }

    public boolean hasWaitingCratesForXray(int workplace) {
        return !this.xrayCratesQueue[workplace].isEmpty();
    }

    public boolean hasWaitingCratesForXrayDequeue(int workplace) {
        return !this.xrayDequeueCratesQueue[workplace].isEmpty();
    }

    public boolean hasWaitingMetalTraveller(int workplace) {
        return !this.metalPeopleQueue[workplace].isEmpty();
    }

    public Traveller pollMetalTraveller(int workplace) {
        return this.metalPeopleQueue[workplace].poll();
    }

    public Crate pollCrateForXray(int workplace) {
        if (!this.xrayCratesQueue[workplace].isEmpty()) {
            return this.xrayCratesQueue[workplace].poll();
        } else if (!this.xrayDequeueCratesQueue[workplace].isEmpty()) {
            return this.xrayDequeueCratesQueue[workplace].poll();
        }
        return null;
    }
}
