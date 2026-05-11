package S3.agents.agentmovement;

import OSPABA.*;
import S3.entities.EmerDepEmployee;
import S3.simulation.*;

//meta! id="63"
public class ManagerMovement extends OSPABA.Manager {
    public ManagerMovement(int id, Simulation mySim, Agent myAgent) {
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

    //meta! sender="AgentHospital", id="74", type="Request"
    public void processHandlePatientToWaitingRoom(MessageForm message) {
        message.setAddressee(myAgent().findAssistant(Id.processPatientToWaitingRoomMovement));
        startContinualAssistant(message);
    }

    //meta! sender="AgentHospital", id="72", type="Request"
    public void processHandeDoctorsMovement(MessageForm message) {
    }

    //meta! sender="AgentHospital", id="75", type="Request"
    public void processHandleLeavingPatient(MessageForm message) {
    }

    //meta! sender="AgentHospital", id="73", type="Request"
    public void processHandleNursesMovement(MessageForm message) {
    }

    //meta! sender="AgentHospital", id="76", type="Request"
    public void processHandlePatientToClinic(MessageForm message) {
    }

    //meta! userInfo="Process messages defined in code", id="0"
    public void processDefault(MessageForm message) {
        switch (message.code()) {
        }
    }

    //meta! sender="ProcessPatientToWaitingRoomMovement", id="94", type="Finish"
    public void processFinishProcessPatientToWaitingRoomMovement(MessageForm message) {
        message.setCode(Mc.handlePatientToWaitingRoom);
        response(message);
    }

    //meta! sender="ProcessDoctorsMovement", id="90", type="Finish"
    public void processFinishProcessDoctorsMovement(MessageForm message) {
    }

    //meta! sender="ProcessPatientToClinicMovement", id="98", type="Finish"
    public void processFinishProcessPatientToClinicMovement(MessageForm message) {
    }

    //meta! sender="ProcessNursesMovement", id="92", type="Finish"
    public void processFinishProcessNursesMovement(MessageForm message) {
    }

    //meta! sender="ProcessPatientLeavingMovement", id="96", type="Finish"
    public void processFinishProcessPatientLeavingMovement(MessageForm message) {
    }

    //meta! sender="AgentHospital", id="109", type="Request"
    public void processHandleInitialPersonalMovement(MessageForm message) {
        message.setAddressee(myAgent().findAssistant(Id.processInitialPersonalMovement));
        startContinualAssistant(message);
    }

    //meta! sender="ProcessInitialPersonalMovement", id="106", type="Finish"
    public void processFinishProcessInitialPersonalMovement(MessageForm message) {
        message.setCode(Mc.handleInitialPersonalMovement);
        EmerDepEmployee employee = ((Message) message).getEmployee();
        employee.setBusy(false);
        response(message);
    }

    //meta! userInfo="Generated code: do not modify", tag="begin"
    public void init() {
    }

    @Override
    public void processMessage(MessageForm message) {
        switch (message.code()) {
            case Mc.handleLeavingPatient:
                processHandleLeavingPatient(message);
                break;

            case Mc.finish:
                switch (message.sender().id()) {
                    case Id.processDoctorsMovement:
                        processFinishProcessDoctorsMovement(message);
                        break;

                    case Id.processPatientLeavingMovement:
                        processFinishProcessPatientLeavingMovement(message);
                        break;

                    case Id.processInitialPersonalMovement:
                        processFinishProcessInitialPersonalMovement(message);
                        break;

                    case Id.processPatientToWaitingRoomMovement:
                        processFinishProcessPatientToWaitingRoomMovement(message);
                        break;

                    case Id.processPatientToClinicMovement:
                        processFinishProcessPatientToClinicMovement(message);
                        break;

                    case Id.processNursesMovement:
                        processFinishProcessNursesMovement(message);
                        break;
                }
                break;

            case Mc.handleInitialPersonalMovement:
                processHandleInitialPersonalMovement(message);
                break;

            case Mc.handleNursesMovement:
                processHandleNursesMovement(message);
                break;

            case Mc.handlePatientToWaitingRoom:
                processHandlePatientToWaitingRoom(message);
                break;

            case Mc.handlePatientToClinic:
                processHandlePatientToClinic(message);
                break;

            case Mc.handeDoctorsMovement:
                processHandeDoctorsMovement(message);
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