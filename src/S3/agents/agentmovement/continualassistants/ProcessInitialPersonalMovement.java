package S3.agents.agentmovement.continualassistants;

import OSPABA.*;
import S3.RNGs.intervals.UniformInterval;
import S3.RNGs.uniform.ContinuousUniformDistRNG;
import S3.agents.agentmovement.AgentMovement;
import S3.entities.EmerDepEmployee;
import S3.simulation.*;

import java.util.Random;

//meta! id="105"
public class ProcessInitialPersonalMovement extends OSPABA.Process {
    private ContinuousUniformDistRNG movementTime = new ContinuousUniformDistRNG(new Random(), new UniformInterval(90, 200));

    public ProcessInitialPersonalMovement(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication
    }

    //meta! sender="AgentMovement", id="106", type="Start"
    public void processStart(MessageForm message) {
        Message msg = (Message) message.createCopy();

        EmerDepEmployee employee = msg.getDoctor() != null
                ? msg.getDoctor()
                : msg.getNurse();

        employee.setBusy(true);

        double time = movementTime.generate();
        msg.setCode(Mc.finish);

        hold(time, msg);
    }

    //meta! userInfo="Process messages defined in code", id="0"
    public void processDefault(MessageForm message) {
        switch (message.code()) {
        }
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
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
    //meta! tag="end"

    @Override
    public AgentMovement myAgent() {
        return (AgentMovement) super.myAgent();
    }

}
