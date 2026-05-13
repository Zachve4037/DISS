package S3.agents.agenthospital.continualassistants;

import OSPABA.*;
import S3.RNGs.empirical.ContinuousEmpiricalDistRNG;
import S3.RNGs.empirical.DiscreteEmpiricalDistRNG;
import S3.RNGs.intervals.EmpiricInterval;
import S3.RNGs.intervals.UniformInterval;
import S3.RNGs.uniform.DiscreteUniformDistRNG;
import S3.entities.Clinic;
import S3.entities.EmerDepEmployee;
import S3.entities.Patient;
import S3.entities.PatientType;
import S3.simulation.*;
import S3.agents.agenthospital.*;

import java.util.List;
import java.util.Random;

//meta! id="39"
public class ProcessInitialExamintation extends OSPABA.Process {
    private ContinuousEmpiricalDistRNG examinationTimePeso = new ContinuousEmpiricalDistRNG(
            new Random(),
            List.of(
                    new EmpiricInterval(3, 5, 0.6),
                    new EmpiricInterval(5, 9, 0.4)
            )
    );
    private DiscreteUniformDistRNG examinationTimeSanitka = new DiscreteUniformDistRNG(
            new Random(), new UniformInterval(4, 8)
    );
    private DiscreteEmpiricalDistRNG priorityPeso = new DiscreteEmpiricalDistRNG(
            new Random(),
            List.of(
                    new EmpiricInterval(1, 2, 0.1),
                    new EmpiricInterval(2, 3, 0.2),
                    new EmpiricInterval(3, 4, 0.15),
                    new EmpiricInterval(4, 5, 0.25),
                    new EmpiricInterval(5, 6, 0.3)
            )
    );
    private DiscreteEmpiricalDistRNG prioritySanitka= new DiscreteEmpiricalDistRNG(
            new Random(),
            List.of(
                    new EmpiricInterval(1, 2, 0.3),
                    new EmpiricInterval(2, 3, 0.25),
                    new EmpiricInterval(3, 4, 0.2),
                    new EmpiricInterval(4, 5, 0.15),
                    new EmpiricInterval(5, 6, 0.1)
            )
    );

    public ProcessInitialExamintation(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication
    }

    //meta! sender="AgentHospital", id="40", type="Start"
    public void processStart(MessageForm message) {
        Message msg = (Message) message.createCopy();
        Patient patient = msg.getPatient();
        EmerDepEmployee nurse = msg.getNurse();
        nurse.startWork(patient);

        Clinic clinic = msg.getClinic();
        clinic.setOccupied(true);

        int priority = (patient.getType() == PatientType.AMBULANCE)
                ? prioritySanitka.generate()
                : priorityPeso.generate();
        patient.setPriority(priority);

        double examinationTime = (patient.getType() == PatientType.AMBULANCE)
                ? examinationTimeSanitka.generate()
                : examinationTimePeso.generate();

        msg.setProcessDuration(examinationTime);
        msg.setCode(Mc.finish);
        hold(examinationTime * 60, msg);
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
    public AgentHospital myAgent() {
        return (AgentHospital) super.myAgent();
    }

}