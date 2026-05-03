package S3.entities;

import OSPABA.Entity;
import OSPABA.Simulation;

public class Patient extends Entity {
    private final int id;
    private final PatientType type;

    private final double arrivalTime;

    private double waitStartTime;
    private double totalWaitTime;
    private boolean isWaiting;

    private double serviceStartTime;
    private double totalServiceTime;
    private boolean isInService;

    public Patient(Simulation mySim, int id, PatientType type) {
        super(mySim);
        this.id = id;
        this.type = type;
        this.arrivalTime = 0;
        this.waitStartTime = 0;
        this.totalWaitTime = 0;
        this.isWaiting = false;
        this.serviceStartTime = 0;
        this.totalServiceTime = 0;
        this.isInService = false;
    }

    public int getId() {
        return id;
    }

    public PatientType getType() {
        return type;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void startWaiting() {
        if (!isWaiting) {
            this.waitStartTime = mySim().currentTime();
            this.isWaiting = true;
        }
    }

    public void endWaiting() {
        if (isWaiting) {
            this.isWaiting = false;
            this.totalWaitTime += mySim().currentTime() - this.waitStartTime;
        }
    }

    public double getTotalWaitTime() {
        return totalWaitTime;
    }

    public double getWaitStartTime() {
        return waitStartTime;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public void startService() {
        endWaiting();

        if (!isInService) {
            this.serviceStartTime = mySim().currentTime();
            this.isInService = true;
        }
    }

    public void endService() {
        if (isInService) {
            this.isInService = false;
            this.totalServiceTime += mySim().currentTime() - this.serviceStartTime;
        }
    }

    public double getTotalServiceTime() {
        return totalServiceTime;
    }

    public double getServiceStartTime() {
        return serviceStartTime;
    }

    public boolean isInService() {
        return isInService;
    }

    public double getTotalTimeInSystem() {
        return mySim().currentTime() - arrivalTime;
    }
}
