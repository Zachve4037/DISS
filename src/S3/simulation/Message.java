package S3.simulation;

import OSPABA.*;
import S3.entities.Patient;
import S3.entities.EmerDepEmployee;

public class Message extends OSPABA.MessageForm
{
	private Patient patient;
	private EmerDepEmployee employee;
	private double processDuration;

	public Message(Simulation mySim)
	{
		super(mySim);
		this.patient = null;
		this.employee = null;
		this.processDuration = -1;
	}

	private Message(Message original)
	{
		super(original);
		// copy() is called in superclass
	}

	@Override
	public MessageForm createCopy()
	{
		return new Message(this);
	}

	@Override
	protected void copy(MessageForm message)
	{
		super.copy(message);
		Message original = (Message)message;
		this.patient = original.patient;
		this.employee = original.employee;
		this.processDuration = original.processDuration;
	}

	public Patient getPatient()
	{
		return patient;
	}

	public void setPatient(Patient patient)
	{
		this.patient = patient;
	}

	public EmerDepEmployee getEmployee()
	{
		return employee;
	}

	public void setEmployee(EmerDepEmployee employee)
	{
		this.employee = employee;
	}

	public double getProcessDuration()
	{
		return processDuration;
	}

	public void setProcessDuration(double processDuration)
	{
		this.processDuration = processDuration;
	}
}