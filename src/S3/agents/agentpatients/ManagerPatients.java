package S3.agents.agentpatients;

import OSPABA.*;
import S3.entities.Patient;
import S3.simulation.*;

//meta! id="62"
public class ManagerPatients extends OSPABA.Manager {
    public ManagerPatients(int id, Simulation mySim, Agent myAgent) {
        super(id, mySim, myAgent);
        init();
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication

        if (petriNet() != null) {
            petriNet().clear();
        }
    }

    //meta! sender="AgentHospital", id="79", type="Request"
    public void processGetPatientForDoctorsExamination(MessageForm message) {
    }

    //meta! sender="AgentHospital", id="78", type="Request"
    public void processGetPatientForInitialExamination(MessageForm message) {
    }

    //meta! userInfo="Process messages defined in code", id="0"
    public void processDefault(MessageForm message) {
        switch (message.code()) {
        }
    }

    //meta! sender="AgentHospital", id="84", type="Notice"
    public void processPutPatient(MessageForm message) {
        Message msg = (Message) message.createCopy();
        Patient current = msg.getPatient();
        if (current.isWaitingForInitialExam()) {
            myAgent().getInitialExaminationQueue().add(current);
        } else if (current.isWaitingForDoctorsExam()) {
            if (current.getPriority() <= 4) {
                myAgent().getDoctorsExaminationQueueA().add(current);
            } else {
                myAgent().getDoctorsExaminationQueueB().add(current);
            }
            //TODO upravit podla priority
        } else {
            System.out.println("Patient " + current.getId() + " is not waiting for any examination.");
        }
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    public void init() {
    }

    @Override
    public void processMessage(MessageForm message) {
        switch (message.code()) {
            case Mc.putPatient:
                processPutPatient(message);
                break;

            case Mc.getPatientForDoctorsExamination:
                processGetPatientForDoctorsExamination(message);
                break;

            case Mc.getPatientForInitialExamination:
                processGetPatientForInitialExamination(message);
                break;

            default:
                processDefault(message);
                break;
        }
    }
    //meta! tag="end"

    @Override
    public AgentPatients myAgent() {
        return (AgentPatients) super.myAgent();
    }

}