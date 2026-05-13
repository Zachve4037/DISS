package S3.agents.agentresources;

import OSPABA.*;
import S3.entities.Clinic;
import S3.entities.ClinicType;
import S3.entities.EmerDepEmployee;
import S3.entities.Patient;
import S3.simulation.*;

//meta! id="61"
public class ManagerResources extends OSPABA.Manager {
    public ManagerResources(int id, Simulation mySim, Agent myAgent) {
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

    //meta! sender="AgentHospital", id="71", type="Request"
    public void processAllocateDoctorsExaminationBResources(MessageForm message) {
        Message msg = (Message) message.createCopy();
        EmerDepEmployee doctor = myAgent().getDoctors().stream().filter(
                d -> !d.isBusy()
        ).findFirst().orElse(null);
        EmerDepEmployee nurse = myAgent().getNurses().stream().filter(
                n -> !n.isBusy()
        ).findFirst().orElse(null);
        Clinic clinic = findClinicForDoctorExamination(msg.getPatient(), ClinicType.B);
        setResponseMessage(msg, doctor, nurse, clinic);
    }

    //meta! sender="AgentHospital", id="69", type="Request"
    public void processAllocateInitialExaminationResources(MessageForm message) {
        Message msg = (Message) message.createCopy();
        EmerDepEmployee nurse = myAgent().getNurses().stream().filter(
                n -> !n.isBusy()
        ).findFirst().orElse(null);
        Clinic clinic = myAgent().getClinics().stream().filter(
                c -> !c.isOccupied() && c.getType() == ClinicType.B
        ).findFirst().orElse(null);
        if (nurse == null || clinic == null) {
            msg.setNurse(null);
            msg.setClinic(null);
        } else {
            nurse.setBusy(true);
            msg.setNurse(nurse);
            clinic.setOccupied(true);
            msg.setClinic(clinic);
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeReserved(nurse, clinic);
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().clinicOccupied(clinic, true);
        }
        response(msg);
    }

    //meta! sender="AgentHospital", id="70", type="Request"
    public void processAllocateDoctorsExaminationAResources(MessageForm message) {
        Message msg = (Message) message.createCopy();
        EmerDepEmployee doctor = myAgent().getDoctors().stream().filter(
                d -> !d.isBusy()
        ).findFirst().orElse(null);
        EmerDepEmployee nurse = myAgent().getNurses().stream().filter(
                n -> !n.isBusy()
        ).findFirst().orElse(null);
        Clinic clinic = findClinicForDoctorExamination(msg.getPatient(), ClinicType.A);
        setResponseMessage(msg, doctor, nurse, clinic);
    }

    public void processAllocateDoctorsExaminationResources(MessageForm message) {
        Message msg = (Message) message.createCopy();
        EmerDepEmployee doctor = myAgent().getDoctors().stream().filter(
                d -> !d.isBusy()
        ).findFirst().orElse(null);
        EmerDepEmployee nurse = myAgent().getNurses().stream().filter(
                n -> !n.isBusy()
        ).findFirst().orElse(null);
        Clinic clinic = findClinicForDoctorExamination(msg.getPatient());
        setResponseMessage(msg, doctor, nurse, clinic);
    }

    private Clinic findClinicForDoctorExamination(Patient patient, ClinicType requestedType) {
        if (patient == null || requestedType == null) {
            return null;
        }

        Clinic lastClinic = patient.getLastClinic();
        if (lastClinic != null
                && !lastClinic.isOccupied()
                && lastClinic.getType() == requestedType
                && canUseClinic(patient, requestedType)) {
            return lastClinic;
        }

        return firstFreeClinic(requestedType);
    }

    private Clinic findClinicForDoctorExamination(Patient patient) {
        if (patient == null) {
            return null;
        }

        Clinic lastClinic = patient.getLastClinic();
        if (lastClinic != null && !lastClinic.isOccupied() && canUseClinic(patient, lastClinic.getType())) {
            return lastClinic;
        }

        if (patient.getPriority() <= 2) {
            return firstFreeClinic(ClinicType.A);
        }

        if (patient.getPriority() == 5) {
            return firstFreeClinic(ClinicType.B);
        }

        Clinic clinicA = firstFreeClinic(ClinicType.A);
        return clinicA != null ? clinicA : firstFreeClinic(ClinicType.B);
    }

    private Clinic firstFreeClinic(ClinicType clinicType) {
        return myAgent().getClinics().stream().filter(
                c -> !c.isOccupied() && c.getType() == clinicType
        ).findFirst().orElse(null);
    }

    private boolean canUseClinic(Patient patient, ClinicType clinicType) {
        if (patient == null || clinicType == null) {
            return false;
        }

        int priority = patient.getPriority();
        if (clinicType == ClinicType.A) {
            return priority >= 1 && priority <= 4;
        }
        return priority >= 3 && priority <= 5;
    }

    private void setResponseMessage(Message msg, EmerDepEmployee doctor, EmerDepEmployee nurse, Clinic clinic) {
        if (doctor == null || nurse == null || clinic == null) {
            msg.setDoctor(null);
            msg.setNurse(null);
            msg.setClinic(null);
        } else {
            msg.setDoctor(doctor);
            msg.setNurse(nurse);
            msg.setClinic(clinic);
            doctor.setBusy(true);
            nurse.setBusy(true);
            clinic.setOccupied(true);
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeReserved(doctor, clinic);
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeReserved(nurse, clinic);
            ((EmergencyDepartmentSimulation) mySim()).edAnimator().clinicOccupied(clinic, true);
        }
        response(msg);
    }

    public void processReleaseResources(MessageForm message) {
        Message msg = (Message) message;

        Clinic c = msg.getClinic();
        EmerDepEmployee d = msg.getDoctor();
        EmerDepEmployee n = msg.getNurse();

        if (c != null) {
            int id = c.getId();
            if (id >= 0 && id < myAgent().getClinics().size()) {
                myAgent().getClinics().get(id).setOccupied(false);
                ((EmergencyDepartmentSimulation) mySim()).edAnimator().clinicOccupied(myAgent().getClinics().get(id), false);
            } else {
                System.err.println("[ManagerResources] Warning: clinic id out of bounds: " + id);
            }
        }
        if (d != null) {
            int id = d.getId();
            if (id >= 0 && id < myAgent().getDoctors().size()) {
                myAgent().getDoctors().get(id).setBusy(false);
                ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeIdle(myAgent().getDoctors().get(id));
            } else {
                System.err.println("[ManagerResources] Warning: doctor id out of bounds: " + id);
            }
        }
        if (n != null) {
            int id = n.getId();
            if (id >= 0 && id < myAgent().getNurses().size()) {
                myAgent().getNurses().get(id).setBusy(false);
                ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeIdle(myAgent().getNurses().get(id));
            } else {
                System.err.println("[ManagerResources] Warning: nurse id out of bounds: " + id);
            }
        }
    }

    //meta! userInfo="Process messages defined in code", id="0"
    public void processDefault(MessageForm message) {
        switch (message.code()) {
        }
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    public void init() {
    }

    @Override
    public void processMessage(MessageForm message) {
        switch (message.code()) {
            case Mc.allocateDoctorsExaminationBResources:
                processAllocateDoctorsExaminationBResources(message);
                break;

            case Mc.allocateInitialExaminationResources:
                processAllocateInitialExaminationResources(message);
                break;

            case Mc.allocateDoctorsExaminationAResources:
                processAllocateDoctorsExaminationAResources(message);
                break;

            case Mc.allocateDoctorsExaminationResources:
                processAllocateDoctorsExaminationResources(message);
                break;

            case Mc.releaseResources:
                processReleaseResources(message);
                break;

            default:
                processDefault(message);
                break;
        }
    }
    //meta! tag="end"

    @Override
    public AgentResources myAgent() {
        return (AgentResources) super.myAgent();
    }

}
