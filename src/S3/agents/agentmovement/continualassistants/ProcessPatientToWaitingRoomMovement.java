package S3.agents.agentmovement.continualassistants;

import OSPABA.*;
import S3.RNGs.intervals.TriangleInterval;
import S3.RNGs.intervals.UniformInterval;
import S3.RNGs.triangle.ContinuousTriangularDistRNG;
import S3.RNGs.uniform.ContinuousUniformDistRNG;
import S3.entities.Patient;
import S3.entities.PatientType;
import S3.simulation.*;
import S3.agents.agentmovement.*;

import java.util.Random;

//meta! id="93"
public class ProcessPatientToWaitingRoomMovement extends OSPABA.Process {
    private ContinuousTriangularDistRNG movementTimePeso = new ContinuousTriangularDistRNG(new Random(), new TriangleInterval(120, 150, 300));
    private ContinuousUniformDistRNG movementTimeSanitka = new ContinuousUniformDistRNG(new Random(), new UniformInterval(90, 200));

    public ProcessPatientToWaitingRoomMovement(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication
    }

    //meta! sender="AgentMovement", id="94", type="Start"
    public void processStart(MessageForm message) {
        Message msg = (Message) message.createCopy();
        Patient patient = msg.getPatient();

        if (patient == null) {
            System.out.println("Error: No patient found for movement to waiting room.");
        }

        double time = (patient.getType() == PatientType.AMBULANCE)
                ? movementTimeSanitka.generate()
                : movementTimePeso.generate();

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