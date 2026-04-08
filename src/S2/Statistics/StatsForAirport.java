package S2.Statistics;

import java.util.*;

public class StatsForAirport {
    private long repCustomersEnteredUnloadQueue;
    private long repCustomersEnteredMetalQueue;
    private long repCratesEnteredXrayQueue;
    private long repCratesEnteredPickupQueue;

    //priemerna dlzka radu
    private WeightedStatistic repUnloadQueueLen;
    private WeightedStatistic repMetalQueueLen;
    private WeightedStatistic repXrayQueueLen;
    private WeightedStatistic repPickupQueueLen;

    // priemerna doba v rade
    private ArithmeticalStatistic repWaitUnload;
    private ArithmeticalStatistic repWaitMetal;
    private ArithmeticalStatistic repWaitXray;
    private ArithmeticalStatistic repWaitPickup;

    // priemerna doba zakaznika v celej simulacii
    private ArithmeticalStatistic repCustomerSystemTime;

    // aktualne dlzky a cas poslednej zmeny pre weighted priemer
    private int unloadQueueLen;
    private int metalQueueLen;
    private int xrayQueueLen;
    private int pickupQueueLen;

    private double lastUnloadLenChangeTime;
    private double lastMetalLenChangeTime;
    private double lastXrayLenChangeTime;
    private double lastPickupLenChangeTime;

    // mapy vstupov do radov
    private final Map<Long, Double> customerEnterSystemAt = new HashMap<>();
    private final Map<Long, Double> customerEnterUnloadQueueAt = new HashMap<>();
    private final Map<Long, Double> customerEnterMetalQueueAt = new HashMap<>();
    private final Map<Long, Double> crateEnterXrayQueueAt = new HashMap<>();
    private final Map<Long, Double> crateEnterPickupQueueAt = new HashMap<>();

    // ---------- SIMULACIA (priemer cez replikacie) ----------
    private final ArithmeticalStatistic simCustomersEnteredUnloadQueue;
    private final ArithmeticalStatistic simCustomersEnteredMetalQueue;

    //intervaly spolahlivosti
    private final ConfidenceIntervalStatistic ciAvgCustomerSystemTime;
    private final ConfidenceIntervalStatistic ciAvgPickupQueueLen;
    private final ConfidenceIntervalStatistic ciAvgMetalQueueLen;


    public ArithmeticalStatistic getSimAvgCustomerSystemTime() {
        return simAvgCustomerSystemTime;
    }

    public ArithmeticalStatistic getSimAvgWaitPickup() {
        return simAvgWaitPickup;
    }

    public ArithmeticalStatistic getSimAvgWaitXray() {
        return simAvgWaitXray;
    }

    public ArithmeticalStatistic getSimAvgWaitMetal() {
        return simAvgWaitMetal;
    }

    public ArithmeticalStatistic getSimAvgWaitUnload() {
        return simAvgWaitUnload;
    }

    public ArithmeticalStatistic getSimAvgPickupQueueLen() {
        return simAvgPickupQueueLen;
    }

    public ArithmeticalStatistic getSimAvgXrayQueueLen() {
        return simAvgXrayQueueLen;
    }

    public ArithmeticalStatistic getSimAvgMetalQueueLen() {
        return simAvgMetalQueueLen;
    }

    public ArithmeticalStatistic getSimAvgUnloadQueueLen() {
        return simAvgUnloadQueueLen;
    }

    public ArithmeticalStatistic getSimCratesEnteredPickupQueue() {
        return simCratesEnteredPickupQueue;
    }

    public ArithmeticalStatistic getSimCratesEnteredXrayQueue() {
        return simCratesEnteredXrayQueue;
    }

    public ArithmeticalStatistic getSimCustomersEnteredMetalQueue() {
        return simCustomersEnteredMetalQueue;
    }

    public ArithmeticalStatistic getSimCustomersEnteredUnloadQueue() {
        return simCustomersEnteredUnloadQueue;
    }

    public ArithmeticalStatistic getRepWaitUnload() {
        return repWaitUnload;
    }

    public ArithmeticalStatistic getRepWaitMetal() {
        return repWaitMetal;
    }

    public ArithmeticalStatistic getRepWaitXray() {
        return repWaitXray;
    }

    public ArithmeticalStatistic getRepWaitPickup() {
        return repWaitPickup;
    }

    public ArithmeticalStatistic getRepCustomerSystemTime() {
        return repCustomerSystemTime;
    }

    public ConfidenceIntervalStatistic getCiAvgCustomerSystemTime() {
        return ciAvgCustomerSystemTime;
    }

    public ConfidenceIntervalStatistic getCiAvgPickupQueueLen() {
        return ciAvgPickupQueueLen;
    }

    public ConfidenceIntervalStatistic getCiAvgMetalQueueLen() {
        return ciAvgMetalQueueLen;
    }

    private final ArithmeticalStatistic simCratesEnteredXrayQueue;
    private final ArithmeticalStatistic simCratesEnteredPickupQueue;

