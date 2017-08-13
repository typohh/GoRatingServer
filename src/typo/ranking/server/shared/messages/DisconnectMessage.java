package typo.ranking.server.shared.messages;

public class DisconnectMessage implements Message {

	private long mMessageId;
	private long mActive;

	private DisconnectMessage() {}

	public DisconnectMessage( long pMessageId , long pActive ) {
		mMessageId=pMessageId;
		mActive = pActive;
	}

	@Override
	public String toJson() {
		return "{\"type\":\"disconnect\",\"active\":\"" + mActive + "\",\"messageId\":\"" + mMessageId + "\"}";
	}

	@Override
	public long getMessageId() {
		return mMessageId;
	}
}
