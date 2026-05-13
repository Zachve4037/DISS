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
        Message msg = (Message) message;
        if (msg.getNurse() == null || msg.getClinic() == null || msg.getPatient() == null || msg.getDoctor() == null) {
            requeuePatient(msg);
            return;
        }
        msg.setCode(Mc.handleNursesMovement);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);
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
        Message msg = (Message) message;
        Patient p = msg.getPatient();
        if (p == null) return;

        // Decide which clinic type to try (A, B, or both)
        if (p.getPriority() == 1 || p.getPriority() == 2) {
            msg.setCode(Mc.allocateDoctorsExaminationAResources);
            msg.setAddressee(mySim().findAgent(Id.agentResources));
            request(msg);
        } else if (p.getPriority() == 5) {
            msg.setCode(Mc.allocateDoctorsExaminationBResources);
            msg.setAddressee(mySim().findAgent(Id.agentResources));
            request(msg);
        } else {
            msg.setCode(Mc.allocateDoctorsExaminationAResources);
            msg.setAddressee(mySim().findAgent(Id.agentResources));
            request(msg);
            // If A fails, in the response handler, try B
        }
    }

    //meta! sender="ProcessDoctorsExamination", id="42", type="Finish"
    public void processFinishProcessDoctorsExamination(MessageForm message) {
        Message msg = (Message) message.createCopy();

        Patient patient = msg.getPatient();

        if (patient == null) {
            return;
        }

        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();

        // trvanie lekárskeho vyšetrenia
        sim.recordDoctorExamDuration(patient, msg.getProcessDuration());

        // pacient už nečaká na lekárske vyšetrenie
        patient.setWaitingForDoctorsExam(false);

        // ukončenie práce doktora
        EmerDepEmployee doctor = msg.getDoctor();
        if (doctor != null) {
            doctor.endWork();
        }

        // ukončenie práce sestry
        EmerDepEmployee nurse = msg.getNurse();
        if (nurse != null) {
            nurse.endWork();
        }

        // uvoľnenie ambulancie
        Message releaseMsg = (Message) msg.createCopy();
        releaseMsg.setCode(Mc.releaseResources);
        releaseMsg.setAddressee(mySim().findAgent(Id.agentResources));
        notice(releaseMsg);

        // pacient musí fyzicky odísť zo systému cez AgentMovement
        Message leavingMsg = (Message) msg.createCopy();
        leavingMsg.setCode(Mc.handleLeavingPatient);
        leavingMsg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(leavingMsg);

        // po uvoľnení zdrojov skús rozbehnúť ďalšie lekárske vyšetrenie
        Message retryDoctors = new Message(mySim());
        retryDoctors.setCode(Mc.tryStartDoctorsExamination);
        retryDoctors.setAddressee(myAgent());
        notice(retryDoctors);

        // ak už nečaká nikto na lekára / alebo ostali voľné zdroje,
        // môžeš skúsiť aj vstupné vyšetrenie
        Message retryInitial = new Message(mySim());
        retryInitial.setCode(Mc.tryStartInitialExamination);
        retryInitial.setAddressee(myAgent());
        notice(retryInitial);
    }

    //meta! sender="ProcessInitialExamintation", id="40", type="Finish"
    public void processFinishProcessInitialExamintation(MessageForm message) {
        Message msg = (Message) message.createCopy();
        Patient patient = msg.getPatient();

        if (patient == null) {
            return;
        }

        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        sim.recordInitialExamDuration(patient, msg.getProcessDuration());

        patient.setWaitingForInitialExam(false);
        patient.setWaitingForDoctorsExam(true);

        EmerDepEmployee nurse = msg.getNurse();
        if (nurse != null) {
            nurse.endWork();
        }

        Message releaseMsg = (Message) msg.createCopy();
        releaseMsg.setCode(Mc.releaseResources);
        releaseMsg.setAddressee(mySim().findAgent(Id.agentResources));
        notice(releaseMsg);

        Message putMsg = (Message) msg.createCopy();
        putMsg.setCode(Mc.putPatient);
        putMsg.setAddressee(mySim().findAgent(Id.agentPatients));
        notice(putMsg);

        Message doctorExamMsg = new Message(mySim());
        doctorExamMsg.setCode(Mc.tryStartDoctorsExamination);
        doctorExamMsg.setAddressee(myAgent());
        notice(doctorExamMsg);

        Message retry = new Message(mySim());
        retry.setCode(Mc.tryStartInitialExamination);
        retry.setAddressee(myAgent());
        notice(retry);
    }

    //meta! sender="AgentResources", id="69", type="Response"
    public void processAllocateInitialExaminationResources(MessageForm message) {
        Message msg = (Message) message;
        if (msg.getNurse() == null || msg.getClinic() == null || msg.getPatient() == null) {
            requeuePatient(msg);
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
        Message msg = (Message) message;
        if (msg.getNurse() == null || msg.getClinic() == null || msg.getPatient() == null || msg.getDoctor() == null) {
            Patient patient = msg.getPatient();
            if (patient != null && (patient.getPriority() == 3 || patient.getPriority() == 4)) {
                msg.setCode(Mc.allocateDoctorsExaminationBResources);
                msg.setAddressee(mySim().findAgent(Id.agentResources));
                request(msg);
            } else {
                requeuePatient(msg);
            }
            return;
        }
        msg.setCode(Mc.handleNursesMovement);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);
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
            Patient patient = msg.getPatient();
            patient.endWaiting();
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

        if (senderId == Id.agentMovement) {
            Message msg = (Message) message.createCopy();
            Patient patient = msg.getPatient();

            if (patient == null) {
                return;
            }

            if (patient.isWaitingForInitialExam()) {
                msg.setCode(Mc.start);
                msg.setAddressee(myAgent().findAssistant(Id.processInitialExamintation));
                startContinualAssistant(msg);
                return;
            }

            if (patient.isWaitingForDoctorsExam()) {
                msg.setCode(Mc.start);
                msg.setAddressee(myAgent().findAssistant(Id.processDoctorsExamination));
                startContinualAssistant(msg);
                return;
            }

            return;
        }

        Message req = (Message) message.createCopy();
        req.setCode(Mc.handlePatientToClinic);
        req.setAddressee(mySim().findAgent(Id.agentMovement));
        request(req);
    }

    private void requeuePatient(Message msg) {
        if (msg == null || msg.getPatient() == null) {
            return;
        }

        Message putBack = (Message) msg.createCopy();
        putBack.setDoctor(null);
        putBack.setNurse(null);
        putBack.setClinic(null);
        putBack.setCode(Mc.putPatient);
        putBack.setAddressee(mySim().findAgent(Id.agentPatients));
        notice(putBack);
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
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();

        if (!sim.agentPatients().getDoctorsExaminationQueue().isEmpty()) {
            return;
        }

        Message req = (Message) message.createCopy();
        req.setCode(Mc.getPatientForInitialExamination);
        req.setAddressee(mySim().findAgent(Id.agentPatients));
        request(req);
    }

    private void processTryStartDoctorsExamination(MessageForm message) {
        Message req = (Message) message.createCopy();
        req.setCode(Mc.getPatientForDoctorsExamination);
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

            case Mc.tryStartDoctorsExamination:
                processTryStartDoctorsExamination(message);
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