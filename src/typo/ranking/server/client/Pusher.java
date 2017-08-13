package typo.ranking.server.client;

public class Pusher {

	private Object mPusher;
	
	private native Object joinJs( Object pPusher , String pChannel ) /*-{
		var pusher = new Pusher('be759ea99bd5db89bd42', {
			encrypted : false
		});
	}-*/;
}
