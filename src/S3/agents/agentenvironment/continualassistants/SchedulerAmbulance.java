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
    private int patientIdCounter;

    public SchedulerAmbulance(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        this.ambulanceArrivalTimeRNG = new ContinuousErlangDistRNG(new Random(), 8, 0.0212);
        this.patientIdCounter = 0;
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
                Patient patient = new Patient(_mySim, patientIdCounter++, PatientType.AMBULANCE);

                double nextDelay = ambulanceArrivalTimeRNG.generate();
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