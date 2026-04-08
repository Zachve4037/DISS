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
import java.util.concurrent.ConcurrentLinkedQueue;

public class Airport extends EventSimCore {
    private final int TIME_LIMIT = 86_400; // 24 hodin v sekundach
    private int travellerIdCounter = 0;
    private int crateIdCounter = 0;
    private final Map<Integer, Integer> remainingCratesByTravellerId = new HashMap<>();

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
    private final Queue<Traveller>[] cratesUnloadPeopleQueue = new Queue[]{
            new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>()
    };
    private final Queue<Traveller>[] metalPeopleQueue = new Queue[]{
            new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>()
    };
    private final Queue<Traveller>[] checkedPeopleQueue = new Queue[]{
            new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>()
    };

    //obsluhy
    private boolean[] xrayBusy = new boolean[]{false, false};
    private boolean[] xrayDequeue = new boolean[]{false, false};
    private boolean[] metalDetectorBusy = new boolean[]{false, false};
    private boolean[] securityCheckBusy = new boolean[]{false, false};

    //intervaly
    private final TriangleInterval securityCheckTimeInterval = new TriangleInterval(10, 35, 120);
    private final UniformInterval xrayTimeInterval = new UniformInterval(9, 46);
    private final UniformInterval metalTimeInterval = new UniformInterval(6, 27);

    private final Random routingRng;
    private final Set<Integer> securityClearedTravellerIds = new HashSet<>();
    private final Map<Integer, Integer> readyCratesByTravellerId = new HashMap<>();
    private final Map<Integer, Double> travellerEnterSystemAt = new HashMap<>();


    public Airport(Random seed, int cratesIn, int cratesOut) {
        this.routingRng = new Random(seed.nextLong());
        this.stats = new StatsForAirport();
        this.arrivalTimeRNG = new ContinuousExponentialDistRNG(seed, 1.0 / 15.0); // TODO: lambda
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
        resetReplicationState();
        stats.beforeRep();
        this.scheduleEvent(new ArrivalEvent(this.currentTime, this));
    }

    @Override
    protected void afterReplication() {
        stats.finalizeReplication(currentTime);
        stats.afterRep();
        super.afterReplication();
    }

    public StatsForAirport getStats() {
        return stats;
    }

    public void enqueueCheckedTraveller(Traveller traveller, int workplace) {
        this.checkedPeopleQueue[workplace].add(traveller);
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
        return this.getVisualTimeSeconds();
    }

    public List<Integer> getUnloadQueueTravellerIds() {
        List<Integer> out = new ArrayList<>();
        for (Queue<Traveller> q : cratesUnloadPeopleQueue) {
            for (Traveller t : q) out.add(t.getId());
        }
        return out;
    }

