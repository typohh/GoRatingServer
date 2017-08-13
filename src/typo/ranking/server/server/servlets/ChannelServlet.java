package typo.ranking.server.server.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import typo.ranking.server.server.Game;
import typo.ranking.server.server.HehRating;
import typo.ranking.server.server.Player;
import typo.ranking.server.server.Storage;
import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.messages.GameDataMessage;
import typo.ranking.server.shared.messages.Message;
import typo.ranking.server.shared.messages.UserDataMessage;

public class ChannelServlet extends HttpServlet {

	private final static Logger logger = Logger.getLogger( "debug" );

	private static Queue getQueue() {
		return QueueFactory.getQueue( "message-queue" );
	}

	@Override
	protected void service( HttpServletRequest pRequest , HttpServletResponse pResponse ) throws ServletException , IOException {
		String message = pRequest.getParameter( "m" );
		ChannelService cs = ChannelServiceFactory.getChannelService();
		long playerId = Long.parseLong( pRequest.getParameter( "u" ) );
		long channelId = getChannelId( playerId );
		logger.info( "resending " + playerId + "/" + channelId + " " + message );
		cs.sendMessage( new ChannelMessage( Long.toString( channelId ) , message ) );
		pResponse.sendError( HttpServletResponse.SC_GATEWAY_TIMEOUT );
	}

	private static long getChannelId( long pPlayerId ) {
		Long channelId = (Long) Storage.get().getCache( "channel" + pPlayerId );
		if( channelId == null ) {
			Player player = Storage.get().getPlayerById( pPlayerId );
			if( player == null ) {
				throw new Error( "unknown player id.." );
			}
			channelId = player.getChannelId();
			setChannelId( pPlayerId , channelId );
		}
		return channelId;
	}

	private static boolean isBot( long pPlayerId ) {
		Boolean isBot = (Boolean) Storage.get().getCache( "isbot" + pPlayerId );
		if( isBot == null ) {
			Player player = Storage.get().getPlayerById( pPlayerId );
			if( player == null ) {
				throw new Error( "unknown player id.." );
			}
			isBot = player.isBot();
			setIsBot( pPlayerId , isBot );
		}
		return isBot;
	}

	private static String createName( long pUserId , long pMessageId ) {
		return pUserId + "" + pMessageId;
	}

	public static void sendMessageBot( long pUserId , Message pMessage ) {
		try {
			final String charset = "UTF-8";
			URLConnection connection = new URL( "http://bot.goratingserver.com:9091/" ).openConnection();
			connection.setConnectTimeout( 5000 ); // 5 seconds to open it..
			connection.setReadTimeout( 30000 ); // 30 seconds to read it..
			connection.setDoOutput( true ); // Triggers POST.
			connection.setRequestProperty( "Accept-Charset" , charset );
			connection.setRequestProperty( "Content-Type" , "application/json;charset=" + charset );
			OutputStream output = connection.getOutputStream();
			String message=pMessage.toJson();
			long id=message.hashCode();
			String messageWrapped="{\"t\":\"broadcast\",\"i\":\"" + id + "\",\"d\":[\"" + pUserId + "\"],\"m\":" + pMessage.toJson() + "}";
			output.write( messageWrapped.getBytes( charset ) );
			output.flush();
			output.close();
			StringBuffer response = new StringBuffer();
			InputStream input = connection.getInputStream();
			for( int c = input.read() ; c != -1 ; c = input.read() ) {
				response.append( (char) c );
			}
			String expected="{\"id\":\"" + id + "\"}";
			if( !response.equals( expected )) {
				logger.severe( "response was " + response + " when expected " + expected );
			}
		} catch( IOException pE ) {
			logger.severe( pE.getMessage() );
		}
	}
	
