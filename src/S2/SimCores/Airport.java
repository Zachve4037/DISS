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
    private static final double DAY_SECONDS = 86_400.0;
    private static final double WARMUP_SECONDS = 7 * DAY_SECONDS; // 7 dni na zahriatie
    private static final double MEASURE_SECONDS = DAY_SECONDS;
    private static final double DEFAULT_ARRIVAL_LAMBDA = 1.0 / 15.0;
    private boolean measurementStarted = false;

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
    private final StatsForAirport stats;
    private int totalUnloadQueueLen() { return cratesUnloadPeopleQueue[0].size() + cratesUnloadPeopleQueue[1].size(); }
    private int totalMetalQueueLen()  { return metalPeopleQueue[0].size() + metalPeopleQueue[1].size(); }
    private int totalXrayQueueLen()   { return xrayCratesQueue[0].size() + xrayCratesQueue[1].size(); }
    private int totalPickupQueueLen() { return xrayDequeueCratesQueue[0].size() + xrayDequeueCratesQueue[1].size(); }

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
    private final boolean[] xrayBusy = new boolean[]{false, false};
    private final boolean[] metalDetectorBusy = new boolean[]{false, false};
    private final boolean[] securityCheckBusy = new boolean[]{false, false};

    //intervaly
    private final TriangleInterval securityCheckTimeInterval = new TriangleInterval(10, 35, 120);
    private final UniformInterval xrayTimeInterval = new UniformInterval(9, 46);
    private final UniformInterval metalTimeInterval = new UniformInterval(6, 27);

    private final Random routingRng;
    private final Set<Integer> securityClearedTravellerIds = new HashSet<>();
    private final Map<Integer, Integer> readyCratesByTravellerId = new HashMap<>();
    private final Map<Integer, Double> travellerEnterSystemAt = new HashMap<>();

    public Airport(Random seed, int cratesIn, int cratesOut) {
        this(seed, cratesIn, cratesOut, DEFAULT_ARRIVAL_LAMBDA);
    }

    public Airport(Random seed, int cratesIn, int cratesOut, double arrivalLambda) {
        this.routingRng = new Random(seed.nextLong());
        this.stats = new StatsForAirport();
        this.arrivalTimeRNG = new ContinuousExponentialDistRNG(seed, arrivalLambda);
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

        this.xrayCratesQueue = new ArrayBlockingQueue[]{new ArrayBlockingQueue<Crate>(cratesIn), new ArrayBlockingQueue<Crate>(cratesIn)};
        this.xrayDequeueCratesQueue = new ArrayBlockingQueue[]{new ArrayBlockingQueue<Crate>(cratesOut), new ArrayBlockingQueue<Crate>(cratesOut)};
    }

    public static double capacityPerDayToLambda(int capacityPerDay) {
        if (capacityPerDay <= 0) {
            throw new IllegalArgumentException("capacityPerDay must be > 0");
        }
        return capacityPerDay / DAY_SECONDS;
    }

    @Override
    public boolean shouldStop() {
        return this.currentTime >= (WARMUP_SECONDS + MEASURE_SECONDS);
    }

    @Override
    protected void initialEvents() {
        resetReplicationState();
        measurementStarted = false;
        stats.beforeRep();
        this.scheduleEvent(new ArrivalEvent(this.currentTime, this));
    }

    @Override
    protected void afterReplication() {
        if (measurementStarted) {
            stats.finalizeReplication(currentTime);
            stats.afterRep();
        }
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

    private boolean isInMeasurementWindow(double now) {
        return now >= WARMUP_SECONDS;
    }

    private void ensureMeasurementStarted(double now) {
        if (!measurementStarted && isInMeasurementWindow(now)) {
            stats.beginMeasuredWindow(
                    now,
                    totalUnloadQueueLen(),
                    totalMetalQueueLen(),
                    totalXrayQueueLen(),
                    totalPickupQueueLen()
            );
            measurementStarted = true;
        }
    }

    public boolean hasWaitingCratesForXray(int workplace) {
        return !this.xrayCratesQueue[workplace].isEmpty();
    }

    public Traveller pollMetalTraveller(int workplace) {
        Traveller t = metalPeopleQueue[workplace].poll();
        if (t != null) {
            ensureMeasurementStarted(currentTime);
            if (measurementStarted) {
                stats.onCustomerLeftMetalQueue(t.getId(), currentTime, totalMetalQueueLen());
            }
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
        ensureMeasurementStarted(now);
        if (measurementStarted) {
            stats.onCustomerEnteredSystem(id, now);
        }
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
        ensureMeasurementStarted(now);
        if (measurementStarted) {
            stats.onCustomerEnteredUnloadQueue(traveller.getId(), now, totalUnloadQueueLen());
        }
        return workplace;
    }

    public boolean hasWaitingUnloadTraveller(int workplace) {
        return !cratesUnloadPeopleQueue[workplace].isEmpty();
    }

    public Traveller peekUnloadTraveller(int workplace) {
        return cratesUnloadPeopleQueue[workplace].peek();
    }

    public void pollUnloadTraveller(int workplace) {
        Traveller t = cratesUnloadPeopleQueue[workplace].poll();
        if (t != null) {
            ensureMeasurementStarted(currentTime);
            if (measurementStarted) {
                stats.onCustomerLeftUnloadQueue(t.getId(), currentTime, totalUnloadQueueLen());
            }
        }
    }

    public void enqueueCrateForXrayEnqueue(Crate crate, int workplace, double now) {
        if (!xrayCratesQueue[workplace].offer(crate)) {
            throw new IllegalStateException("Xray input belt is full for workplace " + workplace);
        }
        ensureMeasurementStarted(now);
        if (measurementStarted) {
            stats.onCrateEnteredXrayQueue(crate.id(), now, totalXrayQueueLen());
        }
    }

    public void enqueueCrateForXrayDequeue(Crate crate, int workplace, double now) {
        if (!xrayDequeueCratesQueue[workplace].offer(crate)) {
            throw new IllegalStateException("Xray output belt is full for workplace " + workplace);
        }
        ensureMeasurementStarted(now);
        if (measurementStarted) {
            stats.onCrateEnteredPickupQueue(crate.id(), now, totalPickupQueueLen());
        }
    }

    public Crate pollCrateForXrayInput(int workplace) {
        Crate c = xrayCratesQueue[workplace].poll();
        if (c != null) {
            ensureMeasurementStarted(currentTime);
            if (measurementStarted) {
                stats.onCrateLeftXrayQueue(c.id(), currentTime, totalXrayQueueLen());
            }
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
        ensureMeasurementStarted(now);
        if (measurementStarted) {
            stats.onCustomerEnteredMetalQueue(traveller.getId(), now, totalMetalQueueLen());
        }
    }

    public boolean hasWaitingMetalTraveller(int workplace) {
        return !this.metalPeopleQueue[workplace].isEmpty();
    }

    public boolean needsInspection() {
        return routingRng.nextDouble() < 0.19;
    }

    public void markCrateReadyForOwner(Crate c) {
        readyCratesByTravellerId.merge(c.tId(), 1, Integer::sum);
    }

    public void markTravellerAfterSecurity(Traveller t) {
        securityClearedTravellerIds.add(t.getId());
    }

    public boolean isTravellerReadyToTakeCrates(Traveller t) {
        if (!securityClearedTravellerIds.contains(t.getId())) return false;

        int remaining = remainingCratesByTravellerId.getOrDefault(t.getId(), 0);
        int ready = readyCratesByTravellerId.getOrDefault(t.getId(), 0);

        return remaining == 0 || ready > 0;
    }

    public boolean isTravellerAlreadyInCheckedQueue(Traveller t, int workplace) {
        return !checkedPeopleQueue[workplace].contains(t);
    }

    public void removeTravellerFromCheckedQueue(Traveller t, int workplace) {
        checkedPeopleQueue[workplace].remove(t);
    }

    public void takeAllTravellerCratesFromOutput(Traveller t, int workplace, double now) {
        int removed = 0;
        Iterator<Crate> it = xrayDequeueCratesQueue[workplace].iterator();
        while (it.hasNext()) {
            Crate c = it.next();
            if (c.tId() == t.getId()) {
                it.remove();
                removed++;
                ensureMeasurementStarted(now);
                if (measurementStarted) {
                    stats.onCrateLeftPickupQueue(c.id(), now, totalPickupQueueLen());
                }
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

        ensureMeasurementStarted(now);

        Double enter = travellerEnterSystemAt.remove(t.getId());
        if (enter != null && measurementStarted) {
            customerServed(now - enter);
            stats.onCustomerFinishedSystem(t.getId(), now);
        }

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
            out.add(c.id());
        }
        return out;
    }

    public List<Integer> getXrayDequeueCrateIds(int workplace) {
        List<Integer> out = new ArrayList<>();
        for (Crate c : xrayDequeueCratesQueue[workplace]) {
            out.add(c.id());
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
