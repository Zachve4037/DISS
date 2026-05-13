package S3.agents.agenthospital;

import OSPABA.*;
import S3.agents.agentresources.AgentResources;
import S3.entities.Clinic;
import S3.entities.ClinicType;
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

        if (petriNet() != null) {
            petriNet().clear();
        }
    }

    //meta! sender="AgentResources", id="71", type="Response"
    public void processAllocateDoctorsExaminationBResources(MessageForm message) {
        startDoctorExaminationIfResourcesAllocated((Message) message);
    }

    //meta! sender="AgentMovement", id="74", type="Response"
    public void processHandlePatientToWaitingRoom(MessageForm message) {
        Message msg = (Message) message.createCopy();
        msg.setCode(Mc.putPatient);
        msg.setAddressee(mySim().findAgent(Id.agentPatients));
        notice(msg);

        scheduleTryDoctors();
        scheduleTryInitial();
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

        if (msg.getPatient() == null || msg.getRequestedClinicType() == null) {
            scheduleTryInitial();
            return;
        }

        msg.setCode(msg.getRequestedClinicType() == ClinicType.A
                ? Mc.allocateDoctorsExaminationAResources
                : Mc.allocateDoctorsExaminationBResources);
        msg.setAddressee(mySim().findAgent(Id.agentResources));
        request(msg);
    }

    //meta! sender="ProcessDoctorsExamination", id="42", type="Finish"
    public void processFinishProcessDoctorsExamination(MessageForm message) {
        Message msg = (Message) message.createCopy();
        Patient patient = msg.getPatient();

        if (patient == null) {
            return;
        }

        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();

        patient.setWaitingForDoctorsExam(false);

        EmerDepEmployee doctor = msg.getDoctor();
        if (doctor != null) {
            doctor.endWork();
        }

        EmerDepEmployee nurse = msg.getNurse();
        if (nurse != null) {
            nurse.endWork();
        }

        releaseClinicNow(msg);
        Message releaseMsg = (Message) msg.createCopy();
        releaseMsg.setCode(Mc.releaseResources);
        releaseMsg.setAddressee(mySim().findAgent(Id.agentResources));
        notice(releaseMsg);

        // After doctor examination the patient still has to leave the department physically.
        Message leavingMsg = (Message) msg.createCopy();
        leavingMsg.setCode(Mc.handleLeavingPatient);
        leavingMsg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(leavingMsg);

        scheduleTryDoctors();
        scheduleTryInitial();
    }

    //meta! sender="ProcessInitialExamintation", id="40", type="Finish"
    public void processFinishProcessInitialExamintation(MessageForm message) {
        Message msg = (Message) message.createCopy();
        Patient patient = msg.getPatient();

        if (patient == null) {
            return;
        }

        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();

        patient.setWaitingForInitialExam(false);
        patient.setWaitingForDoctorsExam(true);

        EmerDepEmployee nurse = msg.getNurse();
        if (nurse != null) {
            nurse.endWork();
        }

        releaseClinicNow(msg);
        Message releaseMsg = (Message) msg.createCopy();
        releaseMsg.setCode(Mc.releaseResources);
        releaseMsg.setAddressee(mySim().findAgent(Id.agentResources));
        notice(releaseMsg);

        Message putMsg = (Message) msg.createCopy();
        putMsg.setDoctor(null);
        putMsg.setNurse(null);
        putMsg.setClinic(null);
        putMsg.setRequestedClinicType(null);
        putMsg.setCode(Mc.putPatient);
        putMsg.setAddressee(mySim().findAgent(Id.agentPatients));
        notice(putMsg);

        scheduleTryDoctors();
        scheduleTryInitial();
    }

    //meta! sender="AgentResources", id="69", type="Response"
    public void processAllocateInitialExaminationResources(MessageForm message) {
        Message msg = (Message) message;

        if (msg.getNurse() == null || msg.getClinic() == null || msg.getPatient() == null) {
            requeuePatient(msg);
            scheduleTryDoctors();
            return;
        }

        // Waiting ends when the patient is selected and starts moving to the clinic.
        msg.getPatient().endWaiting();

        msg.setCode(Mc.handlePatientToClinic);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);

        scheduleTryDoctors();
        scheduleTryInitial();
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
        startDoctorExaminationIfResourcesAllocated((Message) message);
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

        if (senderId == Id.agentMovement) {
            Message msg = (Message) message.createCopy();
            if (msg.getPatient() != null) {
                msg.getPatient().endWaiting();
            }
            msg.setCode(Mc.handlePatientToClinic);
            msg.setAddressee(mySim().findAgent(Id.agentMovement));
            request(msg);
            return;
        }

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
            }
            return;
        }

        Message req = (Message) message.createCopy();
        req.setCode(Mc.handlePatientToClinic);
        req.setAddressee(mySim().findAgent(Id.agentMovement));
        request(req);
    }

    private void startDoctorExaminationIfResourcesAllocated(Message msg) {
        if (msg.getNurse() == null || msg.getClinic() == null || msg.getPatient() == null || msg.getDoctor() == null) {
            requeuePatient(msg);
            scheduleTryInitial();
            return;
        }

        // Waiting ends as soon as the patient is selected and starts moving to the clinic.
        msg.getPatient().endWaiting();

        msg.setCode(Mc.handlePatientToClinic);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);

        scheduleTryDoctors();
        scheduleTryInitial();
    }

    private void requeuePatient(Message msg) {
        if (msg == null || msg.getPatient() == null) {
            return;
        }

        Message putBack = (Message) msg.createCopy();
        putBack.setDoctor(null);
        putBack.setNurse(null);
        putBack.setClinic(null);
        putBack.setRequestedClinicType(null);
        putBack.setCode(Mc.putPatient);
        putBack.setAddressee(mySim().findAgent(Id.agentPatients));
        notice(putBack);
    }

    private void releaseClinicNow(Message msg) {
        if (msg != null && msg.getClinic() != null) {
            msg.getClinic().setOccupied(false);
        }
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
            return;
        }

        if (senderId == Id.agentMovement) {
            remainingInitialMovements--;
            if (remainingInitialMovements <= 0) {
                scheduleTryDoctors();
                scheduleTryInitial();
            }
        }
    }

    private void processTryStartInitialExamination(MessageForm message) {
        // Default policy: doctor examination has priority over initial examination,
        // but only when a doctor examination can really start now.
        if (canTryDoctorBranch(ClinicType.A) || canTryDoctorBranch(ClinicType.B)) {
            scheduleTryDoctors();
            return;
        }

        Message req = (Message) message.createCopy();
        req.setCode(Mc.getPatientForInitialExamination);
        req.setAddressee(mySim().findAgent(Id.agentPatients));
        request(req);
    }

    private void processTryStartDoctorsExamination(MessageForm message) {
        int freePairs = freeDoctorNursePairs();

        if (freePairs > 0 && canTryDoctorBranch(ClinicType.A)) {
            Message reqA = new Message(mySim());
            reqA.setRequestedClinicType(ClinicType.A);
            reqA.setCode(Mc.getPatientForDoctorsExamination);
            reqA.setAddressee(mySim().findAgent(Id.agentPatients));
            request(reqA);
            freePairs--;
        }

        if (freePairs > 0 && canTryDoctorBranch(ClinicType.B)) {
            Message reqB = new Message(mySim());
            reqB.setRequestedClinicType(ClinicType.B);
            reqB.setCode(Mc.getPatientForDoctorsExamination);
            reqB.setAddressee(mySim().findAgent(Id.agentPatients));
            request(reqB);
        }
    }

    private boolean canTryDoctorBranch(ClinicType clinicType) {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        return freeDoctorNursePairs() > 0
                && hasFreeClinic(clinicType)
                && sim.agentPatients().hasDoctorPatientFor(clinicType);
    }

    private int freeDoctorNursePairs() {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        AgentResources resources = sim.agentResources();

        long freeDoctors = resources.getDoctors().stream().filter(d -> !d.isBusy()).count();
        long freeNurses = resources.getNurses().stream().filter(n -> !n.isBusy()).count();
        return (int) Math.min(freeDoctors, freeNurses);
    }

    private boolean hasFreeClinic(ClinicType clinicType) {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        return sim.agentResources().getClinics().stream()
                .anyMatch(c -> !c.isOccupied() && c.getType() == clinicType);
    }

    private void scheduleTryDoctors() {
        Message retry = new Message(mySim());
        retry.setCode(Mc.tryStartDoctorsExamination);
        retry.setAddressee(myAgent());
        notice(retry);
    }

    private void scheduleTryInitial() {
        Message retry = new Message(mySim());
        retry.setCode(Mc.tryStartInitialExamination);
        retry.setAddressee(myAgent());
        notice(retry);
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
