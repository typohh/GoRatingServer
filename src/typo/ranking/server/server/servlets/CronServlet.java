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

import typo.ranking.server.server.Storage;

public class CronServlet extends HttpServlet {

	
	public boolean performOneTimeMagic() {
		String oneTimeMagic=(String) Storage.get().getValue( "oneTimeMagic" );
		if( oneTimeMagic != null && oneTimeMagic.equals( "1" )) {
			Storage.get().putValue( "oneTimeMagic" , "2" );
			return true;
		}
		return false;
	}
	
	Logger logger = Logger.getLogger( "debug" );
	@Override
	protected void service( HttpServletRequest pRequest , HttpServletResponse pResponse ) throws ServletException , IOException {
		PairingServlet.schedulePairing( 0 );
		EndGameServlet.scheduleCleanup();
		String header=pRequest.getHeader( "X-Appengine-Cron" );
		if( header != null && header.equals( "true" )) {
			for( int i=1 ; i<12 ; ++i ) {
				TaskOptions options = TaskOptions.Builder.withUrl( "/rankingserver/cron" );
				options.method( Method.GET ).etaMillis( System.currentTimeMillis() + i * 5 * 1000 );
				Queue queue = QueueFactory.getDefaultQueue();
				queue.add( options );
			}
		}
//		if( performOneTimeMagic() ) {
//			Query q = new Query( "Player" ).setKeysOnly().addSort( "RatingMean" , SortDirection.DESCENDING );
//			PreparedQuery pq=Storage.get().doQuery( q );
//			for( Entity e : pq.asIterable() ) {
//				Player player=Storage.get().getPlayerById( e.getKey().getId() );
//				if( player.getNumberOfGames() < 200 ) {
//					updatePlayer( player );
//				}
//			}
//		}
	}

//	private void updatePlayer( Player pPlayer ) {
//		int thisWeek=0;
//		int lastWeek=0;
//		
//		long thisWeekBegins=Player.getThisWeekBegins( System.currentTimeMillis() );
//		long lastWeekBegins=thisWeekBegins - Player.sWeek;
//		{
//			Query q = new Query( "Game" ).setKeysOnly();
//			Filter propertyFilter = new FilterPredicate( "PlayerBlack" , FilterOperator.EQUAL , KeyFactory.createKey( "Player" , pPlayer.getId() ) );
//			q.setFilter( propertyFilter );
//			PreparedQuery pq=Storage.get().doQuery( q );
//			for( Entity e : pq.asIterable() ) {
//				Game game=Storage.get().getGameById( e.getKey().getId() );
//				if( game.getDateCreated() > thisWeekBegins ) {
//					++thisWeek;
//				} else if( game.getDateCreated() > lastWeekBegins ) {
//					++lastWeek;
//				}
//			}
//		}
//		{
//			Query q = new Query( "Game" ).setKeysOnly();
//			Filter propertyFilter = new FilterPredicate( "PlayerWhite" , FilterOperator.EQUAL , KeyFactory.createKey( "Player" , pPlayer.getId() ) );
//			q.setFilter( propertyFilter );
//			PreparedQuery pq=Storage.get().doQuery( q );
//			for( Entity e : pq.asIterable() ) {
//				Game game=Storage.get().getGameById( e.getKey().getId() );
//				if( game.getDateCreated() > thisWeekBegins ) {
//					++thisWeek;
//				} else if( game.getDateCreated() > lastWeekBegins ) {
//					++lastWeek;
//				}
//			}
//		}
//		if( pPlayer.getDateLastGame() > thisWeekBegins ) {
//			pPlayer.fixGamesPerWeek( thisWeek , lastWeek );
//		} else if( pPlayer.getDateLastGame() > lastWeekBegins ) {
//			pPlayer.fixGamesPerWeek( lastWeek , 0 );
//		}
//		Storage.get().put( pPlayer );
//		
//		logger.info( "updated " + pPlayer.getName() + " to " + thisWeek + " & " + lastWeek );
//	}
	
}
