package S3.agents.agentpatients;

import OSPABA.*;
import S3.entities.Patient;
import S3.entities.PatientType;
import S3.simulation.*;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

//meta! id="62"
public class AgentPatients extends OSPABA.Agent {

    //patients queues
    private Queue<Patient> initialExaminationQueue;
    private Queue<Patient> doctorsExaminationQueueA;
    private Queue<Patient> doctorsExaminationQueueB;
    private Comparator<Patient> initialQueueComparator;

    public AgentPatients(int id, Simulation mySim, Agent parent) {
        super(id, mySim, parent);
        init();
        this.initialQueueComparator = Comparator
                .comparingInt((Patient p) -> p.getType() == PatientType.AMBULANCE ? 0 : 1)
                .thenComparingDouble(Patient::getArrivalTime)
                .thenComparingInt(Patient::getId);
        initialExaminationQueue = new PriorityBlockingQueue<>(1, initialQueueComparator);
        doctorsExaminationQueueA = new ConcurrentLinkedQueue<>();
        doctorsExaminationQueueB = new ConcurrentLinkedQueue<>();
    }

    public Queue<Patient> getInitialExaminationQueue() {
        return initialExaminationQueue;
    }

    public Queue<Patient> getDoctorsExaminationQueues() {
        return doctorsExaminationQueueA.addAll(doctorsExaminationQueueB) ? doctorsExaminationQueueA : doctorsExaminationQueueB;
    }

    public Queue<Patient> getDoctorsExaminationQueueA() {
        return doctorsExaminationQueueA;
    }

    public Queue<Patient> getDoctorsExaminationQueueB() {
        return doctorsExaminationQueueB;
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        this.doctorsExaminationQueueA.clear();
        this.doctorsExaminationQueueB.clear();
        this.initialExaminationQueue = new PriorityBlockingQueue<>(11, initialQueueComparator);
        // Setup component for the next replication
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