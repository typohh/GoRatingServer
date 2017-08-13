package typo.ranking.server.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;

import typo.ranking.server.shared.Move;

public abstract class Rest {

	public abstract void responseReceived( RequestType pType , String pMessage );

	public abstract void errorReceived( int pStatusCode , String pResponse );

	public abstract void disconnected();

	public abstract void reconnected();

	private class RetrySend {

		public RetrySend( RequestType pType , String pRequestUrl ) {
			mRequestUrl=pRequestUrl;
			mType=pType;
		}
		
		private String mRequestUrl;
		private int mRetries = 0;
		private RequestType mType;
		private Timer mTimer = new Timer() {

			@Override
			public void run() {
				send();
			}
		};
		
		private RequestCallback mCallback = new RequestCallback() {

			@Override
			public void onError( Request request , Throwable exception ) {
				++mRetries;
				disconnected();
				mTimer.schedule( mRetries * mRetries * 100 );
			}

			@Override
			public void onResponseReceived( Request request , Response response ) {
				if( response.getStatusCode() == 0 ) {
					++mRetries;
					disconnected();
					mTimer.schedule( mRetries * mRetries * 100 );
					return;
				}
				if( 200 == response.getStatusCode() ) {
					if( mRetries > 0 ) {
						reconnected();
					}
					if( mType != RequestType.Void ) {
						responseReceived( mType , response.getText() );
					}
				} else {
					errorReceived( response.getStatusCode() , response.getStatusText() );
				}
			}
		};

		public void send() {
			RequestBuilder builder = new RequestBuilder( RequestBuilder.GET , URL.encode( mRequestUrl ) );
			try {
				builder.sendRequest( null , mCallback );
			} catch( RequestException e ) {
				++mRetries;
				disconnected();
				mTimer.schedule( mRetries * mRetries * 100 );
			}
		}
	}

//	private String sUrl=;

	private String getUrl() {
		return GWT.getModuleBaseURL() + "restful";
//		if( GWT.getModuleBaseURL().contains( ":8888" )) {
//			return "http://localhost:8888/rankingserver/restful";
//		} else {
//			return "http://goratingserver.com/rankingserver/restful";
//		}
	}
	
	public void registerForPairing( long pUserId , boolean pBlitz , boolean pFast ) {
		new RetrySend( RequestType.Void , getUrl() + "?c=rfp&u=" + pUserId + "&b=" + pBlitz + "&f=" + pFast ).send();
	}

	public void sendMove( long pUserId , short pMoveNumber , Move pMove ) {
		new RetrySend( RequestType.Void , getUrl() + "?c=sm&u=" + pUserId + "&n=" + pMoveNumber + "&m=" + pMove.toJson() ).send();
	}

	public void ack( long pUserId , long pMessageId ) {
		new RetrySend( RequestType.Void , getUrl() + "?c=a&u=" + pUserId + "&m=" + pMessageId ).send();
	}

	public void registerWatchGame( long pUserId , long pGameId ) {
		new RetrySend( RequestType.Void , getUrl() + "?c=rwg&u=" + pUserId + "&g=" + pGameId ).send();
	}

	public void initialize( long pUserId ) {
		new RetrySend( RequestType.Void , getUrl() + "?c=i&u=" + pUserId ).send();
	}

	public void changeName( long pUserId ) {
		new RetrySend( RequestType.Void , getUrl() + "?c=cn&u=" + pUserId ).send();
	}
	
	public enum RequestType {
		getChannelSecret,
		createUserId,
		Input,
		Void,
	}

	// non listen thingies..
	public void getGameData() {
		new RetrySend( RequestType.Input , getUrl() + "?c=ggd" ).send();
	}

	public void getLeaderboard() {
		new RetrySend( RequestType.Input , getUrl() + "?c=glb" ).send();
	}

	public void getChannelSecret( long pUserId , long pUnique , double pVersion ) {
		new RetrySend( RequestType.getChannelSecret , getUrl() + "?c=gcs&u=" + pUserId + "&a=" + pUnique + "&v=" + pVersion ).send();
	}

	public void createUserId() {
		new RetrySend( RequestType.createUserId , getUrl() + "?c=cui" ).send();
	}
}
