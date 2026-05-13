package S3.entities;

import OSPABA.Entity;
import OSPABA.Simulation;

public class Patient extends Entity {
    private final int id;
    private final PatientType type;

    private int priority;
    private final double arrivalTime;

    private double waitStartTime;
    private double totalWaitTime;
    private boolean isWaiting;

    private double serviceStartTime;
    private double totalServiceTime;
    private boolean isInService;

    private boolean isWaitingForDoctorsExam;
    private boolean isWaitingForInitialExam;
    private Clinic lastClinic;

    private double initialExamWaitTime;
    private double doctorsExamWaitTime;

    private double doctorsExamWaitStartTime;
    private boolean doctorsExamWaitRunning;
    private boolean doctorsExamWaitRecorded;

    private double finalizedTotalTimeInSystem;
    private boolean statisticsFinalized;

    private WaitingPhase waitingPhase;

    private enum WaitingPhase {
        NONE,
        INITIAL_EXAM,
        DOCTORS_EXAM
    }

    public Patient(Simulation mySim, int id, PatientType type) {
        super(mySim);
        this.id = id;
        this.type = type;
        this.arrivalTime = mySim.currentTime();

        this.waitStartTime = 0;
        this.totalWaitTime = 0;
        this.isWaiting = false;

        this.serviceStartTime = 0;
        this.totalServiceTime = 0;
        this.isInService = false;

        this.isWaitingForInitialExam = true;
        this.isWaitingForDoctorsExam = false;
        this.lastClinic = null;

        this.initialExamWaitTime = 0;
        this.doctorsExamWaitTime = 0;
        this.doctorsExamWaitStartTime = this.arrivalTime;
        this.doctorsExamWaitRunning = true;
        this.doctorsExamWaitRecorded = false;

        this.finalizedTotalTimeInSystem = 0;
        this.statisticsFinalized = false;
        this.waitingPhase = WaitingPhase.NONE;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public void startWaitingForInitialExam() {
        startWaiting(WaitingPhase.INITIAL_EXAM);
    }

    public void startWaitingForDoctorsExam() {
        startDoctorsExamWaitIfNeeded();
        startWaiting(WaitingPhase.DOCTORS_EXAM);
    }

    private void startWaiting(WaitingPhase phase) {
        if (!isWaiting) {
            this.waitStartTime = mySim().currentTime();
            this.isWaiting = true;
        }
        this.waitingPhase = phase;
    }

    public void endWaiting() {
        if (isWaiting) {
            double waited = mySim().currentTime() - this.waitStartTime;
            this.totalWaitTime += waited;

            if (waitingPhase == WaitingPhase.INITIAL_EXAM) {
                this.initialExamWaitTime += waited;
            } else if (waitingPhase == WaitingPhase.DOCTORS_EXAM) {
                endDoctorsExamWaitIfRunning();
            }

            this.isWaiting = false;
            this.waitingPhase = WaitingPhase.NONE;
        }
    }

    private void startDoctorsExamWaitIfNeeded() {
        if (!doctorsExamWaitRunning && !doctorsExamWaitRecorded) {
            this.doctorsExamWaitStartTime = mySim().currentTime();
            this.doctorsExamWaitRunning = true;
        }
    }

    private void endDoctorsExamWaitIfRunning() {
        if (doctorsExamWaitRunning && !doctorsExamWaitRecorded) {
            this.doctorsExamWaitTime = mySim().currentTime() - this.doctorsExamWaitStartTime;
            this.doctorsExamWaitRunning = false;
            this.doctorsExamWaitRecorded = true;
        }
    }

    public double getTotalWaitTime() {
        return totalWaitTime;
    }

    public double getWaitStartTime() {
        return waitStartTime;
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

    public double getInitialExamWaitTime() {
        return initialExamWaitTime;
    }

    public double getDoctorsExamWaitTime() {
        return doctorsExamWaitTime;
    }

    public void finalizeStatistics() {
        if (isWaiting) {
            endWaiting();
        }
        if (doctorsExamWaitRunning) {
            endDoctorsExamWaitIfRunning();
        }
        if (isInService) {
            endService();
        }
        this.finalizedTotalTimeInSystem = mySim().currentTime() - arrivalTime;
        this.statisticsFinalized = true;
    }

    public double getTotalTimeInSystem() {
        return statisticsFinalized
                ? finalizedTotalTimeInSystem
                : mySim().currentTime() - arrivalTime;
    }

    public boolean isWaitingForDoctorsExam() {
        return isWaitingForDoctorsExam;
    }

    public void setWaitingForDoctorsExam(boolean waitingForDoctorsExam) {
        isWaitingForDoctorsExam = waitingForDoctorsExam;
    }

    public boolean isWaitingForInitialExam() {
        return isWaitingForInitialExam;
    }

    public void setWaitingForInitialExam(boolean waitingForInitialExam) {
        isWaitingForInitialExam = waitingForInitialExam;
    }

    public Clinic getLastClinic() {
        return lastClinic;
    }

    public void setLastClinic(Clinic lastClinic) {
        this.lastClinic = lastClinic;
    }
}
