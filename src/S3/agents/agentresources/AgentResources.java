package S3.agents.agentresources;

import OSPABA.*;
import S3.simulation.*;

//meta! id="61"
public class AgentResources extends OSPABA.Agent
{
	public AgentResources(int id, Simulation mySim, Agent parent)
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
		new ManagerResources(Id.managerResources, mySim(), this);
		addOwnMessage(Mc.allocateDoctorsExaminationBResources);
		addOwnMessage(Mc.allocateInitialExaminationResources);
		addOwnMessage(Mc.allocateDoctorsExaminationAResources);
	}
	//meta! tag="end"
}
