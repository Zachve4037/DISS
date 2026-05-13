package S3.agents.agentpatients;

import OSPABA.*;
import S3.entities.ClinicType;
import S3.entities.Patient;
import S3.entities.PatientType;
import S3.simulation.*;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

//meta! id="62"
public class AgentPatients extends OSPABA.Agent {

    private Queue<Patient> initialExaminationQueue;
    private Queue<Patient> doctorsExaminationQueue;

    private Comparator<Patient> initialQueueComparator;
    private Comparator<Patient> doctorQueueComparator;

    public AgentPatients(int id, Simulation mySim, Agent parent) {
        super(id, mySim, parent);
        init();

        this.initialQueueComparator = Comparator
                .comparingInt((Patient p) -> p.getType() == PatientType.AMBULANCE ? 0 : 1)
                .thenComparingDouble(Patient::getArrivalTime)
                .thenComparingInt(Patient::getId);

        // Lower priority number means more urgent.
        this.doctorQueueComparator = Comparator
                .comparingInt(Patient::getPriority)
                .thenComparingDouble(Patient::getWaitStartTime)
                .thenComparingInt(Patient::getId);

        initialExaminationQueue = new PriorityBlockingQueue<>(11, initialQueueComparator);
        doctorsExaminationQueue = new PriorityBlockingQueue<>(11, doctorQueueComparator);
    }

    public Queue<Patient> getInitialExaminationQueue() {
        return initialExaminationQueue;
    }

    public Queue<Patient> getDoctorsExaminationQueue() {
        return doctorsExaminationQueue;
    }

    public Queue<Patient> getDoctorsExaminationQueues() {
        Queue<Patient> copy = new PriorityBlockingQueue<>(Math.max(11, doctorsExaminationQueue.size() + 1), doctorQueueComparator);
        copy.addAll(doctorsExaminationQueue);
        return copy;
    }

    // Compatibility helpers kept for older GUI/manager code.
    public Queue<Patient> getDoctorsExaminationQueueA() {
        Queue<Patient> copy = new PriorityBlockingQueue<>(11, doctorQueueComparator);
        for (Patient p : doctorsExaminationQueue) {
            if (canUseClinic(p, ClinicType.A)) {
                copy.add(p);
            }
        }
        return copy;
    }

    public Queue<Patient> getDoctorsExaminationQueueB() {
        Queue<Patient> copy = new PriorityBlockingQueue<>(11, doctorQueueComparator);
        for (Patient p : doctorsExaminationQueue) {
            if (canUseClinic(p, ClinicType.B)) {
                copy.add(p);
            }
        }
        return copy;
    }

    public boolean hasDoctorPatientFor(ClinicType clinicType) {
        for (Patient patient : doctorsExaminationQueue) {
            if (canUseClinic(patient, clinicType)) {
                return true;
            }
        }
        return false;
    }

    public Patient pollDoctorPatientFor(ClinicType clinicType) {
        Patient best = null;

        for (Patient patient : doctorsExaminationQueue) {
            if (!canUseClinic(patient, clinicType)) {
                continue;
            }
            if (best == null || doctorQueueComparator.compare(patient, best) < 0) {
                best = patient;
            }
        }

        if (best != null) {
            doctorsExaminationQueue.remove(best);
        }

        return best;
    }

    public boolean hasDoctorPatients() {
        return !doctorsExaminationQueue.isEmpty();
    }

    public boolean hasDoctorPatientOnlyForA() {
        for (Patient patient : doctorsExaminationQueue) {
            if (patient.getPriority() == 1 || patient.getPriority() == 2) {
                return true;
            }
        }
        return false;
    }

    public Patient peekDoctorPatient() {
        return doctorsExaminationQueue.peek();
    }

    public Patient pollDoctorPatient() {
        return doctorsExaminationQueue.poll();
    }

    public boolean hasInitialPatients() {
        return !initialExaminationQueue.isEmpty();
    }

    private boolean canUseClinic(Patient patient, ClinicType clinicType) {
        if (patient == null || clinicType == null) {
            return false;
        }

        int priority = patient.getPriority();

        if (clinicType == ClinicType.A) {
            return priority >= 1 && priority <= 4;
        }

        if (clinicType == ClinicType.B) {
            return priority >= 3 && priority <= 5;
        }

        return false;
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        initialExaminationQueue = new PriorityBlockingQueue<>(11, initialQueueComparator);
        doctorsExaminationQueue = new PriorityBlockingQueue<>(11, doctorQueueComparator);
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    private void init() {
        new ManagerPatients(Id.managerPatients, mySim(), this);
        addOwnMessage(Mc.getPatientForDoctorsExamination);
        addOwnMessage(Mc.putPatient);
        addOwnMessage(Mc.getPatientForInitialExamination);
    }
    //meta! tag="end"
}
