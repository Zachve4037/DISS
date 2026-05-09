package S3.agents.agentenvironment;

import OSPABA.*;
import S3.simulation.*;
import S3.agents.agentenvironment.continualassistants.*;

//meta! id="3"
public class AgentEnvironment extends OSPABA.Agent {
	private int id;
	public AgentEnvironment(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	public int getId() {
		id++;
		return id;
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
		new ManagerEnvironment(Id.managerEnvironment, mySim(), this);
		new SchedulerAmbulance(Id.schedulerAmbulance, mySim(), this);
		new SchedulerThemselves(Id.schedulerThemselves, mySim(), this);
		addOwnMessage(Mc.patientDeparture);
		addOwnMessage(Mc.patientArrival);
	}
    //meta! tag="end"
}