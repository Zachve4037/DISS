package S3.agents.agentenvironment;

import OSPABA.*;
import S3.simulation.*;

//meta! id="3"
public class ManagerEnvironment extends OSPABA.Manager {
    public ManagerEnvironment(int id, Simulation mySim, Agent myAgent) {
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

    //meta! sender="SchedulerAmbulance", id="16", type="Finish"
    public void processFinishSchedulerAmbulance(MessageForm message) {
        Message msg = (Message) message.createCopy();
        msg.setCode(Mc.patientArrival);
        msg.setAddressee(mySim().findAgent(Id.agentBoss));
        notice(msg);
    }

    //meta! sender="SchedulerThemselves", id="18", type="Finish"
    public void processFinishSchedulerThemselves(MessageForm message) {
        Message msg = (Message) message.createCopy();
        msg.setCode(Mc.patientArrival);
        msg.setAddressee(mySim().findAgent(Id.agentBoss));
        notice(msg);
    }

    //meta! sender="AgentBoss", id="32", type="Notice"
    public void processPatientDeparture(MessageForm message) {
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
            case Mc.finish:
                switch (message.sender().id()) {
                    case Id.schedulerAmbulance:
                        processFinishSchedulerAmbulance(message);
                        break;

                    case Id.schedulerThemselves:
                        processFinishSchedulerThemselves(message);
                        break;
                }
                break;

            case Mc.start:
                Message msgAmb = (Message) message.createCopy();
                msgAmb.setAddressee(Id.schedulerAmbulance);
                startContinualAssistant(msgAmb);

                Message msgWalk = (Message) message.createCopy();
                msgWalk.setAddressee(Id.schedulerThemselves);
                startContinualAssistant(msgWalk);
                break;

            case Mc.patientDeparture:
                processPatientDeparture(message);
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