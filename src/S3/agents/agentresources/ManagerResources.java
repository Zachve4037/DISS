package S3.agents.agentresources;

import OSPABA.*;
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
    }

    //meta! sender="AgentHospital", id="69", type="Request"
    public void processAllocateInitialExaminationResources(MessageForm message) {
    }

    //meta! sender="AgentHospital", id="70", type="Request"
    public void processAllocateDoctorsExaminationAResources(MessageForm message) {
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