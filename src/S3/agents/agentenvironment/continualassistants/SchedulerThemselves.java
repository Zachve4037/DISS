package S3.agents.agentenvironment.continualassistants;

import OSPABA.*;
import S2.RNGs.exponential.ContinuousExponentialDistRNG;
import S3.simulation.*;
import S3.agents.agentenvironment.*;
import S3.entities.Patient;
import S3.entities.PatientType;

import java.util.Random;

//meta! id="17"
public class SchedulerThemselves extends OSPABA.Scheduler {
    private ContinuousExponentialDistRNG walkInArrivalTimeRNG;
    private int patientIdCounter;

    public SchedulerThemselves(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        this.walkInArrivalTimeRNG = new ContinuousExponentialDistRNG(new Random(), 1.0 / 565.4236);
        this.patientIdCounter = 10000000;
    }

    //meta! sender="AgentEnvironment", id="18", type="Start"
    public void processStart(MessageForm message) {
        double nextDelay = walkInArrivalTimeRNG.generate();
        Message msgCopy = (Message) message.createCopy();
        msgCopy.setCode(Mc.patientArrival);
        hold(nextDelay, msgCopy);
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

            case Mc.patientArrival:
                Patient patient = new Patient(_mySim, patientIdCounter++, PatientType.WALK_IN);

                double nextDelay = walkInArrivalTimeRNG.generate();
                Message msgCopy = (Message) message.createCopy();
                msgCopy.setCode(Mc.patientArrival);
                hold(nextDelay, msgCopy);

                Message arrivalMsg = new Message(mySim());
                arrivalMsg.setCode(Mc.patientArrival);
                arrivalMsg.setAddressee(myAgent());
                arrivalMsg.setPatient(patient);
                notice(arrivalMsg);
                break;

            default:
                processDefault(message);
                break;
        }
    }
    //meta! tag="end"

    @Override
    public AgentEnvironment myAgent() {
        return (AgentEnvironment) super.myAgent();
    }

}