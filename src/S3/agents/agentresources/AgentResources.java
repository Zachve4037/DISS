package S3.agents.agentresources;

import OSPABA.*;
import S3.entities.Clinic;
import S3.entities.EmerDepEmployee;
import S3.simulation.*;

import java.util.ArrayList;
import java.util.List;

//meta! id="61"
public class AgentResources extends OSPABA.Agent {
	private List<Clinic> clinics;
	private List<EmerDepEmployee> doctors;
	private List<EmerDepEmployee> nurses;

	private int numberOfDocs;
	private int numberOfNurses;

	public AgentResources(int id, Simulation mySim, Agent parent)
	{
		super(id, mySim, parent);
		init();
	}

	public List<Clinic> getClinics() {
		return clinics;
	}

	public List<EmerDepEmployee> getDoctors() {
		return doctors;
	}

	public List<EmerDepEmployee> getNurses() {
		return nurses;
	}

	public void setNumberOfPersonal(int doctors, int nurses) {
		this.numberOfDocs = doctors;
		this.numberOfNurses = nurses;
	}

	@Override
	public void prepareReplication()
	{
		super.prepareReplication();
		// Setup component for the next replication
		doctors = new ArrayList<>();
		nurses = new ArrayList<>();
		clinics = new ArrayList<>();
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