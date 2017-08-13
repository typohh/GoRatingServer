package typo.ranking.server.shared.messages;


public interface Message {
	
	public long getMessageId();
	
	public String toJson();
	
}
