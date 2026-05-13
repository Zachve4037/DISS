package S3.agents.agentmovement.continualassistants;

import OSPABA.*;
import S3.RNGs.intervals.TriangleInterval;
import S3.RNGs.triangle.ContinuousTriangularDistRNG;
import S3.simulation.*;
import S3.entities.Patient;
import S3.entities.Clinic;
import S3.agents.agentmovement.*;

import java.util.Random;

//meta! id="97"
public class ProcessPatientToClinicMovement extends OSPABA.Process {
    private static final double SAME_CLINIC_TRANSFER_EPSILON = 1e-6;
    private ContinuousTriangularDistRNG movementTime = new ContinuousTriangularDistRNG(new Random(), new TriangleInterval(15, 20, 45));

    public ProcessPatientToClinicMovement(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication
    }

    //meta! sender="AgentMovement", id="98", type="Start"
    public void processStart(MessageForm message) {
        Message msg = (Message) message.createCopy();
        Patient patient = msg.getPatient();
        Clinic clinic = msg.getClinic();
        double time = isSameClinicDoctorExamination(patient, clinic)
                ? SAME_CLINIC_TRANSFER_EPSILON
                : movementTime.generate();
        ((EmergencyDepartmentSimulation) mySim()).edAnimator().movePatientToClinic(patient, clinic, time);
        msg.setCode(Mc.finish);
        hold(time, msg);
    }

    private boolean isSameClinicDoctorExamination(Patient patient, Clinic clinic) {
        return patient != null
                && clinic != null
                && patient.isWaitingForDoctorsExam()
                && patient.getLastClinic() == clinic;
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
