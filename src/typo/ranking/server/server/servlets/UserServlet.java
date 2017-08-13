package typo.ranking.server.server.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import typo.ranking.server.server.Game;
import typo.ranking.server.server.HeartBeat;
import typo.ranking.server.server.NameGenerator;
import typo.ranking.server.server.Player;
import typo.ranking.server.server.Storage;

public class UserServlet extends HttpServlet {

	static Logger logger = Logger.getLogger( "debug" );
	
	private static String getName() {
		while( true ) {
			String name = NameGenerator.get();
			if( !Storage.get().hasNameBy( name ) ) {
				return name;
			} else {
//				logger.severe( "name generator gave duplicate name " + name );
			}
		}
	}
	
	public static void changeName( long pUserId ) {
		Player player = Storage.get().getPlayerById( pUserId );
		if( player.getNumberOfGames() != 0 ) {
			throw new Error( "trying to change name after game has been played." );
		}
		String name = getName();
		// logger.severe( "generating new name " + player.getId() + " = " + name );
		player.setName( name );
		Storage.get().put( player );
		ChannelServlet.sendMessage( pUserId , ChannelServlet.constructUserData( player ) );
	}
	
	public static Player createUser() {
		String name = getName();
		Player player = new Player( name );
		Storage.get().put( player );
		return player;
	}

	// queued operations to prevent race conditions..
	
	public static void registerWatching( long pUserId , long pGameId ) {
		if( pUserId == 0 ) {
			throw new Error( "trying to register for watching with no userid.");
		}
		TaskOptions options = TaskOptions.Builder.withUrl( "/rankingserver/user" );
		options.method( Method.GET ).param( "c" , "w" ).param( "u" , Long.toString( pUserId ) ).param( "g" , Long.toString( pGameId ) );
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add( options );
	}

	public static void registerChannel( long pUserId , long pChannelId , boolean pBot ) {
		if( pUserId == 0 ) {
			throw new Error( "trying to register channel with no userid.");
		}
		TaskOptions options = TaskOptions.Builder.withUrl( "/rankingserver/user" );
		options.method( Method.GET ).param( "c" , "c" ).param( "u" , Long.toString( pUserId )).param( "h" , Long.toString( pChannelId ) ).param( "b" , Boolean.toString( pBot ) );
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add( options );
	}
	
	public static void registerPairing( long pUserId , boolean pBlitz , boolean pFast ) {
		if( pUserId == 0 ) {
			throw new Error( "trying to register for pairing with no userid.");
		}
		HeartBeat.setPing( pUserId );
		TaskOptions options = TaskOptions.Builder.withUrl( "/rankingserver/user" );
		options.method( Method.GET ).param( "c" , "p" ).param( "u" , Long.toString( pUserId ) ).param( "b" , Boolean.toString( pBlitz ) ).param( "f" , Boolean.toString( pFast ) );
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add( options );
	}
	
	public static void setPlayersLastMoveWatched( long pPlayerId , long pGameId , int pMoveNumber ) {
		Storage.get().putCache( "Watching" + pPlayerId + "x" + pGameId , pMoveNumber );
	}

	public static int getPlayersLastMoveWatched( long pPlayerId , long pGameId ) {
		return (int)Storage.convertLong( Storage.get().getCache( "Watching" + pPlayerId + "x" + pGameId ) );
	}
	
	@Override
	protected void doGet( HttpServletRequest pReq , HttpServletResponse pResp ) throws ServletException , IOException {
		long pUserId=Long.parseLong( pReq.getParameter( "u" ) );
		if( pReq.getParameter( "c" ).equals( "w" ) ) { // watching..
			long gameId=Long.parseLong( pReq.getParameter( "g" ) );
			Player player=Storage.get().getPlayerById( pUserId );
			if( player.getActiveGame() != 0 && gameId != 0 ) {
				logger.warning( "cant register to watch when playing." );
				return;
			}
			player.setWatchingGame( gameId );
			Storage.get().put( player );
			if( gameId != 0 ) {
				Game game=Storage.get().getGameById( gameId );
				logger.info( "watching game : " + player.getName() );
				if( gameId != 0 ) {
					setPlayersLastMoveWatched( player.getId() , game.getId() , game.getNumberOfMoves() );
				}
				ChannelServlet.sendMessage( player.getId() , ChannelServlet.constructGameData( game ) );
				UserServlet.setPlayersLastMoveWatched( player.getId() , game.getId() , game.getNumberOfMoves() );
			} else {
				logger.info( "stopped watching game : " + player.getName() );
			}
			return;
		}
		if( pReq.getParameter( "c" ).equals( "p" ) ) { // pairing..
			boolean blitz=Boolean.parseBoolean( pReq.getParameter( "b" ) );
			boolean fast=Boolean.parseBoolean( pReq.getParameter( "f" ) );
			Player player=Storage.get().getPlayerById( pUserId );
			logger.info( "looking for game " + player.getName() + " " + blitz + " " + fast );
			if( player.getActiveGame() != 0 ) {
				logger.warning( "cant register for pairing when playing." );
				return;
			}
			player.setActiveSearch( blitz , fast );
			Storage.get().put( player );
			return;
		}
		if( pReq.getParameter( "c" ).equals( "c" ) ) { // channel..
			Player player=Storage.get().getPlayerById( pUserId );
			long channelId=Long.parseLong( pReq.getParameter( "h" ) );
			boolean bot=Boolean.parseBoolean( pReq.getParameter( "b" ) );
			player.setChannelId( channelId , bot );
			Storage.get().putCache( "channel" + channelId , player.getId() );
			Storage.get().put( player );
			return;
		}
		throw new Error();
	}
	
	
	
}