    private final ArithmeticalStatistic simAvgUnloadQueueLen;
    private final ArithmeticalStatistic simAvgMetalQueueLen;
    private final ArithmeticalStatistic simAvgXrayQueueLen;
    private final ArithmeticalStatistic simAvgPickupQueueLen;

    private final ArithmeticalStatistic simAvgWaitUnload;
    private final ArithmeticalStatistic simAvgWaitMetal;
    private final ArithmeticalStatistic simAvgWaitXray;
    private final ArithmeticalStatistic simAvgWaitPickup;

    private final ArithmeticalStatistic simAvgCustomerSystemTime;

    public StatsForAirport() {
        simCustomersEnteredUnloadQueue = new ArithmeticalStatistic();
        simCustomersEnteredMetalQueue = new ArithmeticalStatistic();
        simCratesEnteredXrayQueue = new ArithmeticalStatistic();
        simCratesEnteredPickupQueue = new ArithmeticalStatistic();

        simAvgUnloadQueueLen = new ArithmeticalStatistic();
        simAvgMetalQueueLen = new ArithmeticalStatistic();
        simAvgXrayQueueLen = new ArithmeticalStatistic();
        simAvgPickupQueueLen = new ArithmeticalStatistic();

        simAvgWaitUnload = new ArithmeticalStatistic();
        simAvgWaitMetal = new ArithmeticalStatistic();
        simAvgWaitXray = new ArithmeticalStatistic();
        simAvgWaitPickup = new ArithmeticalStatistic();

        simAvgCustomerSystemTime = new ArithmeticalStatistic();

        ciAvgCustomerSystemTime = new ConfidenceIntervalStatistic();
        ciAvgPickupQueueLen = new ConfidenceIntervalStatistic();
        ciAvgMetalQueueLen = new ConfidenceIntervalStatistic();

        beforeRep();
    }

    public void beforeRep() {
        repCustomersEnteredUnloadQueue = 0;
        repCustomersEnteredMetalQueue = 0;
        repCratesEnteredXrayQueue = 0;
        repCratesEnteredPickupQueue = 0;

        repUnloadQueueLen = new WeightedStatistic();
        repMetalQueueLen = new WeightedStatistic();
        repXrayQueueLen = new WeightedStatistic();
        repPickupQueueLen = new WeightedStatistic();

        repWaitUnload = new ArithmeticalStatistic();
        repWaitMetal = new ArithmeticalStatistic();
        repWaitXray = new ArithmeticalStatistic();
        repWaitPickup = new ArithmeticalStatistic();

        repCustomerSystemTime = new ArithmeticalStatistic();

        unloadQueueLen = 0;
        metalQueueLen = 0;
        xrayQueueLen = 0;
        pickupQueueLen = 0;

        lastUnloadLenChangeTime = 0.0;
        lastMetalLenChangeTime = 0.0;
        lastXrayLenChangeTime = 0.0;
        lastPickupLenChangeTime = 0.0;

        customerEnterSystemAt.clear();
        customerEnterUnloadQueueAt.clear();
        customerEnterMetalQueueAt.clear();
        crateEnterXrayQueueAt.clear();
        crateEnterPickupQueueAt.clear();
    }

    public void afterRep() {
        simCustomersEnteredUnloadQueue.addValue(repCustomersEnteredUnloadQueue, 1.0);
        simCustomersEnteredMetalQueue.addValue(repCustomersEnteredMetalQueue, 1.0);
        simCratesEnteredXrayQueue.addValue(repCratesEnteredXrayQueue, 1.0);
        simCratesEnteredPickupQueue.addValue(repCratesEnteredPickupQueue, 1.0);

        simAvgUnloadQueueLen.addValue(repUnloadQueueLen.getValue(), 1.0);
        simAvgMetalQueueLen.addValue(repMetalQueueLen.getValue(), 1.0);
        simAvgXrayQueueLen.addValue(repXrayQueueLen.getValue(), 1.0);
        simAvgPickupQueueLen.addValue(repPickupQueueLen.getValue(), 1.0);

        simAvgWaitUnload.addValue(repWaitUnload.getValue(), 1.0);
        simAvgWaitMetal.addValue(repWaitMetal.getValue(), 1.0);
        simAvgWaitXray.addValue(repWaitXray.getValue(), 1.0);
        simAvgWaitPickup.addValue(repWaitPickup.getValue(), 1.0);

        simAvgCustomerSystemTime.addValue(repCustomerSystemTime.getValue(), 1.0);

        ciAvgCustomerSystemTime.addValue(repCustomerSystemTime.getValue(), 1.0);
        ciAvgPickupQueueLen.addValue(repPickupQueueLen.getValue(), 1.0);
        ciAvgMetalQueueLen.addValue(repMetalQueueLen.getValue(), 1.0);
    }

