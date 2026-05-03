package S3.entities;

import OSPABA.Entity;
import OSPABA.Simulation;

public class EmerDepEmployee extends Entity {
    private final int id;
    private final EmployeeType type;

    private Patient currentPatient;
    private boolean working;
    private double workTime;
    private double workStartTime;
    private double totalWorkTime;

    public EmerDepEmployee(Simulation mySim, int id, EmployeeType type) {
        super(mySim);
        this.id = id;
        this.type = type;
        this.currentPatient = null;
        this.working = false;
        this.workTime = 0;
        this.workStartTime = 0;
        this.totalWorkTime = 0;
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
        this.working = true;

        if (patient != null) {
            patient.startService();
        }
    }

    public void endWork() {
        this.working = false;
        this.workTime = mySim().currentTime() - this.workStartTime;
        this.totalWorkTime += this.workTime;

        if (currentPatient != null) {
            currentPatient.endService();
        }

        this.currentPatient = null;
    }

    public boolean isWorking() {
        return working;
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
}
