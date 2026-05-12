package S3.agents.agenthospital.continualassistants;

import OSPABA.*;
import S3.RNGs.empirical.ContinuousEmpiricalDistRNG;
import S3.RNGs.intervals.EmpiricInterval;
import S3.RNGs.intervals.UniformInterval;
import S3.RNGs.uniform.ContinuousUniformDistRNG;
import S3.entities.PatientType;
import S3.simulation.*;
import S3.agents.agenthospital.*;
import OSPABA.Process;

import java.util.List;
import java.util.Random;

//meta! id="41"
public class ProcessDoctorsExamination extends OSPABA.Process {
    private ContinuousEmpiricalDistRNG examinationTimePeso = new ContinuousEmpiricalDistRNG(
            new Random(),
            List.of(
                    new EmpiricInterval(10, 12, 0.1),
                    new EmpiricInterval(12, 14, 0.6),
                    new EmpiricInterval(14, 18, 0.3)
            )
    );
    private ContinuousUniformDistRNG examinationTimeSanitka = new ContinuousUniformDistRNG(
            new Random(), new UniformInterval(15, 30)
    );

    public ProcessDoctorsExamination(int id, Simulation mySim, CommonAgent myAgent) {
        super(id, mySim, myAgent);
    }

    @Override
    public void prepareReplication() {
        super.prepareReplication();
        // Setup component for the next replication
    }

    //meta! sender="AgentHospital", id="42", type="Start"
    public void processStart(MessageForm message) {
        Message msg = (Message) message.createCopy();
        double time = (msg.getPatient().getType() == PatientType.AMBULANCE)
                ? examinationTimeSanitka.generate()
                : examinationTimePeso.generate();
        msg.setCode(Mc.finish);

        hold(time * 60, msg);
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