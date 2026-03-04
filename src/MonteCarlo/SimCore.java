package MonteCarlo;

public abstract class SimCore {
    private boolean run = true;

    public void runSimulation(int replicationCount) {
        int currentReplication = 1;
        beforeSimulation();
        while (currentReplication <= replicationCount && run) {
            beforeReplication();
            doReplication();
            afterReplication();
            currentReplication++;
        }
        afterSimulation();
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    protected abstract void beforeSimulation();

    protected abstract void beforeReplication();

    protected abstract void doReplication();

    protected abstract void afterReplication();

    protected abstract void afterSimulation();
}