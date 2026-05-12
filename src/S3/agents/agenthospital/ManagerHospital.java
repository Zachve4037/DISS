package S3.agents.agenthospital;

import OSPABA.*;
import S3.agents.agentresources.AgentResources;
import S3.entities.EmerDepEmployee;
import S3.entities.Patient;
import S3.simulation.*;

//meta! id="4"
public class ManagerHospital extends OSPABA.Manager {
    private int remainingInitialMovements;

    public ManagerHospital(int id, Simulation mySim, Agent myAgent) {
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

    //meta! sender="AgentResources", id="71", type="Response"
    public void processAllocateDoctorsExaminationBResources(MessageForm message) {
    }

    //meta! sender="AgentMovement", id="74", type="Response"
    public void processHandlePatientToWaitingRoom(MessageForm message) {
        Message msg = (Message) message.createCopy();

        msg.setCode(Mc.putPatient);
        msg.setAddressee(mySim().findAgent(Id.agentPatients));

        notice(msg);

        Message startMsg = new Message(mySim());
        startMsg.setCode(Mc.tryStartInitialExamination);
        startMsg.setAddressee(myAgent());

        notice(startMsg);
    }

    //meta! sender="AgentMovement", id="72", type="Response"
    public void processHandeDoctorsMovement(MessageForm message) {
        int senderId = message.sender().id();

        if (senderId == Id.agentMovement) {
            response(message);
            return;
        }

        Message req = (Message) message.createCopy();
        req.setCode(Mc.handleDoctorsMovement);
        req.setAddressee(mySim().findAgent(Id.agentMovement));
        request(req);
    }

    //meta! sender="AgentPatients", id="79", type="Response"
    public void processGetPatientForDoctorsExamination(MessageForm message) {
    }

    //meta! sender="ProcessDoctorsExamination", id="42", type="Finish"
    public void processFinishProcessDoctorsExamination(MessageForm message) {
    }

    //meta! sender="ProcessInitialExamintation", id="40", type="Finish"
    public void processFinishProcessInitialExamintation(MessageForm message) {
        Message msg = (Message) message.createCopy();
        Patient patient = msg.getPatient();

        patient.setWaitingForInitialExam(false);
        patient.setWaitingForDoctorsExam(true);

        EmerDepEmployee nurse = msg.getNurse();
        nurse.endWork();

        // Put patient to next queue
        Message putMsg = (Message) msg.createCopy();
        putMsg.setCode(Mc.putPatient);
        putMsg.setAddressee(mySim().findAgent(Id.agentPatients));
        notice(putMsg);

        // Release resources
        msg.setCode(Mc.releaseResources);
        msg.setAddressee(mySim().findAgent(Id.agentResources));
        notice(msg);

        // Try to start another examination
        Message retry = new Message(mySim());
        retry.setCode(Mc.tryStartInitialExamination);
        retry.setAddressee(myAgent());
        notice(retry);
    }

    //meta! sender="AgentResources", id="69", type="Response"
    public void processAllocateInitialExaminationResources(MessageForm message) {
        Message msg = (Message) message;
        if (msg.getNurse() == null || msg.getClinic() == null || msg.getPatient() == null) {
            return;
        }
        msg.setCode(Mc.handleNursesMovement);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);
    }

    //meta! sender="AgentMovement", id="75", type="Response"
    public void processHandleLeavingPatient(MessageForm message) {
        int senderId = message.sender().id();

        if (senderId == Id.agentMovement) {
            Message msg = (Message) message.createCopy();
            msg.setCode(Mc.patientDeparture);
            msg.setAddressee(mySim().findAgent(Id.agentEnvironment));
            notice(msg);
            return;
        }

        Message req = (Message) message.createCopy();
        req.setCode(Mc.handleLeavingPatient);
        req.setAddressee(mySim().findAgent(Id.agentMovement));
        request(req);
    }

