package typo.ranking.server.client;

public abstract class Channel {

	public abstract void onMessage( String message );

	public abstract void onOpen();

	public abstract void onError( int code , String description );

	public abstract void onClose();

	private Object mSocket;
	
	public void join( String channelKey ) {
		mSocket=joinJs( channelKey );
	}
	
	public void close() {
		closeJs( mSocket );
	}
	
	
	private native void closeJs( Object pSocket ) /*-{
		pSocket.close();
	}-*/;
	
	private native Object joinJs( String channelKey ) /*-{
		var channel = new $wnd.goog.appengine.Channel(channelKey);
		var socket = channel.open();
		var self = this;
		socket.onmessage = function(evt) {
			var data = evt.data;
			self.@typo.ranking.server.client.Channel::onMessage(Ljava/lang/String;)(data);
		};
		socket.onopen = function() {
			self.@typo.ranking.server.client.Channel::onOpen()();
		};
		socket.onerror = function(error) {
			self.@typo.ranking.server.client.Channel::onError(ILjava/lang/String;)(error.code, error.description);
		};
		socket.onclose = function() {
			self.@typo.ranking.server.client.Channel::onClose()();
		};
		return socket;
	}-*/;
}
