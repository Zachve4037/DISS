package S3.simulation;

import OSPABA.*;

public class Message extends OSPABA.MessageForm
{
	public Message(Simulation mySim)
	{
		super(mySim);
	}

	public Message(Message original)
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
		// Copy attributes
	}
}