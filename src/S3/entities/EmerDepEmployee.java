package S3.entities;

import OSPABA.Entity;
import OSPABA.Simulation;

public class EmerDepEmployee extends Entity {
    private final int id;
    private final EmployeeType type;

    private Patient currentPatient;
    private Clinic currentClinic;

    private EmployeeState state;

    private double workTime;
    private double workStartTime;
    private double totalWorkTime;

    public EmerDepEmployee(Simulation mySim, int id, EmployeeType type) {
        super(mySim);
        this.id = id;
        this.type = type;
        this.currentPatient = null;
        this.currentClinic = null;
        this.state = EmployeeState.IDLE;
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

    public EmployeeState getState() {
        return state;
    }

    public boolean isBusy() {
        return state != EmployeeState.IDLE;
    }

    public void reserve(Clinic clinic) {
        this.currentClinic = clinic;
        this.state = EmployeeState.RESERVED;
    }

    public void startMoving() {
        this.state = EmployeeState.MOVING;
    }

    public void finishMoving() {
        this.state = EmployeeState.RESERVED;
    }

    public void finishInitialMovement() {
        this.state = EmployeeState.IDLE;
    }

    public void startWork(Patient patient) {
        this.workStartTime = mySim().currentTime();
        this.currentPatient = patient;
        this.state = EmployeeState.WORKING;

        if (patient != null) {
            patient.startService();
        }
    }

    public void endWork() {
        if (state == EmployeeState.WORKING) {
            this.workTime = mySim().currentTime() - this.workStartTime;
            this.totalWorkTime += this.workTime;
        }

        if (currentPatient != null) {
            currentPatient.endService();
        }

        this.currentPatient = null;
        this.currentClinic = null;
        this.state = EmployeeState.IDLE;
    }

    public void setBusy(boolean busy) {
        this.state = busy ? EmployeeState.RESERVED : EmployeeState.IDLE;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public double getTotalWorkTime() {
        return totalWorkTime;
    }

    public double getWorkStartTime() {
        return workStartTime;
    }

}
