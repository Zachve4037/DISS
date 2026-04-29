package S3.agents.agentmovement;

import OSPABA.*;
import S3.simulation.*;

//meta! id="63"
public class AgentMovement extends OSPABA.Agent
{
	public AgentMovement(int id, Simulation mySim, Agent parent)
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
		new ManagerMovement(Id.managerMovement, mySim(), this);
		addOwnMessage(Mc.handlePatientToWaitingRoom);
		addOwnMessage(Mc.handeDoctorsMovement);
		addOwnMessage(Mc.handleLeavingPatient);
		addOwnMessage(Mc.handleNursesMovement);
		addOwnMessage(Mc.handlePatientToClinic);
	}
	//meta! tag="end"
}