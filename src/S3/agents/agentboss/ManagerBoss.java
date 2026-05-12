package S3.agents.agentboss;

import OSPABA.*;
import S3.entities.Patient;
import S3.simulation.*;

//meta! id="2"
public class ManagerBoss extends OSPABA.Manager {
    public ManagerBoss(int id, Simulation mySim, Agent myAgent) {
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

    //meta! sender="AgentHospital", id="33", type="Response"
    public void processHandlePatient(MessageForm message) {
        //TODO ked pacient odchadza zo systemu
    }

    //meta! sender="AgentEnvironment", id="23", type="Notice"
    public void processPatientArrival(MessageForm message) {
        Message msg = (Message) message;
        EmergencyDepartmentSimulation sim = (EmergencyDepartmentSimulation) mySim();

        if (msg.getPatient() != null) {
            sim.recordArrival(msg.getPatient());
        }

        Message req = (Message) msg.createCopy();
        req.setCode(Mc.handlePatient);
        req.setAddressee(((EmergencyDepartmentSimulation) mySim()).agentHospital());
        request(req);
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
            case Mc.patientArrival:
                processPatientArrival(message);
                break;

            case Mc.handlePatient:
                processHandlePatient(message);
                break;

            default:
                processDefault(message);
                break;
        }
    }
    //meta! tag="end"

    @Override
    public AgentBoss myAgent() {
        return (AgentBoss) super.myAgent();
    }

}