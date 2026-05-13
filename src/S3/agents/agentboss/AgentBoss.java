package S3.agents.agentboss;

import OSPABA.*;
import S3.simulation.*;

//meta! id="2"
public class AgentBoss extends OSPABA.Agent {
    public AgentBoss(int id, Simulation mySim, Agent parent) {
        super(id, mySim, parent);
        init();
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
    }

    public void startSimulation() {
        Message envStart = new Message(mySim());
        envStart.setCode(Mc.start);
        envStart.setAddressee(Id.agentEnvironment);
        myManager().notice(envStart);

        Message staffMove = new Message(mySim());
        staffMove.setCode(Mc.handleInitialPersonalMovement);
        staffMove.setAddressee(Id.agentHospital);
        myManager().notice(staffMove);
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    private void init() {
        new ManagerBoss(Id.managerBoss, mySim(), this);
        addOwnMessage(Mc.handlePatient);
        addOwnMessage(Mc.patientArrival);
        addOwnMessage(Mc.handleInitialPersonalMovement);
        addOwnMessage(Mc.patientDeparture);
    }
    //meta! tag="end"
}
