package S3.agents.agentmovement;

import OSPABA.*;
import S3.simulation.*;

//meta! id="63"
public class ManagerMovement extends OSPABA.Manager
{
	public ManagerMovement(int id, Simulation mySim, Agent myAgent)
	{
		super(id, mySim, myAgent);
		init();
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication

		if (petriNet() != null)
		{
			petriNet().clear();
		}
	}

	//meta! sender="AgentHospital", id="74", type="Request"
	public void processHandlePatientToWaitingRoom(MessageForm message)
	{
	}

	//meta! sender="AgentHospital", id="72", type="Request"
	public void processHandeDoctorsMovement(MessageForm message)
	{
	}

	//meta! sender="AgentHospital", id="75", type="Request"
	public void processHandleLeavingPatient(MessageForm message)
	{
	}

	//meta! sender="AgentHospital", id="73", type="Request"
	public void processHandleNursesMovement(MessageForm message)
	{
	}

	//meta! sender="AgentHospital", id="76", type="Request"
	public void processHandlePatientToClinic(MessageForm message)
	{
	}

	//meta! userInfo="Process messages defined in code", id="0"
	public void processDefault(MessageForm message)
	{
		switch (message.code())
		{
		}
	}

	//meta! userInfo="Generated code: do not modify", tag="begin"
	public void init()
	{
	}

	@Override
	public void processMessage(MessageForm message)
	{
		switch (message.code())
		{
		case Mc.handeDoctorsMovement:
			processHandeDoctorsMovement(message);
		break;

		case Mc.handleNursesMovement:
			processHandleNursesMovement(message);
		break;

		case Mc.handleLeavingPatient:
			processHandleLeavingPatient(message);
		break;

		case Mc.handlePatientToClinic:
			processHandlePatientToClinic(message);
		break;

		case Mc.handlePatientToWaitingRoom:
			processHandlePatientToWaitingRoom(message);
		break;

		default:
			processDefault(message);
		break;
		}
	}
	//meta! tag="end"

	@Override
	public AgentMovement myAgent()
	{
		return (AgentMovement)super.myAgent();
	}

}
