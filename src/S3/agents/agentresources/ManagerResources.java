package S3.agents.agentresources;

import OSPABA.*;
import S3.entities.Clinic;
import S3.entities.ClinicType;
import S3.entities.EmerDepEmployee;
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
        Clinic clinic = myAgent().getClinics().stream().filter(
                c -> !c.isOccupied() && c.getType() == ClinicType.B
        ).findFirst().orElse(null);
        setResponseMessage(message, msg, doctor, nurse, clinic);
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
        }
        response(message);
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
        Clinic clinic = myAgent().getClinics().stream().filter(
                c -> !c.isOccupied() && c.getType() == ClinicType.A
        ).findFirst().orElse(null);
        setResponseMessage(message, msg, doctor, nurse, clinic);
    }

    private void setResponseMessage(MessageForm message, Message msg, EmerDepEmployee doctor, EmerDepEmployee nurse, Clinic clinic) {
        if (doctor == null || nurse == null || clinic == null) {
            msg.setDoctor(null);
            msg.setNurse(null);
            msg.setClinic(null);
        } else {
            doctor.setBusy(true);
            msg.setDoctor(doctor);
            nurse.setBusy(true);
            msg.setNurse(nurse);
            clinic.setOccupied(true);
            msg.setClinic(clinic);
        }
        response(message);
    }

    public void processReleaseResources(MessageForm message) {
        Message msg = (Message) message.createCopy();
        msg.setCode(Mc.releaseResources);
        if (msg.getClinic() != null) {
            myAgent().getClinics().get(msg.getClinic().getId()).setOccupied(false);
        }
        if (msg.getDoctor() != null) {
            myAgent().getDoctors().get(msg.getDoctor().getId()).setBusy(false);
        }
        if (msg.getNurse() != null) {
            myAgent().getNurses().get(msg.getNurse().getId()).setBusy(false);
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