	public static void sendMessage( long pUserId , Message pMessage ) {
//		TaskOptions options = TaskOptions.Builder.withMethod( TaskOptions.Method.POST );
//		options.url( "/rankingserver/channel" );
//		options.taskName( createName( pUserId , pMessage.getMessageId() ) );
//		options.param( "u" , Long.toString( pUserId ) );
//		options.param( "m" , pMessage.toJson() );
//		options.countdownMillis( 3000 );
//		getQueue().add( options );
		boolean isBot=isBot( pUserId );
		logger.info( "sending " + pUserId + pMessage.toJson() );
		if( isBot ) {
			sendMessageBot( pUserId , pMessage );
		} else {
			ChannelService csf = ChannelServiceFactory.getChannelService();
			long channelId = getChannelId( pUserId );
			csf.sendMessage( new ChannelMessage( Long.toString( channelId ) , pMessage.toJson() ) );
		}
	}

	public static void setChannelId( long pPlayerId , long pChannelId ) {
		Storage.get().putCache( "channel" + pPlayerId , pChannelId );
	}

	public static void setIsBot( long pPlayerId , boolean pIsBot ) {
		Storage.get().putCache( "isbot" + pPlayerId , pIsBot );
	}

	public static void updateWatchersAndPlayers( Game pGame , Message pMessage ) {
		int moveNumber = pGame.getNumberOfMoves();
		if( pGame.hasDead() ) {
			++moveNumber;
		}
		ChannelServlet.sendMessage( pGame.getPlayerBlack() , pMessage );
		ChannelServlet.sendMessage( pGame.getPlayerWhite() , pMessage );
		for( long userId : Storage.get().getWatchingz( pGame.getId() ) ) {
			int last = UserServlet.getPlayersLastMoveWatched( userId , pGame.getId() );
			if( last != moveNumber - 1 ) {
				logger.warning( "user " + userId + " last received " + last + " when should be " + (moveNumber - 1) );
				ChannelServlet.sendMessage( userId , ChannelServlet.constructGameData( pGame ) );
			} else {
				ChannelServlet.sendMessage( userId , pMessage );
			}
			UserServlet.setPlayersLastMoveWatched( userId , pGame.getId() , pGame.getNumberOfMoves() );
		}
	}

	public static void receivedAck( long pUserId , long pMessageId ) {
		if( !getQueue().deleteTask( pUserId + "" + pMessageId ) ) {
			logger.info( "no message corresponding to ack" );
		}
	}

	public static long generateMessageId() {
		final Random random = new Random();
		return random.nextLong() + Thread.currentThread().getId(); // to avoid accidentally generating the same..
	}

	public static Message constructGameData( Game pGame ) {
		Player playerBlack = Storage.get().getPlayerById( pGame.getPlayerBlack() );
		Player playerWhite = Storage.get().getPlayerById( pGame.getPlayerWhite() );
		GameDataMessage gd = new GameDataMessage( generateMessageId() );
		gd.getPlayer( true ).set( playerBlack.getName() , playerBlack.getRating().getRating() , pGame.getPlayerPeriods( playerBlack.getId() ) );
		gd.getPlayer( false ).set( playerWhite.getName() , playerWhite.getRating().getRating() , pGame.getPlayerPeriods( playerWhite.getId() ) );
		for( Move move : pGame.getMoves() ) {
			gd.addMove( move );
		}
		gd.setTimePerPeriod( pGame.getPeriodTime() );
		gd.setTimeSinceLastMove( (int)( System.currentTimeMillis() - pGame.getDateLastMove() ) );
		gd.setDateCreated( pGame.getDateCreated() );
		gd.setGameId( pGame.getId() );
		if( pGame.hasDead() ) {
			gd.setHasDead();
			for( Move move : pGame.getDead() ) {
				gd.addDead( move );
			}
		}
		return gd;
	}

	public static Message constructUserData( Player pPlayer ) {
		HehRating rating = pPlayer.getRating();
		return new UserDataMessage( generateMessageId() , pPlayer.getName() , pPlayer.getId() , rating.getRating() , pPlayer.getNumberOfGames() , pPlayer.isLookingForBlitz() , pPlayer.isLookingForFast() );
	}

	public static String getChannelSecret( long pChannelId ) {
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		return channelService.createChannel( Long.toString( pChannelId ) );
	}
}
