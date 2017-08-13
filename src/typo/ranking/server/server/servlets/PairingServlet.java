package typo.ranking.server.server.servlets;

import java.io.IOException;
import java.util.ArrayList;
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
import typo.ranking.server.server.HehRating;
import typo.ranking.server.server.Player;
import typo.ranking.server.server.Storage;
import typo.ranking.server.shared.messages.PingMessage;

public class PairingServlet extends HttpServlet {

	private static void createGame( Player pA , Player pB ) {
		if( pA.getId() == pB.getId() ) {
			throw new Error( "pairing against self.." );
		}
		logger.info( "creating game " + pA.getName() + " vs " + pB.getName() );
		long byomi;
		int periods;
		if( pA.isLookingForBlitz() && pB.isLookingForBlitz() ) { // most common kgs blitz..
			byomi = 10000;
			periods = 3;
		} else if( pA.isLookingForFast() && pB.isLookingForFast() ) { // nhk cup times..
			byomi = 30000;
			periods = 20;
		} else {
			logger.severe( "pairing players who are not searching for game.." );
			return;
		}
		// create game..
		Game game;
		if( Math.random() < 0.5 ) {
			game = new Game( pA.getId() , pB.getId() , byomi , periods );
		} else {
			game = new Game( pB.getId() , pA.getId() , byomi , periods );
		}
		Storage.get().put( game );
		// update players..
		Player playerA = Storage.get().getPlayerById( pA.getId() );
		playerA.setActiveGame( game.getId() );
		playerA.setWatchingGame( 0 );
		playerA.setLastOpponent( pB.getId() );
		playerA.setActiveSearch( false , false );
		Storage.get().put( playerA );
		Player playerB = Storage.get().getPlayerById( pB.getId() );
		playerB.setActiveGame( game.getId() );
		playerB.setWatchingGame( 0 );
		playerB.setLastOpponent( pA.getId() );
		playerB.setActiveSearch( false , false );
		Storage.get().put( playerB );
		ChannelServlet.sendMessage( playerA.getId() , ChannelServlet.constructGameData( game ) );
		ChannelServlet.sendMessage( playerB.getId() , ChannelServlet.constructGameData( game ) );
	}

	public static void schedulePairing( long pUserId ) {
		TaskOptions options = TaskOptions.Builder.withUrl( "/rankingserver/pairing" );
		if( pUserId != 0 ) {
			options.param( "u" , Long.toString( pUserId ) );
		}
		options.method( Method.GET );
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add( options );
	}

	public static long getWaitTime( Player pA , Player pB ) { // should count somehow against uncertainty..
		if( !(pA.isLookingForBlitz() && pB.isLookingForBlitz()) && !(pA.isLookingForFast() && pB.isLookingForFast()) ) {
			return 10000000l;
		}
		double pr = HehRating.getProbability( pA.getRating() , pB.getRating() );
		double t = (0.5 - Math.abs( 0.5 - pr ));
		long required = (long) (1000l * (10 / (t * t) - 40));
		if( pA.getLastOpponent() == pB.getId() ) {
			required *= 2;
		}
		if( pB.getLastOpponent() == pA.getId() ) {
			required *= 2;
		}
		if( pA.getLastOpponent() == pB.getId() ) {
			required += 5000;
		}
		if( pB.getLastOpponent() == pA.getId() ) {
			required += 5000;
		}
		long time = System.currentTimeMillis();
		long aWait=(time - pA.getSearchStartTime());
		long bWait=(time - pB.getSearchStartTime());
		if( pA.isBot() ) {
			aWait/=4;
		}
		if( pB.isBot() ) {
			bWait/=4;
		}
		return (long) (required - (aWait + bWait));
	}

	private static boolean tryPairing( Player pPlayerA , Player pPlayerB ) {
		long time = System.currentTimeMillis();
		long waitTime = getWaitTime( pPlayerA , pPlayerB );
		if( waitTime < 0 ) {
			boolean pingRequired = false;
			long timeRequired=Math.max( time + waitTime , Math.max( pPlayerA.getSearchStartTime() , pPlayerB.getSearchStartTime() ));
			if( HeartBeat.getPing( pPlayerA.getId() ) < timeRequired ) {
				if( HeartBeat.getOutstandingRequest( pPlayerA.getId() ) == HeartBeat.sNoValue ) { // haven't pinged yet..
					HeartBeat.setOutstandingRequest( pPlayerA.getId() );
					ChannelServlet.sendMessage( pPlayerA.getId() , new PingMessage( ChannelServlet.generateMessageId() ) );
				}
				pingRequired = true;
			}
			if( HeartBeat.getPing( pPlayerB.getId() ) < timeRequired ) {
				if( HeartBeat.getOutstandingRequest( pPlayerB.getId() ) == HeartBeat.sNoValue ) { // haven't pinged yet..
					HeartBeat.setOutstandingRequest( pPlayerB.getId() );
					ChannelServlet.sendMessage( pPlayerB.getId() , new PingMessage( ChannelServlet.generateMessageId() ) );
				}
				pingRequired = true;
			}
			if( !pingRequired ) {
				createGame( pPlayerA , pPlayerB );
				return true;
			}
		}
		return false;
	}

	@Override
	protected void doGet( HttpServletRequest pReq , HttpServletResponse pResp ) throws ServletException , IOException {
		ArrayList<Player> players = Storage.get().listActiveSearch();
		if( pReq.getParameter( "u" ) != null ) {
			long userId=Long.parseLong( pReq.getParameter( "u" ) );
			Player player=Storage.get().getPlayerById( userId );
			for( Player opponent : players ) {
				if( opponent.getId() != player.getId() ) {
					if( tryPairing( player , opponent ) ) {
						break;
					}
				}
			}
		} else {
			if( players.size() != 0 ) {
				logger.info( "pairing from among " + players.size() + " players." );
			}
			long time=System.currentTimeMillis();
			for( Player playerI : players ) {
				for( Player playerJ : players ) {
					if( playerJ.getId() == playerI.getId() ) {
						break;
					}
					long waitTime = getWaitTime( playerI , playerJ );
					if( waitTime > 0 ) {
						continue;
					}
					if( tryPairing( playerI , playerJ ) ) {
						break;
					}
				}
			}
			
			for( Player playerI : players ) {
				
				long outstanding=HeartBeat.getOutstandingRequest( playerI.getId() );
				if( outstanding != HeartBeat.sNoValue && outstanding < time - 1l * 60l * 1000l ) { // outstanding for 20 seconds, ditch this guy..
					logger.warning( "no ack from user " + playerI.getName() );
					UserServlet.registerPairing( playerI.getId() , false , false );
					continue;
				}
			}
		}
	}

	static Logger logger = Logger.getLogger( "server" );

	
	public static void isAlive( long pUserId ) {
		HeartBeat.setPing( pUserId );
		Player player=Storage.get().getPlayerById( pUserId );
		if( player.getSearchStartTime() != 0 ) {
			schedulePairing( pUserId );
		}
	}
}
