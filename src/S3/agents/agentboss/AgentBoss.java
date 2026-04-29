package S3.agents.agentboss;

import OSPABA.*;
import S3.simulation.*;

//meta! id="2"
public class AgentBoss extends OSPABA.Agent
{
	public AgentBoss(int id, Simulation mySim, Agent parent)
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
		new ManagerBoss(Id.managerBoss, mySim(), this);
		addOwnMessage(Mc.handlePatient);
		addOwnMessage(Mc.patientArrival);
	}
	//meta! tag="end"
}