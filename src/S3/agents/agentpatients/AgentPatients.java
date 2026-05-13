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
        this.initialQueueComparator = Comparator.comparingInt(
                (Patient p) -> p.getType() == PatientType.AMBULANCE ? 0 : 1).
                thenComparingDouble(Patient::getArrivalTime).
                thenComparingInt(Patient::getId);
        this.doctorQueueComparator = Comparator.comparingInt(Patient::getPriority).
                thenComparingDouble(Patient::getArrivalTime).
                thenComparingInt(Patient::getId);
        initialExaminationQueue = new PriorityBlockingQueue<>(11, initialQueueComparator);
        doctorsExaminationQueue = new PriorityBlockingQueue<>(11, doctorQueueComparator);
    }

    public Patient pollDoctorPatientFor(ClinicType clinicType) {
        Patient best = doctorsExaminationQueue.stream()
                .filter(p -> canUseClinic(p, clinicType))
                .min(doctorQueueComparator)
                .orElse(null);

        if (best != null) {
            doctorsExaminationQueue.remove(best);
        }

        return best;
    }

    private boolean canUseClinic(Patient patient, ClinicType clinicType) {
        int priority = patient.getPriority();

        if (clinicType == ClinicType.A) {
            return priority >= 1 && priority <= 4;
        }

        if (clinicType == ClinicType.B) {
            return priority >= 3 && priority <= 5;
        }

        return false;
    }

    public Queue<Patient> getDoctorsExaminationQueues() {
        Queue<Patient> copy = new PriorityBlockingQueue<>(11, doctorQueueComparator);
        copy.addAll(doctorsExaminationQueue);
        return copy;
    }

    public Queue<Patient> getDoctorsExaminationQueue() {
        return this.doctorsExaminationQueue;
    }

    public Queue<Patient> getInitialExaminationQueue() {
        return initialExaminationQueue;
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