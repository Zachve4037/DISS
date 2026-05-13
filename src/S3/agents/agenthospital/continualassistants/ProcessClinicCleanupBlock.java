package S3.agents.agenthospital.continualassistants;

import OSPABA.CommonAgent;
import OSPABA.MessageForm;
import OSPABA.Simulation;
import S3.agents.agenthospital.AgentHospital;
import S3.simulation.Mc;
import S3.simulation.Message;

public class ProcessClinicCleanupBlock extends OSPABA.Process {
    private static final double CLEANUP_BLOCK_DURATION = 5 * 60;

    public ProcessClinicCleanupBlock(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    public void processStart(MessageForm message) {
        Message msg = (Message) message.createCopy();
        msg.setCode(Mc.finish);
        hold(CLEANUP_BLOCK_DURATION, msg);
    }

    public void processDefault(MessageForm message) {
        switch (message.code()) {
        }
    }

    @Override
    public void processMessage(MessageForm message) {
        switch (message.code()) {
            case Mc.start:
                processStart(message);
                break;

            case Mc.finish:
                assistantFinished(message);
                break;

            default:
                processDefault(message);
                break;
        }
    }

    @Override
    public AgentHospital myAgent() {
        return (AgentHospital) super.myAgent();
    }
}
