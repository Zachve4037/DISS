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
            scheduleTryInitial();
            return;
        }

        startPatientMovementToClinic(msg);

        // Skús hneď obsadiť aj ďalšie voľné zdroje.
        scheduleTryDoctors();
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

        if (p == null) {
            // Pre daný typ ambulancie nie je vhodný pacient. Voľné zdroje môžu ísť na vstupné vyšetrenie.
            scheduleTryInitial();
            return;
        }

        ClinicType requestedType = msg.getRequestedClinicType();

        if (requestedType == ClinicType.B) {
            msg.setCode(Mc.allocateDoctorsExaminationBResources);
        } else {
            // Defaultne A. Pre priority 1/2 je to jediná možnosť; pre 3/4 je to A-vetva.
            msg.setCode(Mc.allocateDoctorsExaminationAResources);
        }

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

        // uvoľnenie ambulancie musí prebehnúť pred retry správami, inak retry často vidí kliniku ešte obsadenú.
        releaseResourcesNow(msg);

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

        patient.setWaitingForInitialExam(false);
        patient.setWaitingForDoctorsExam(true);

        EmerDepEmployee nurse = msg.getNurse();
        if (nurse != null) {
            nurse.endWork();
        }

        releaseResourcesNow(msg);

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

        startPatientMovementToClinic(msg);

        // Toto je dôležité: jeden trigger nesmie spustiť iba jedno vstupné vyšetrenie,
        // inak pri veľkom rade ostanú voľné B ambulancie/sestry nevyužité.
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
        Message msg = (Message) message;
        if (msg.getNurse() == null || msg.getClinic() == null || msg.getPatient() == null || msg.getDoctor() == null) {
            requeuePatient(msg);
            scheduleTryInitial();
            return;
        }

        startPatientMovementToClinic(msg);

        // Skús hneď obsadiť aj ďalšie voľné zdroje.
        scheduleTryDoctors();
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


    private void releaseResourcesNow(Message msg) {
        if (msg == null) {
            return;
        }

        Clinic clinic = msg.getClinic();
        if (clinic != null) {
            clinic.setOccupied(false);
        }
    }

    private void scheduleTryDoctors() {
        Message retryDoctors = new Message(mySim());
        retryDoctors.setCode(Mc.tryStartDoctorsExamination);
        retryDoctors.setAddressee(myAgent());
        notice(retryDoctors);
    }

    private void scheduleTryInitial() {
        Message retryInitial = new Message(mySim());
        retryInitial.setCode(Mc.tryStartInitialExamination);
        retryInitial.setAddressee(myAgent());
        notice(retryInitial);
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
                Message tryDoctors = new Message(mySim());
                tryDoctors.setCode(Mc.tryStartDoctorsExamination);
                tryDoctors.setAddressee(myAgent());
                notice(tryDoctors);

                Message tryInitial = new Message(mySim());
                tryInitial.setCode(Mc.tryStartInitialExamination);
                tryInitial.setAddressee(myAgent());
                notice(tryInitial);
            }
        }
    }


    private void startPatientMovementToClinic(Message msg) {
        Patient patient = msg.getPatient();
        if (patient != null) {
            // Čakanie končí v momente, keď je pacient vybraný zo zoznamu,
            // má pridelené zdroje a začína jeho presun do ambulancie.
            patient.endWaiting();
        }

        msg.setCode(Mc.handlePatientToClinic);
        msg.setAddressee(mySim().findAgent(Id.agentMovement));
        request(msg);
    }

    private boolean canStartDoctorExaminationNow() {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();

        boolean freeDoctor = sim.agentResources().getDoctors().stream().anyMatch(d -> !d.isBusy());
        boolean freeNurse = sim.agentResources().getNurses().stream().anyMatch(n -> !n.isBusy());
        if (!freeDoctor || !freeNurse) {
            return false;
        }

        boolean freeA = sim.agentResources().getClinics().stream()
                .anyMatch(c -> !c.isOccupied() && c.getType() == ClinicType.A);
        boolean freeB = sim.agentResources().getClinics().stream()
                .anyMatch(c -> !c.isOccupied() && c.getType() == ClinicType.B);
        if (!freeA && !freeB) {
            return false;
        }

        return sim.agentPatients().getDoctorsExaminationQueues().stream().anyMatch(patient -> {
            int priority = patient.getPriority();
            if (priority <= 2) {
                return freeA;
            }
            if (priority == 5) {
                return freeB;
            }
            return freeA || freeB;
        });
    }

    private void processTryStartInitialExamination(MessageForm message) {
        // Default stratégia zo zadania/poznámok: lekárske vyšetrenie má prednosť
        // pred vstupným vyšetrením. Neznamená to ale tvrdé blokovanie evidencie vždy,
        // keď niekto čaká na lekára. Blokujeme ju iba vtedy, keď sa lekárske vyšetrenie
        // vie spustiť hneď teraz s aktuálne voľnými zdrojmi.
        if (canStartDoctorExaminationNow()) {
            scheduleTryDoctors();
            return;
        }

        Message req = (Message) message.createCopy();
        req.setCode(Mc.getPatientForInitialExamination);
        req.setAddressee(mySim().findAgent(Id.agentPatients));
        request(req);
    }

    private void processTryStartDoctorsExamination(MessageForm message) {
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();

        if (sim.agentPatients().getDoctorsExaminationQueue().isEmpty()) {
            scheduleTryInitial();
            return;
        }

        // Pacient sa stále vyberá pred zdrojmi, ale osobitne pre A a B vetvu.
        // Tak sa nestane, že pacient priority 1/2 zablokuje voľné B ambulancie.
        Message reqA = (Message) message.createCopy();
        reqA.setRequestedClinicType(ClinicType.A);
        reqA.setCode(Mc.getPatientForDoctorsExamination);
        reqA.setAddressee(mySim().findAgent(Id.agentPatients));
        request(reqA);

        Message reqB = (Message) message.createCopy();
        reqB.setRequestedClinicType(ClinicType.B);
        reqB.setCode(Mc.getPatientForDoctorsExamination);
        reqB.setAddressee(mySim().findAgent(Id.agentPatients));
        request(reqB);
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