    public List<Integer> getMetalQueueTravellerIds() {
        List<Integer> out = new ArrayList<>();
        for (Queue<Traveller> q : metalPeopleQueue) {
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

    public Traveller pollMetalTraveller(int workplace) {
        Traveller t = metalPeopleQueue[workplace].poll();
        if (t != null) {
            stats.onCustomerLeftMetalQueue(t.getId(), currentTime, metalPeopleQueue[workplace].size());
        }
        return t;
    }

    public Traveller createNewTraveller(double now) {
        int id = travellerIdCounter++;
        int crateCount = sampleNumberOfCrates();
        Traveller t = new Traveller(id, crateCount, crateIdCounter);
        crateIdCounter += crateCount;

        remainingCratesByTravellerId.put(id, crateCount);
        travellerEnterSystemAt.put(id, now);
        readyCratesByTravellerId.put(id, 0);
        stats.onCustomerEnteredSystem(id, now);
        return t;
    }

    public int enqueTravellerForCratesUnload(Traveller traveller, double now) {
        int size0 = cratesUnloadPeopleQueue[0].size();
        int size1 = cratesUnloadPeopleQueue[1].size();
        int workplace;
        if (size0 < size1) workplace = 0;
        else if (size1 < size0) workplace = 1;
        else workplace = routingRng.nextBoolean() ? 0 : 1;

        cratesUnloadPeopleQueue[workplace].add(traveller);
        stats.onCustomerEnteredUnloadQueue(traveller.getId(), now, cratesUnloadPeopleQueue[workplace].size());
        return workplace;
    }

    public boolean hasWaitingUnloadTraveller(int workplace) {
        return !cratesUnloadPeopleQueue[workplace].isEmpty();
    }

    public Traveller peekUnloadTraveller(int workplace) {
        return cratesUnloadPeopleQueue[workplace].peek();
    }

    public Traveller pollUnloadTraveller(int workplace) {
        Traveller t = cratesUnloadPeopleQueue[workplace].poll();
        if (t != null) {
            stats.onCustomerLeftUnloadQueue(t.getId(), currentTime, cratesUnloadPeopleQueue[workplace].size());
        }
        return t;
    }

    public void enqueueCrateForXrayEnqueue(Crate crate, int workplace, double now) {
        if (!xrayCratesQueue[workplace].offer(crate)) {
            throw new IllegalStateException("Xray input belt is full for workplace " + workplace);
        }
        stats.onCrateEnteredXrayQueue(crate.getId(), now, xrayCratesQueue[workplace].size());
    }

    public void enqueueCrateForXrayDequeue(Crate crate, int workplace, double now) {
        if (!xrayDequeueCratesQueue[workplace].offer(crate)) {
            throw new IllegalStateException("Xray output belt is full for workplace " + workplace);
        }
        stats.onCrateEnteredPickupQueue(crate.getId(), now, xrayDequeueCratesQueue[workplace].size());
    }

    public Crate pollCrateForXrayInput(int workplace) {
        Crate c = xrayCratesQueue[workplace].poll();
        if (c != null) {
            stats.onCrateLeftXrayQueue(c.getId(), currentTime, xrayCratesQueue[workplace].size());
        }
        return c;
    }

    public boolean canPutCrateOnXrayInput(int workplace) {
        return xrayCratesQueue[workplace].remainingCapacity() > 0;
    }

    public boolean isXrayOutputFull(int workplace) {
        return xrayDequeueCratesQueue[workplace].remainingCapacity() == 0;
    }

    public void enqueueTravellerForMetal(Traveller traveller, int workplace, double now) {
        this.metalPeopleQueue[workplace].add(traveller);
        stats.onCustomerEnteredMetalQueue(traveller.getId(), now, metalPeopleQueue[workplace].size());
    }

    public boolean hasWaitingMetalTraveller(int workplace) {
        return !this.metalPeopleQueue[workplace].isEmpty();
    }

    public boolean needsInspection() {
        return routingRng.nextDouble() < 0.19;
    }

    public void markCrateReadyForOwner(Crate c) {
        readyCratesByTravellerId.merge(c.gettId(), 1, Integer::sum);
    }

    public void markTravellerAfterSecurity(Traveller t, double now) {
        securityClearedTravellerIds.add(t.getId());
    }

    public boolean isTravellerReadyToTakeCrates(Traveller t, int workplace) {
        if (!securityClearedTravellerIds.contains(t.getId())) return false;

        int remaining = remainingCratesByTravellerId.getOrDefault(t.getId(), 0);
        int ready = readyCratesByTravellerId.getOrDefault(t.getId(), 0);

        return remaining == 0 || ready > 0;
    }

    public boolean isTravellerAlreadyInCheckedQueue(Traveller t, int workplace) {
        return checkedPeopleQueue[workplace].contains(t);
    }

    public void removeTravellerFromCheckedQueue(Traveller t, int workplace) {
        checkedPeopleQueue[workplace].remove(t);
    }

    public void takeAllTravellerCratesFromOutput(Traveller t, int workplace, double now) {
        int removed = 0;
        Iterator<Crate> it = xrayDequeueCratesQueue[workplace].iterator();
        while (it.hasNext()) {
            Crate c = it.next();
            if (c.gettId() == t.getId()) {
                it.remove();
                removed++;
                stats.onCrateLeftPickupQueue(c.getId(), now, xrayDequeueCratesQueue[workplace].size());
            }
        }

        if (removed > 0) {
            readyCratesByTravellerId.merge(t.getId(), -removed, Integer::sum);
            remainingCratesByTravellerId.merge(t.getId(), -removed, Integer::sum);
        }
    }

    public void finishTraveller(Traveller t, double now) {
        securityClearedTravellerIds.remove(t.getId());
        readyCratesByTravellerId.remove(t.getId());

        Double enter = travellerEnterSystemAt.remove(t.getId());
        if (enter != null) {
            customerServed(now - enter);
        }
        stats.onCustomerFinishedSystem(t.getId(), now);
        remainingCratesByTravellerId.remove(t.getId());
    }

    public Traveller peekCheckedTraveller(int workplace) {
        return checkedPeopleQueue[workplace].peek();
    }

    public boolean hasWaitingCheckedTraveller(int workplace) {
        return !checkedPeopleQueue[workplace].isEmpty();
    }

    public boolean hasTravellerCollectedAllCrates(Traveller t) {
        return remainingCratesByTravellerId.getOrDefault(t.getId(), 0) <= 0;
    }

    public List<Integer> getUnloadQueueTravellerIds(int workplace) {
        List<Integer> out = new ArrayList<>();
        for (Traveller t : cratesUnloadPeopleQueue[workplace]) {
            out.add(t.getId());
        }
        return out;
    }

    public List<Integer> getMetalQueueTravellerIds(int workplace) {
        List<Integer> out = new ArrayList<>();
        for (Traveller t : metalPeopleQueue[workplace]) {
            out.add(t.getId());
        }
        return out;
    }

    public List<Integer> getXrayEnqueueCrateIds(int workplace) {
        List<Integer> out = new ArrayList<>();
        for (Crate c : xrayCratesQueue[workplace]) {
            out.add(c.getId());
        }
        return out;
    }

    public List<Integer> getXrayDequeueCrateIds(int workplace) {
        List<Integer> out = new ArrayList<>();
        for (Crate c : xrayDequeueCratesQueue[workplace]) {
            out.add(c.getId());
        }
        return out;
    }

    private void resetReplicationState() {
        travellerIdCounter = 0;
        crateIdCounter = 0;

        cratesUnloadPeopleQueue[0].clear();
        cratesUnloadPeopleQueue[1].clear();
        metalPeopleQueue[0].clear();
        metalPeopleQueue[1].clear();
        checkedPeopleQueue[0].clear();
        checkedPeopleQueue[1].clear();

        xrayCratesQueue[0].clear();
        xrayCratesQueue[1].clear();
        xrayDequeueCratesQueue[0].clear();
        xrayDequeueCratesQueue[1].clear();

        xrayBusy[0] = xrayBusy[1] = false;
        metalDetectorBusy[0] = metalDetectorBusy[1] = false;
        securityCheckBusy[0] = securityCheckBusy[1] = false;

        securityClearedTravellerIds.clear();
        readyCratesByTravellerId.clear();
        travellerEnterSystemAt.clear();
        remainingCratesByTravellerId.clear();
    }
}