    //meta! sender="AgentBoss", id="33", type="Request"
    public void processHandlePatient(MessageForm message) {
        Message msg = (Message) message.createCopy();
        msg.setCode(Mc.handlePatientToWaitingRoom);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);
    }

    //meta! sender="AgentResources", id="70", type="Response"
    public void processAllocateDoctorsExaminationAResources(MessageForm message) {
    }

    //meta! sender="AgentPatients", id="78", type="Response"
    public void processGetPatientForInitialExamination(MessageForm message) {
        Message msg = (Message) message;
        if (msg.getPatient() == null) {
            return;
        }
        msg.setCode(Mc.allocateInitialExaminationResources);
        msg.setAddressee(mySim().findAgent(Id.agentResources));
        request(msg);
    }

    //meta! sender="AgentMovement", id="73", type="Response"
    public void processHandleNursesMovement(MessageForm message) {
        int senderId = message.sender().id();

        // movement dokončený
        if (senderId == Id.agentMovement) {

            Message msg = (Message) message.createCopy();

            msg.setCode(Mc.handlePatientToClinic);
            msg.setAddressee(mySim().findAgent(Id.agentMovement));

            request(msg);

            return;
        }

        // spustenie movementu
        Message req = (Message) message.createCopy();

        req.setCode(Mc.handleNursesMovement);
        req.setAddressee(mySim().findAgent(Id.agentMovement));

        request(req);
    }

    //meta! sender="AgentMovement", id="76", type="Response"
    public void processHandlePatientToClinic(MessageForm message) {
        int senderId = message.sender().id();

        // pacient dorazil do ambulancie
        if (senderId == Id.agentMovement) {
            message.setCode(Mc.start);
            message.setAddressee(myAgent().findAssistant(Id.processInitialExamintation));
            startContinualAssistant(message);
            return;
        }

        Message req = (Message) message.createCopy();
        req.setCode(Mc.handlePatientToClinic);
        req.setAddressee(mySim().findAgent(Id.agentMovement));
        request(req);
    }

    //meta! userInfo="Process messages defined in code", id="0"
    public void processDefault(MessageForm message) {
        switch (message.code()) {
        }
    }

    //meta! sender="AgentMovement", id="109", type="Response"
    public void processHandleInitialPersonalMovement(MessageForm message) {
        int senderId = message.sender().id();

        if (senderId == Id.agentBoss) {
            EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
            AgentResources resources = sim.agentResources();

            remainingInitialMovements = resources.getDoctors().size() + resources.getNurses().size();

            for (EmerDepEmployee employee : resources.getDoctors()) {
                Message req = (Message) message.createCopy();
                req.setCode(Mc.handleInitialPersonalMovement);
                req.setDoctor(employee);
                req.setAddressee(mySim().findAgent(Id.agentMovement));
                request(req);
            }

            for (EmerDepEmployee employee : resources.getNurses()) {
                Message req = (Message) message.createCopy();
                req.setCode(Mc.handleInitialPersonalMovement);
                req.setNurse(employee);
                req.setAddressee(mySim().findAgent(Id.agentMovement));
                request(req);
            }
        }
    }

    private void processTryStartInitialExamination(MessageForm message) {
        Message req = (Message) message.createCopy();
        req.setCode(Mc.getPatientForInitialExamination);
        req.setAddressee(mySim().findAgent(Id.agentPatients));
        request(req);
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    public void init() {
    }

    @Override
    public void processMessage(MessageForm message) {
        switch (message.code()) {
            case Mc.finish:
                switch (message.sender().id()) {
                    case Id.processDoctorsExamination:
                        processFinishProcessDoctorsExamination(message);
                        break;

                    case Id.processInitialExamintation:
                        processFinishProcessInitialExamintation(message);
                        break;
                }
                break;

            case Mc.allocateDoctorsExaminationBResources:
                processAllocateDoctorsExaminationBResources(message);
                break;

            case Mc.handleLeavingPatient:
                processHandleLeavingPatient(message);
                break;

            case Mc.allocateInitialExaminationResources:
                processAllocateInitialExaminationResources(message);
                break;

            case Mc.handleInitialPersonalMovement:
                processHandleInitialPersonalMovement(message);
                break;

            case Mc.allocateDoctorsExaminationAResources:
                processAllocateDoctorsExaminationAResources(message);
                break;

            case Mc.getPatientForDoctorsExamination:
                processGetPatientForDoctorsExamination(message);
                break;

            case Mc.getPatientForInitialExamination:
                processGetPatientForInitialExamination(message);
                break;

            case Mc.handleNursesMovement:
                processHandleNursesMovement(message);
                break;

            case Mc.handlePatientToWaitingRoom:
                processHandlePatientToWaitingRoom(message);
                break;

            case Mc.handlePatient:
                processHandlePatient(message);
                break;

            case Mc.handlePatientToClinic:
                processHandlePatientToClinic(message);
                break;

            case Mc.handleDoctorsMovement:
                processHandeDoctorsMovement(message);
                break;

            case Mc.tryStartInitialExamination:
                processTryStartInitialExamination(message);
                break;

            default:
                processDefault(message);
                break;
        }
    }
    //meta! tag="end"

    @Override
    public AgentHospital myAgent() {
        return (AgentHospital) super.myAgent();
    }

}