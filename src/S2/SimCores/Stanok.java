package S2.SimCores;

import S2.Events.ArrivalEvent;
import S2.RNGs.exponential.ContinuousExponentialDistRNG;
import S2.RNGs.intervals.TriangleInterval;
import S2.RNGs.triangle.ContinuousTriangularDistRNG;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class Stanok extends EventSimCore {
    private final int SERVED_CUSTOMERS_LIMIT = 1000;
    private final ContinuousTriangularDistRNG serviceTimeRNG;
    private final ContinuousExponentialDistRNG arrivalTimeRNG;

    private final Queue<Double> waitingQueue = new ArrayDeque<>();
    private boolean serviceBusy;

    public Stanok(Random seed, int lambdaPerMinute, TriangleInterval serviceTimeInterval) {
        this.serviceTimeRNG = new ContinuousTriangularDistRNG(new Random(seed.nextInt()), serviceTimeInterval);
        double lambdaPerSecond = lambdaPerMinute / 60.0;
        this.arrivalTimeRNG = new ContinuousExponentialDistRNG(new Random(seed.nextInt()), lambdaPerSecond);
    }

    @Override
    public boolean shouldStop() {
        return this.servedCustomers >= SERVED_CUSTOMERS_LIMIT;
    }

    @Override
    protected void initialEvents() {
        this.waitingQueue.clear();
        this.serviceBusy = false;
        scheduleEvent(new ArrivalEvent(0.0, this));
    }

    public double sampleInterArrivalSeconds() {
        return this.arrivalTimeRNG.generate();
    }

    public double sampleServiceSeconds() {
        return this.serviceTimeRNG.generate();
    }

    public boolean isServiceBusy() {
        return this.serviceBusy;
    }

    public void setServiceBusy(boolean bool) {
        this.serviceBusy = bool;
    }

    public void enqueueCustomer(double arrivalTime) {
        waitingQueue.add(arrivalTime);
        super.queueLengthChange(waitingQueue.size());
    }

    public boolean hasWaitingCustomer() {
        return !waitingQueue.isEmpty();
    }

    public double dequeueCustomer() {
        double t = this.waitingQueue.poll();
        super.queueLengthChange(waitingQueue.size());
        return t;
    }
}
