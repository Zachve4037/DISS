package S3.agents.agenthospital;

import OSPABA.*;
import S3.simulation.*;

//meta! id="4"
public class ManagerHospital extends OSPABA.Manager
{
	public ManagerHospital(int id, Simulation mySim, Agent myAgent)
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

	//meta! sender="AgentResources", id="71", type="Response"
	public void processAllocateDoctorsExaminationBResources(MessageForm message)
	{
	}

	//meta! sender="AgentMovement", id="74", type="Response"
	public void processHandlePatientToWaitingRoom(MessageForm message)
	{
	}

	//meta! sender="AgentMovement", id="72", type="Response"
	public void processHandeDoctorsMovement(MessageForm message)
	{
	}

	//meta! sender="AgentPatients", id="79", type="Response"
	public void processGetPatientForDoctorsExamination(MessageForm message)
	{
	}

	//meta! sender="ProcessDoctorsExamination", id="42", type="Finish"
	public void processFinishProcessDoctorsExamination(MessageForm message)
	{
	}

	//meta! sender="ProcessInitialExamintation", id="40", type="Finish"
	public void processFinishProcessInitialExamintation(MessageForm message)
	{
	}

	//meta! sender="AgentResources", id="69", type="Response"
	public void processAllocateInitialExaminationResources(MessageForm message)
	{
	}

	//meta! sender="AgentMovement", id="75", type="Response"
	public void processHandleLeavingPatient(MessageForm message)
	{
	}

	//meta! sender="AgentBoss", id="33", type="Request"
	public void processHandlePatient(MessageForm message)
	{
		Message msg = (Message) message.createCopy();
		msg.setCode(Mc.putPatient);
		msg.setAddressee(mySim().findAgent(Id.agentPatients));
		notice(msg);
	}

	//meta! sender="AgentResources", id="70", type="Response"
	public void processAllocateDoctorsExaminationAResources(MessageForm message)
	{
	}

	//meta! sender="AgentPatients", id="78", type="Response"
	public void processGetPatientForInitialExamination(MessageForm message)
	{
	}

	//meta! sender="AgentMovement", id="73", type="Response"
	public void processHandleNursesMovement(MessageForm message)
	{
	}

	//meta! sender="AgentMovement", id="76", type="Response"
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
		case Mc.finish:
			switch (message.sender().id())
			{
			case Id.processDoctorsExamination:
				processFinishProcessDoctorsExamination(message);
			break;

			case Id.processInitialExamintation:
				processFinishProcessInitialExamintation(message);
			break;
			}
		break;

		case Mc.allocateDoctorsExaminationBResources:
			processAllocateDoctorsExaminationBResources(message);
		break;

		case Mc.handleLeavingPatient:
			processHandleLeavingPatient(message);
		break;

		case Mc.allocateInitialExaminationResources:
			processAllocateInitialExaminationResources(message);
		break;

		case Mc.allocateDoctorsExaminationAResources:
			processAllocateDoctorsExaminationAResources(message);
		break;

		case Mc.getPatientForDoctorsExamination:
			processGetPatientForDoctorsExamination(message);
		break;

		case Mc.getPatientForInitialExamination:
			processGetPatientForInitialExamination(message);
		break;

		case Mc.handleNursesMovement:
			processHandleNursesMovement(message);
		break;

		case Mc.handlePatientToWaitingRoom:
			processHandlePatientToWaitingRoom(message);
		break;

		case Mc.handlePatient:
			processHandlePatient(message);
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
	public AgentHospital myAgent()
	{
		return (AgentHospital)super.myAgent();
	}

}