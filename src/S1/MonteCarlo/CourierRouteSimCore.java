package S1.MonteCarlo;

import S1.timeSamplers.RouteTimeSampler;
import S1.timeTracker.TimeTracker;

public class CourierRouteSimCore extends SimCore {
    public interface ProgressListener {
        void onProgress(long replication, double avgMinutes);
        void onFinished(long replications, double avgMinutes, String avgReturnTime);
    }

    private final RouteTimeSampler routeTimeSampler = new RouteTimeSampler();
    private final String routeId;
    private final int progressStep;
    private final ProgressListener listener;

    private final Object pauseLock = new Object();
    private volatile boolean paused = false;

    private long replicationsDone;
    private double totalMinutes;

    public CourierRouteSimCore(String routeId, int progressStep, ProgressListener listener) {
        this.routeId = routeId;
        this.progressStep = progressStep;
        this.listener = listener;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (!paused) {
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
        }
    }

    public void stopSimulation() {
        setRun(false);
        setPaused(false);
    }

    @Override
    protected void beforeSimulation() {
        replicationsDone = 0;
        totalMinutes = 0.0;
    }

    @Override
    protected void beforeReplication() {
        synchronized (pauseLock) {
            while (paused) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    setRun(false);
                    return;
                }
            }
        }
    }

    @Override
    protected void doReplication() {
        TimeTracker startAt600 = new TimeTracker(6, 0, 0);
        double routeMinutes = routeTimeSampler.sampleRouteMinutes(startAt600, routeId);
        totalMinutes += routeMinutes;
        replicationsDone++;
    }

    @Override
    protected void afterReplication() {
        if (listener != null && progressStep > 0 && replicationsDone % progressStep == 0) {
            double avg = totalMinutes / replicationsDone;
            listener.onProgress(replicationsDone, avg);
        }
    }

    @Override
    protected void afterSimulation() {
        if (replicationsDone == 0) {
            if (listener != null) {
                listener.onFinished(0, 0.0, "06:00:00");
            }
            return;
        }

        double avgMinutes = totalMinutes / replicationsDone;
        TimeTracker returnTime = new TimeTracker(6, 0, 0);
        returnTime.addMinutes(avgMinutes);

        if (listener != null) {
            listener.onFinished(replicationsDone, avgMinutes, returnTime.getTime());
        }
    }
}
