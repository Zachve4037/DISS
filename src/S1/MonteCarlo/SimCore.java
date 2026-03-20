package S1.MonteCarlo;

public abstract class SimCore {
    private volatile boolean run = true;

    public void runSimulation(long replicationCount) {
        setRun(true);
        beforeSimulation();
        long i = 0;
        while (isRun() && (replicationCount <= 0 || i < replicationCount)) {
            beforeReplication();
            if (!isRun()) break;
            doReplication();
            afterReplication();
            i++;
        }
        afterSimulation();
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    protected boolean isRun() {
        return this.run;
    }

    protected abstract void beforeSimulation();

    protected abstract void beforeReplication();

    protected abstract void doReplication();

    protected abstract void afterReplication();

    protected abstract void afterSimulation();
}