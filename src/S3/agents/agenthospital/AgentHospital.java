package S3.agents.agenthospital;

import OSPABA.*;
import S3.agents.agenthospital.continualassistants.*;
import S3.agents.agenthospital.instantassistants.*;
import S3.simulation.*;

//meta! id="4"
public class AgentHospital extends OSPABA.Agent {
    public AgentHospital(int id, Simulation mySim, Agent parent) {
        super(id, mySim, parent);
        init();
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    private void init() {
        new ManagerHospital(Id.managerHospital, mySim(), this);
        new ProcessInitialExamintation(Id.processInitialExamintation, mySim(), this);
        new ProcessDoctorsExamination(Id.processDoctorsExamination, mySim(), this);
        new ProcessClinicCleanupBlock(Id.processClinicCleanupBlock, mySim(), this);
        new ActionHandlePatient(Id.actionHandlePatient, mySim(), this);
        addOwnMessage(Mc.allocateDoctorsExaminationBResources);
        addOwnMessage(Mc.handlePatientToWaitingRoom);
        addOwnMessage(Mc.handleInitialPersonalMovement);
        addOwnMessage(Mc.handleDoctorsMovement);
        addOwnMessage(Mc.getPatientForDoctorsExamination);
        addOwnMessage(Mc.allocateInitialExaminationResources);
        addOwnMessage(Mc.handleLeavingPatient);
        addOwnMessage(Mc.handlePatient);
        addOwnMessage(Mc.allocateDoctorsExaminationAResources);
        addOwnMessage(Mc.getPatientForInitialExamination);
        addOwnMessage(Mc.handleNursesMovement);
        addOwnMessage(Mc.handlePatientToClinic);
        addOwnMessage(Mc.releaseResources);
        addOwnMessage(Mc.tryStartInitialExamination);
        addOwnMessage(Mc.tryStartDoctorsExamination);
        addOwnMessage(Mc.allocateDoctorsExaminationResources);
    }
    //meta! tag="end"
}
