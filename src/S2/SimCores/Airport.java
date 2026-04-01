package S2.SimCores;

import S2.Events.Airport.ArrivalEvent;
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
    //generatory
    private final ContinuousExponentialDistRNG arrivalTimeRNG;
    private final ContinuousUniformDistRNG[] metalDetectorTimeRNGs;
    private final ContinuousUniformDistRNG[] xrayTimeRNGs;
    private final ContinuousTriangularDistRNG securityCheckTimeRNG;
    private final DiscreteEmpiricalDistRNG numberOfCratesRNG;

    //statistiky
    private StatsForAirport stats = new StatsForAirport();

    //rady
    private final ArrayBlockingQueue[] xrayEnqueueCrates = new ArrayBlockingQueue[]{new ArrayBlockingQueue<Traveller>(), new ArrayBlockingQueue()}; //TODO kapacita radov
    private final ArrayBlockingQueue[] xrayDequeueCrates = new ArrayBlockingQueue[]{new ArrayBlockingQueue<Traveller>(), new ArrayBlockingQueue()}; //TODO kapacita radov
    private final LinkedList[] cratesUnloadPeople = new LinkedList[]{new LinkedList(), new LinkedList()};
    private final LinkedList[] metalPeople = new LinkedList[]{new LinkedList(), new LinkedList()};
    private final LinkedList[] checkedPeople = new LinkedList[]{new LinkedList(), new LinkedList()};

    //obsluhy
    private boolean[] xrayBusy = new boolean[]{false, false};
    private boolean[] metalDetectorBusy = new boolean[]{false, false};
    private boolean securityCheckBusy = false;

    //intervaly
    private final TriangleInterval securityCheckTimeInterval = new TriangleInterval(10, 35, 120);
    private final UniformInterval xrayTimeInterval = new UniformInterval(9, 46);
    private final UniformInterval metalTimeInterval = new UniformInterval(6, 27);

    public Airport(Random seed) {
        this.arrivalTimeRNG = new ContinuousExponentialDistRNG(seed, 1.0 / 60.0); // TODO: lambda
        this.metalDetectorTimeRNGs = new ContinuousUniformDistRNG[]{
                new ContinuousUniformDistRNG(seed, metalTimeInterval),
                new ContinuousUniformDistRNG(seed, metalTimeInterval)
        };
        this.xrayTimeRNGs = new ContinuousUniformDistRNG[]{
                new ContinuousUniformDistRNG(seed, xrayTimeInterval),
                new ContinuousUniformDistRNG(seed, xrayTimeInterval)
        };
        this.securityCheckTimeRNG = new ContinuousTriangularDistRNG(seed, securityCheckTimeInterval);
        this.numberOfCratesRNG = new DiscreteEmpiricalDistRNG(seed,
                List.of(
                        new EmpiricInterval(0, 1, 0.15),
                        new EmpiricInterval(1, 2, 0.68),
                        new EmpiricInterval(2, 3, 0.17)
                )
        );
    }

    @Override
    protected boolean shouldStop() {
        return this.currentTime >= TIME_LIMIT;
    }

    @Override
    protected void initialEvents() {
        this.scheduleEvent(new ArrivalEvent(this.currentTime, this));
    }

    public StatsForAirport getStats() {
        return stats;
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

    public boolean isSecurityCheckBusy() {
        return this.securityCheckBusy;
    }

    public void setSecurityCheckBusy(boolean busy) {
        this.securityCheckBusy = busy;
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

    public double sampleSecurityCheckTime() {
        return this.securityCheckTimeRNG.generate();
    }

    public int sampleNumberOfCrates() {
        return this.numberOfCratesRNG.generate();
    }
}
