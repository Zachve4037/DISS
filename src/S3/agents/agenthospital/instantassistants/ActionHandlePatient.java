package S3.agents.agenthospital.instantassistants;

import OSPABA.*;
import S3.agents.agenthospital.AgentHospital;
import S3.simulation.*;
import S3.agents.agenthospital.*;

//meta! id="43"
public class ActionHandlePatient extends OSPABA.Action {
    public ActionHandlePatient(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void execute(MessageForm message) {
    }

    @Override
    public AgentHospital myAgent() {
        return (AgentHospital) super.myAgent();
    }

}
