package S3.agents.agentpatients;

import OSPABA.*;
import S3.simulation.*;

//meta! id="62"
public class AgentPatients extends OSPABA.Agent
{
	public AgentPatients(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
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