    public void beginMeasuredWindow(double now, int unloadLen, int metalLen, int xrayLen, int pickupLen) {
        beforeRep();

        this.unloadQueueLen = unloadLen;
        this.metalQueueLen = metalLen;
        this.xrayQueueLen = xrayLen;
        this.pickupQueueLen = pickupLen;

        this.lastUnloadLenChangeTime = now;
        this.lastMetalLenChangeTime = now;
        this.lastXrayLenChangeTime = now;
        this.lastPickupLenChangeTime = now;
    }

    public void finalizeReplication(double now) {
        closeQueueLengthStatsAt(now);
    }

    public void onCustomerEnteredSystem(long customerId, double now) {
        customerEnterSystemAt.put(customerId, now);
    }

    public void onCustomerFinishedSystem(long customerId, double now) {
        Double enter = customerEnterSystemAt.remove(customerId);
        if (enter != null) {
            repCustomerSystemTime.addValue(now - enter, 1.0);
        }
    }

    public void onCustomerEnteredUnloadQueue(long customerId, double now, int newQueueLen) {
        repCustomersEnteredUnloadQueue++;
        customerEnterUnloadQueueAt.put(customerId, now);
        updateUnloadQueueLen(newQueueLen, now);
    }

    public void onCustomerLeftUnloadQueue(long customerId, double now, int newQueueLen) {
        Double enter = customerEnterUnloadQueueAt.remove(customerId);
        if (enter != null) {
            repWaitUnload.addValue(now - enter, 1.0);
        }
        updateUnloadQueueLen(newQueueLen, now);
    }

    public void onCustomerEnteredMetalQueue(long customerId, double now, int newQueueLen) {
        repCustomersEnteredMetalQueue++;
        customerEnterMetalQueueAt.put(customerId, now);
        updateMetalQueueLen(newQueueLen, now);
    }

    public void onCustomerLeftMetalQueue(long customerId, double now, int newQueueLen) {
        Double enter = customerEnterMetalQueueAt.remove(customerId);
        if (enter != null) {
            repWaitMetal.addValue(now - enter, 1.0);
        }
        updateMetalQueueLen(newQueueLen, now);
    }

    public void onCrateEnteredXrayQueue(long crateId, double now, int newQueueLen) {
        repCratesEnteredXrayQueue++;
        crateEnterXrayQueueAt.put(crateId, now);
        updateXrayQueueLen(newQueueLen, now);
    }

    public void onCrateLeftXrayQueue(long crateId, double now, int newQueueLen) {
        Double enter = crateEnterXrayQueueAt.remove(crateId);
        if (enter != null) {
            repWaitXray.addValue(now - enter, 1.0);
        }
        updateXrayQueueLen(newQueueLen, now);
    }

    public void onCrateEnteredPickupQueue(long crateId, double now, int newQueueLen) {
        repCratesEnteredPickupQueue++;
        crateEnterPickupQueueAt.put(crateId, now);
        updatePickupQueueLen(newQueueLen, now);
    }

    public void onCrateLeftPickupQueue(long crateId, double now, int newQueueLen) {
        Double enter = crateEnterPickupQueueAt.remove(crateId);
        if (enter != null) {
            repWaitPickup.addValue(now - enter, 1.0);
        }
        updatePickupQueueLen(newQueueLen, now);
    }

    private void updateUnloadQueueLen(int newLen, double now) {
        double dt = now - lastUnloadLenChangeTime;
        if (dt > 0) repUnloadQueueLen.addValue(unloadQueueLen, dt);
        unloadQueueLen = newLen;
        lastUnloadLenChangeTime = now;
    }

    private void updateMetalQueueLen(int newLen, double now) {
        double dt = now - lastMetalLenChangeTime;
        if (dt > 0) repMetalQueueLen.addValue(metalQueueLen, dt);
        metalQueueLen = newLen;
        lastMetalLenChangeTime = now;
    }

    private void updateXrayQueueLen(int newLen, double now) {
        double dt = now - lastXrayLenChangeTime;
        if (dt > 0) repXrayQueueLen.addValue(xrayQueueLen, dt);
        xrayQueueLen = newLen;
        lastXrayLenChangeTime = now;
    }

    private void updatePickupQueueLen(int newLen, double now) {
        double dt = now - lastPickupLenChangeTime;
        if (dt > 0) repPickupQueueLen.addValue(pickupQueueLen, dt);
        pickupQueueLen = newLen;
        lastPickupLenChangeTime = now;
    }

    private void closeQueueLengthStatsAt(double now) {
        updateUnloadQueueLen(unloadQueueLen, now);
        updateMetalQueueLen(metalQueueLen, now);
        updateXrayQueueLen(xrayQueueLen, now);
        updatePickupQueueLen(pickupQueueLen, now);
    }

    public long getRepCustomersEnteredUnloadQueue() {
        return repCustomersEnteredUnloadQueue;
    }

    public long getRepCustomersEnteredMetalQueue() {
        return repCustomersEnteredMetalQueue;
    }

    public long getRepCratesEnteredXrayQueue() {
        return repCratesEnteredXrayQueue;
    }

    public long getRepCratesEnteredPickupQueue() {
        return repCratesEnteredPickupQueue;
    }
}
