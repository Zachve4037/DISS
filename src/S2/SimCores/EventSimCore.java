package S2.SimCores;

import S2.Events.Event;
import S2.Statistics.ArithmeticalStatistic;
import S2.Statistics.WeightedStatistic;

import java.util.PriorityQueue;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public abstract class EventSimCore extends SimCore {
    private final PriorityQueue<Event> eventCalendar = new PriorityQueue<>();
    protected double currentTime;
    protected int servedCustomers = 0;

    private volatile BooleanSupplier maxSpeedSupplier = () -> true;
    private volatile IntSupplier speedFactorSupplier = () -> 200;
    private volatile double visualTimeSeconds = 0.0;

    //global
    private ArithmeticalStatistic avgTimeInQueueOverReps;
    private ArithmeticalStatistic avgQueueLengthOverReps;
    private ArithmeticalStatistic avgServiceTimeOverReps;

    //replikacie
    private ArithmeticalStatistic repTimeInQueue;
    private ArithmeticalStatistic repServiceTime;
    private WeightedStatistic repQueueLengthTimeWeighted;
    private double lastQueueLenChangeTime;
    private int queueLength;
    private int completedReps;
    private Long totalServedCustomers = 0L;

    protected abstract boolean shouldStop();
    protected abstract void initialEvents();

    @Override
    protected void beforeSimulation() {
        this.avgTimeInQueueOverReps = new ArithmeticalStatistic();
        this.avgQueueLengthOverReps = new ArithmeticalStatistic();
        this.avgServiceTimeOverReps = new ArithmeticalStatistic();
        this.totalServedCustomers = 0L;
        this.completedReps = 0;
    }

    @Override
    protected void beforeReplication() {
        this.currentTime = 0;
        this.eventCalendar.clear();
        this.servedCustomers = 0;
        this.visualTimeSeconds = 0.0;

        repTimeInQueue = new ArithmeticalStatistic();
        repServiceTime = new ArithmeticalStatistic();
        repQueueLengthTimeWeighted = new WeightedStatistic();

        this.lastQueueLenChangeTime = 0;
        this.queueLength = 0;

        this.initialEvents();
    }

    @Override
    protected void doReplication() {
        while (isRun() && !this.eventCalendar.isEmpty() && !shouldStop()) {
            Event event = this.eventCalendar.poll();
            if (event == null) break;

            double nextEventTime = event.getTimeOfTheEvent();
            double simDelta = nextEventTime - this.currentTime;
            applyRealtimePacing(simDelta);

            this.currentTime = nextEventTime;
            this.visualTimeSeconds = this.currentTime;
            event.execute();
        }
        double dt = currentTime - lastQueueLenChangeTime;
        if (dt > 0) {
            repQueueLengthTimeWeighted.addValue(queueLength, dt);
        }
    }

    @Override
    protected void afterReplication() {
        this.avgTimeInQueueOverReps.addValue(repTimeInQueue.getValue(), 1);
        this.avgServiceTimeOverReps.addValue(repServiceTime.getValue(), 1);
        this.avgQueueLengthOverReps.addValue(repQueueLengthTimeWeighted.getValue(), 1);
        this.completedReps++;
    }

    @Override
    protected void afterSimulation() {
        System.out.println("Simulation finished after " + this.getReplications() + " replications.");
        System.out.println("Average time in queue: " + this.avgTimeInQueueOverReps.getValue());
        System.out.println("Average queue length: " + this.avgQueueLengthOverReps.getValue());
        System.out.println("Average service time: " + this.avgServiceTimeOverReps.getValue());
    }

    private void applyRealtimePacing(double simDeltaSeconds) {
        if (simDeltaSeconds <= 0.0) return;

        // max speed: bez cakania, GUI cas preskoci na dalsi event
        if (maxSpeedSupplier.getAsBoolean()) {
            visualTimeSeconds += simDeltaSeconds;
            return;
        }

        double remainingSim = simDeltaSeconds;
        final double realStepSeconds = 0.05; // 50 ms, dobra odozva GUI/checkboxu

        while (remainingSim > 1e-9 && isRun()) {
            if (maxSpeedSupplier.getAsBoolean()) {
                visualTimeSeconds += remainingSim;
                return;
            }

            int factor = Math.max(1, speedFactorSupplier.getAsInt()); // 1..200
            double simStep = realStepSeconds * factor;
            if (simStep > remainingSim) {
                simStep = remainingSim;
            }

            long sleepNanos = Math.round((simStep / factor) * 1_000_000_000.0);
            long millis = sleepNanos / 1_000_000L;
            int nanos = (int) (sleepNanos % 1_000_000L);

            try {
                Thread.sleep(millis, nanos);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                setRun(false);
                return;
            }

            visualTimeSeconds += simStep;
            remainingSim -= simStep;
        }
    }

    public void setPacingSuppliers(BooleanSupplier maxSpeedSupplier, IntSupplier speedFactorSupplier) {
        this.maxSpeedSupplier = (maxSpeedSupplier != null) ? maxSpeedSupplier : () -> true;
        this.speedFactorSupplier = (speedFactorSupplier != null) ? speedFactorSupplier : () -> 200;
    }

    public void scheduleEvent(Event e) {
        this.eventCalendar.add(e);
    }

    protected void queueLengthChange(int newQueueLength) {
        double dt = currentTime - lastQueueLenChangeTime;
        if (dt > 0) {
            repQueueLengthTimeWeighted.addValue(queueLength, dt);
        }
        this.queueLength = newQueueLength;
        this.lastQueueLenChangeTime = currentTime;
    }

    public void customerLeftQueue(double seconds) {
        this.repTimeInQueue.addValue(seconds, 1.0);
    }

    public void customerServed(double serviceTime) {
        this.repServiceTime.addValue(serviceTime, 1.0);
        this.servedCustomers++;
        this.totalServedCustomers++;
    }

    public int getServedCustomers() {
        return servedCustomers;
    }

    public double getAvgServedCustomers() {
        if (this.completedReps == 0) return 0.0;
        return (double) totalServedCustomers / this.completedReps;
    }

    public long getTotalServedCustomers() {
        return totalServedCustomers;
    }

    public long getCompletedReplications() {
        return completedReps;
    }

    public double getVisualTimeSeconds() {
        return visualTimeSeconds;
    }
}