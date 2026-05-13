package S3.agents.agentmovement.continualassistants;

import OSPABA.*;
import S3.RNGs.intervals.TriangleInterval;
import S3.RNGs.triangle.ContinuousTriangularDistRNG;
import S3.simulation.*;
import S3.agents.agentmovement.*;
import S3.entities.EmerDepEmployee;

import java.util.Random;

//meta! id="91"
public class ProcessNursesMovement extends OSPABA.Process {
    private ContinuousTriangularDistRNG movementTime = new ContinuousTriangularDistRNG(new Random(), new TriangleInterval(15, 20, 45));

    public ProcessNursesMovement(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication
    }

    //meta! sender="AgentMovement", id="92", type="Start"
    public void processStart(MessageForm message) {
        Message msg = (Message) message.createCopy();
        double time = movementTime.generate();
        EmerDepEmployee nurse = msg.getNurse();
        ((EmergencyDepartmentSimulation) mySim()).edAnimator().employeeMoving(nurse, time);
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