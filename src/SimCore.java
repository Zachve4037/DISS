public class SimCore {
    private int reps;
    private boolean stopped = false;
    public SimCore(int counts) {
        this.reps = counts;
        sim();
    }

    private void sim() {
        int counter = 0;
        beforeSim();
        while (counter < 1000000 && !stopped) {
            beforeRep();
            replication();
            afterRep();
            counter++;
        }
        afterSim();
    }

    private void beforeSim() {

    }

    private void afterSim() {

    }

    private void afterRep() {

    }

    private void replication() {

    }

    private void beforeRep() {

    }
}
