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
    private boolean dispatchQueued;
    private int doctorPatientRequestsPending;
    private boolean initialPatientRequestPending;
    private int doctorResourceRequestsPending;
    private int initialResourceRequestsPending;

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

        remainingInitialMovements = 0;
        dispatchQueued = false;
        doctorPatientRequestsPending = 0;
        initialPatientRequestPending = false;
        doctorResourceRequestsPending = 0;
        initialResourceRequestsPending = 0;
    }

    //meta! sender="AgentResources", id="71", type="Response"
    public void processAllocateDoctorsExaminationBResources(MessageForm message) {
        if (doctorResourceRequestsPending > 0) {
            doctorResourceRequestsPending--;
        }
        startDoctorExaminationIfResourcesAllocated((Message) message);
    }

    public void processAllocateDoctorsExaminationResources(MessageForm message) {
        if (doctorResourceRequestsPending > 0) {
            doctorResourceRequestsPending--;
        }
        startDoctorExaminationIfResourcesAllocated((Message) message);
    }

    //meta! sender="AgentMovement", id="74", type="Response"
    public void processHandlePatientToWaitingRoom(MessageForm message) {
        Message msg = (Message) message.createCopy();
        msg.setCode(Mc.putPatient);
        msg.setAddressee(mySim().findAgent(Id.agentPatients));
        notice(msg);

        scheduleDispatch();
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

        if (doctorPatientRequestsPending > 0) {
            doctorPatientRequestsPending--;
        }

        if (msg.getPatient() == null) {
            scheduleDispatch();
            return;
        }

        doctorResourceRequestsPending++;
        msg.setCode(msg.getRequestedClinicType() == ClinicType.A
                ? Mc.allocateDoctorsExaminationAResources
                : msg.getRequestedClinicType() == ClinicType.B
                ? Mc.allocateDoctorsExaminationBResources
                : Mc.allocateDoctorsExaminationResources);
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
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeIdle(doctor);
        }

        EmerDepEmployee nurse = msg.getNurse();
        if (nurse != null) {
            nurse.endWork();
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeIdle(nurse);
        }

        releaseClinicNow(msg);

        // After doctor examination the patient still has to leave the department physically.
        Message leavingMsg = (Message) msg.createCopy();
        leavingMsg.setCode(Mc.handleLeavingPatient);
        leavingMsg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(leavingMsg);

        scheduleDispatch();
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
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeIdle(nurse);
        }

        releaseClinicNow(msg);

        Message putMsg = (Message) msg.createCopy();
        putMsg.setDoctor(null);
        putMsg.setNurse(null);
        putMsg.setClinic(null);
        putMsg.setRequestedClinicType(null);
        putMsg.setCode(Mc.putPatient);
        putMsg.setAddressee(mySim().findAgent(Id.agentPatients));
        notice(putMsg);

        scheduleDispatch();
    }

    //meta! sender="AgentResources", id="69", type="Response"
    public void processAllocateInitialExaminationResources(MessageForm message) {
        Message msg = (Message) message;
        if (initialResourceRequestsPending > 0) {
            initialResourceRequestsPending--;
        }

        if (msg.getNurse() == null || msg.getClinic() == null || msg.getPatient() == null) {
            requeuePatient(msg);
            return;
        }

        // Waiting ends when the patient is selected and starts moving to the clinic.
        msg.getPatient().endWaiting();

        msg.setCode(Mc.handlePatientToClinic);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);

        scheduleDispatch();
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
        if (doctorResourceRequestsPending > 0) {
            doctorResourceRequestsPending--;
        }
        startDoctorExaminationIfResourcesAllocated((Message) message);
    }

    //meta! sender="AgentPatients", id="78", type="Response"
    public void processGetPatientForInitialExamination(MessageForm message) {
        Message msg = (Message) message;
        initialPatientRequestPending = false;
        if (msg.getPatient() == null) {
            scheduleDispatch();
            return;
        }

        initialResourceRequestsPending++;
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
            return;
        }

        // Waiting ends as soon as the patient is selected and starts moving to the clinic.
        msg.getPatient().endWaiting();

        msg.setCode(Mc.handlePatientToClinic);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);

        scheduleDispatch();
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
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().clinicOccupied(msg.getClinic(), false);
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
                scheduleDispatch();
            }
        }
    }

    private void processTryStartInitialExamination(MessageForm message) {
        processDispatch();
    }

    private void processTryStartDoctorsExamination(MessageForm message) {
        processDispatch();
    }

    private void processDispatch() {
        dispatchQueued = false;

        if (hasPendingStartAttempt()) {
            return;
        }

        int freePairs = freeDoctorNursePairs();
        boolean startedDoctor = false;
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        boolean hasAOnlyPatient = sim.agentPatients().hasDoctorPatientOnlyForA();

        if (freePairs > 0 && hasAOnlyPatient && canTryDoctorBranch(ClinicType.A)) {
            requestDoctorPatientFor(ClinicType.A);
            freePairs--;
            startedDoctor = true;
        }

        if (freePairs > 0 && canTryDoctorBranch(ClinicType.B)) {
            requestDoctorPatientFor(ClinicType.B);
            freePairs--;
            startedDoctor = true;
        }

        if (freePairs > 0 && canTryDoctorBranch(ClinicType.A)) {
            requestDoctorPatientFor(ClinicType.A);
            startedDoctor = true;
        }

        if (startedDoctor) {
            return;
        }

        if (canStartInitialPatientNow()) {
            initialPatientRequestPending = true;
            Message req = new Message(mySim());
            req.setCode(Mc.getPatientForInitialExamination);
            req.setAddressee(mySim().findAgent(Id.agentPatients));
            request(req);
        }
    }

    private void requestDoctorPatientFor(ClinicType clinicType) {
        doctorPatientRequestsPending++;
        Message req = new Message(mySim());
        req.setRequestedClinicType(clinicType);
        req.setCode(Mc.getPatientForDoctorsExamination);
        req.setAddressee(mySim().findAgent(Id.agentPatients));
        request(req);
    }

    private boolean hasPendingStartAttempt() {
        return doctorPatientRequestsPending > 0
                || initialPatientRequestPending
                || doctorResourceRequestsPending > 0
                || initialResourceRequestsPending > 0;
    }

    private boolean canTryDoctorBranch(ClinicType clinicType) {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        return freeDoctorNursePairs() > 0
                && hasFreeClinic(clinicType)
                && sim.agentPatients().hasDoctorPatientFor(clinicType);
    }

    private boolean canStartInitialPatientNow() {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        return sim.agentPatients().hasInitialPatients()
                && hasFreeNurse()
                && hasFreeClinic(ClinicType.B);
    }

    private int freeDoctorNursePairs() {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        AgentResources resources = sim.agentResources();

        long freeDoctors = resources.getDoctors().stream().filter(d -> !d.isBusy()).count();
        long freeNurses = resources.getNurses().stream().filter(n -> !n.isBusy()).count();
        return (int) Math.min(freeDoctors, freeNurses);
    }

    private boolean hasFreeNurse() {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        return sim.agentResources().getNurses().stream().anyMatch(n -> !n.isBusy());
    }

    private boolean hasFreeClinic(ClinicType clinicType) {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();
        return sim.agentResources().getClinics().stream()
                .anyMatch(c -> !c.isOccupied() && c.getType() == clinicType);
    }

    private void scheduleDispatch() {
        if (dispatchQueued) {
            return;
        }
        dispatchQueued = true;
        Message retry = new Message(mySim());
        retry.setCode(Mc.tryStartDoctorsExamination);
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

            case Mc.allocateDoctorsExaminationResources:
                processAllocateDoctorsExaminationResources(message);
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
