package S3.agents.agentmovement;

import OSPABA.*;
import S3.agents.agentmovement.continualassistants.*;
import S3.simulation.*;

//meta! id="63"
public class AgentMovement extends OSPABA.Agent {
    public AgentMovement(int id, Simulation mySim, Agent parent) {
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
        new ManagerMovement(Id.managerMovement, mySim(), this);
        new ProcessPatientLeavingMovement(Id.processPatientLeavingMovement, mySim(), this);
        new ProcessDoctorsMovement(Id.processDoctorsMovement, mySim(), this);
        new ProcessPatientToClinicMovement(Id.processPatientToClinicMovement, mySim(), this);
        new ProcessPatientToWaitingRoomMovement(Id.processPatientToWaitingRoomMovement, mySim(), this);
        new ProcessNursesMovement(Id.processNursesMovement, mySim(), this);
        new ProcessInitialPersonalMovement(Id.processInitialPersonalMovement, mySim(), this);
        addOwnMessage(Mc.handlePatientToWaitingRoom);
        addOwnMessage(Mc.handleInitialPersonalMovement);
        addOwnMessage(Mc.handleDoctorsMovement);
        addOwnMessage(Mc.handleLeavingPatient);
        addOwnMessage(Mc.handleNursesMovement);
        addOwnMessage(Mc.handlePatientToClinic);
    }
    //meta! tag="end"
}