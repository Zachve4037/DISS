package S3.agents.agentpatients;

import OSPABA.*;
import S3.entities.Patient;
import S3.simulation.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//meta! id="62"
public class AgentPatients extends OSPABA.Agent {

	//patients queues
	private Queue<Patient> initialExaminationQueue;
	private Queue<Patient> doctorsExaminationQueue;

	public AgentPatients(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
		initialExaminationQueue = new ConcurrentLinkedQueue<>();
		doctorsExaminationQueue = new ConcurrentLinkedQueue<>();
	}

	public Queue<Patient> getInitialExaminationQueue() {
		return initialExaminationQueue;
	}

	public Queue<Patient> getDoctorsExaminationQueue() {
		return doctorsExaminationQueue;
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		this.doctorsExaminationQueue.clear();
		this.initialExaminationQueue.clear();
		// Setup component for the next replication
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	private void init()
	{
		new ManagerPatients(Id.managerPatients, mySim(), this);
		addOwnMessage(Mc.getPatientForDoctorsExamination);
		addOwnMessage(Mc.putPatient);
		addOwnMessage(Mc.getPatientForInitialExamination);
	}
	//meta! tag="end"
}