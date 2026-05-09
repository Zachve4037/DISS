package S3.agents.agentenvironment.continualassistants;

import OSPABA.*;
import S3.RNGs.erlang.ContinuousErlangDistRNG;
import S3.simulation.*;
import S3.agents.agentenvironment.*;
import S3.entities.Patient;
import S3.entities.PatientType;

import java.util.Random;

//meta! id="15"
public class SchedulerAmbulance extends OSPABA.Scheduler {
    private ContinuousErlangDistRNG ambulanceArrivalTimeRNG;

    public SchedulerAmbulance(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        this.ambulanceArrivalTimeRNG = new ContinuousErlangDistRNG(new Random(), 8, 0.0212);
    }

    //meta! sender="AgentEnvironment", id="16", type="Start"
    public void processStart(MessageForm message) {
        double nextDelay = ambulanceArrivalTimeRNG.generate();
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
                Message msg = (Message) message;

                Patient patient = new Patient(mySim(), myAgent().getId(), PatientType.AMBULANCE);
                msg.setPatient(patient);

                double nextDelay = ambulanceArrivalTimeRNG.generate();
                Message nextMsg = (Message) message.createCopy();
                nextMsg.setCode(Mc.patientArrival);
                hold(nextDelay, nextMsg);

                assistantFinished(msg);
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