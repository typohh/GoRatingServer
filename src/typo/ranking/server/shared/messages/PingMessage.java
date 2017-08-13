package typo.ranking.server.shared.messages;

public class PingMessage implements Message {

	private PingMessage() {}

	private long mMessageId;

	public PingMessage( long pMessageId ) {
		mMessageId = pMessageId;
	}

	@Override
	public String toJson() {
		return "{\"type\":\"ping\",\"messageId\":\"" + mMessageId + "\"}";
	}

	@Override
	public long getMessageId() {
		return mMessageId;
	}
}
