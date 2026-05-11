package S3.entities;

import OSPABA.Entity;
import OSPABA.Simulation;

public class EmerDepEmployee extends Entity {
    private final int id;
    private final EmployeeType type;

    private Patient currentPatient;
    private Clinic currentClinic;
    private boolean busy;
    private double workTime;
    private double workStartTime;
    private double totalWorkTime;

    public EmerDepEmployee(Simulation mySim, int id, EmployeeType type) {
        super(mySim);
        this.id = id;
        this.type = type;
        this.currentPatient = null;
        this.busy = false;
        this.workTime = 0;
        this.workStartTime = 0;
        this.totalWorkTime = 0;
        this.currentClinic = null;
    }

    public int getId() {
        return id;
    }

    public EmployeeType getType() {
        return type;
    }

    public void startWork(Patient patient) {
        this.workStartTime = mySim().currentTime();
        this.currentPatient = patient;
        this.busy = true;

        if (patient != null) {
            patient.startService();
        }
    }

    public void endWork() {
        this.busy = false;
        this.workTime = mySim().currentTime() - this.workStartTime;
        this.totalWorkTime += this.workTime;

        if (currentPatient != null) {
            currentPatient.endService();
        }

        this.currentPatient = null;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public boolean isBusy() {
        return busy;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public double getWorkTime() {
        return workTime;
    }

    public double getTotalWorkTime() {
        return totalWorkTime;
    }

    public double getWorkStartTime() {
        return workStartTime;
    }

    public Clinic getCurrentClinic() {
        return currentClinic;
    }

    public void setCurrentClinic(Clinic clinic) {
        this.currentClinic = clinic;
    }
}
