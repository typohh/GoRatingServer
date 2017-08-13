package typo.ranking.server.server.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import typo.ranking.server.server.DeadListProvider;
import typo.ranking.server.server.Game;
import typo.ranking.server.server.HehRating;
import typo.ranking.server.server.HehRating.GlobalParameters;
import typo.ranking.server.server.Player;
import typo.ranking.server.server.Storage;
import typo.ranking.server.shared.BoardManagement;
import typo.ranking.server.shared.BoardState;
import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.Stone;
import typo.ranking.server.shared.messages.DeadListMessage;
import typo.ranking.server.shared.messages.GameUpdateMessage;

public class EndGameServlet extends HttpServlet {

	final static Logger logger = Logger.getLogger( "debug" );

	@Override
	protected void doGet( HttpServletRequest pReq , HttpServletResponse pResp ) throws ServletException , IOException {
		String keyString = pReq.getParameter( "key" );
		if( keyString == null ) { // sanity check games..
			ArrayList<Game> activeGames=Storage.get().listActiveGames();
			if( activeGames.size() > 0 ) {
				logger.info( "number of active games : " + activeGames.size() );
			}
			for( Game game : activeGames ) {
				if( game.isCompleted() ) {
					logger.warning( "active list contains compleated game " + game.getId() );
					continue;
				}
				if( !game.isOver() && game.isTimeLoss( System.currentTimeMillis() ) ) {
					logger.warning( "found orphan game, cleaning up... " + game.getId() );
					scheduleGameEnd( game );
				} else if( game.isOver() ) {
					logger.warning( "game " + game.getId() + " is over, but not completed yet." );
					scheduleGameEnd( game );
				}
			}
		} else {
			long gameId = Long.parseLong( keyString );
			logger.info( "completing game " + gameId );
			Game game = Storage.get().getGameById( gameId );
			if( game.isCompleted() ) {
				logger.info( "game " + gameId + " is already completed!" );
				return;
			}
			BoardManagement bm = (BoardManagement) Storage.get().getCache( "board" + gameId );
			if( bm == null || bm.getNumberOfMoves() != game.getNumberOfMoves() ) {
				if( bm != null ) {
					logger.severe( "wrong number of moves in cached board state." );
				}
				bm = DogFood.createBoard( game );
			}
			if( !bm.isOver() ) {
				if( game.isTimeLoss( System.currentTimeMillis() )) {
					game.setPlayerPeriods( game.getTurn() , 0 );
					game.addMove( Move.TimeLoss );
					GameUpdateMessage update = new GameUpdateMessage( ChannelServlet.generateMessageId() , game.getId() , game.getMoves().length - 1 , game.getLastMove() , game.getPlayerPeriods( game.getPlayerBlack() ) , game.getPlayerPeriods( game.getPlayerWhite() ) , 0 );
					ChannelServlet.updateWatchersAndPlayers( game , update );
					bm.play( Move.TimeLoss );
				} else {
					logger.severe( "asking to complete a game that is not yet over." );
					return;
				}
			}
			if( bm.isScoring() ) {
				ArrayList<Move> deadList = DeadListProvider.waitForDeadList( game );
				game.setDead( deadList );
				BoardState state = new BoardState( bm.getState() );
				for( Move move : deadList ) {
					state.set( move , Stone.Empty );
				}
				game.setScore( state.scoreChinese().getScore() );
				DeadListMessage update2 = new DeadListMessage( ChannelServlet.generateMessageId() , game.getId() , game.getScore() );
				for( Move move : deadList ) {
					update2.addDead( move );
				}
				ChannelServlet.updateWatchersAndPlayers( game , update2 );
				
			}
			game.setCompleted();
			Storage.get().put( game );
			Player winner = Storage.get().getPlayerById( game.getWinner() );
			Player loser = Storage.get().getPlayerById( game.getOpponent( winner.getId() ) );
			HehRating winnerR = winner.getRating();
			HehRating loserR = loser.getRating();
			
			GlobalParameters gp=getGlobalParameters();
			HehRating.update( winnerR , loserR , 1 , gp );
			Storage.get().putValue( "GlobalParameters" , gp );
			
			winner.setRating( winnerR );
			loser.setRating( loserR );
			winner.setActiveGame( 0 );
			loser.setActiveGame( 0 );
			Storage.get().put( winner );
			Storage.get().put( loser );
			double dailyGames = getDailyGames();
			dailyGames += 1;
			Storage.get().putValue( "LastGame" , System.currentTimeMillis() );
			Storage.get().putValue( "DailyGames" , dailyGames );
//			ArrayList<Long> recentGames = getRecentGames();
//			recentGames.add( 0 , gameId );
//			while( recentGames.size() > 100 ) {
//				recentGames.remove( recentGames.size() - 1 );
//			}
//			Storage.get().putValue( "RecentGames" , recentGames );
			ChannelServlet.sendMessage( winner.getId() , ChannelServlet.constructUserData( winner ) );
			ChannelServlet.sendMessage( loser.getId() , ChannelServlet.constructUserData( loser ) );
			// ArrayList<Long> activeGames=getActiveGameList(); // not totaly safe this one..
			// activeGames.remove( game.getId() );
			// Storage.get().putValue( "ActiveGames" , activeGames );
		}
	}

	// public static ArrayList<Long> getActiveGameList() {
	// @SuppressWarnings("unchecked")
	// ArrayList<Long> activeGames=(ArrayList<Long>) Storage.get().getValue( "ActiveGames" );
	// if( activeGames == null ) {
	// return new ArrayList<Long>();
	// }
	// return activeGames;
	// }
	public static void scheduleGameEnd( Game pGame ) {
		if( pGame.isCompleted() ) {
			return;
		}
		TaskOptions options = TaskOptions.Builder.withUrl( "/rankingserver/endgame" );
		options.method( Method.GET );
		options.param( "key" , "" + pGame.getId() );
		QueueFactory.getDefaultQueue().add( options );
	}

	public static HehRating.GlobalParameters getGlobalParameters() {
		HehRating.GlobalParameters gp=(GlobalParameters) Storage.get().getValue( "GlobalParameters" );
		if( gp == null ) {
			gp=new HehRating.GlobalParameters( 30 );
			for( Player player : Storage.get().listPlayers() ) {
				gp.add( player.getRating().getMean() , player.getRating().getWeight() );
			}
		}
		return gp;
	}
	
	public static void scheduleCleanup() {
		TaskOptions options = TaskOptions.Builder.withUrl( "/rankingserver/endgame" );
		options.method( Method.GET );
		try {
			QueueFactory.getDefaultQueue().add( options );
		} catch( TaskAlreadyExistsException pE ) {
		}
	}

	public static double getDailyGames() {
		long lastGame = Storage.convertLong( Storage.get().getValue( "LastGame" ) );
		double dailyGames = Storage.convertDouble( Storage.get().getValue( "DailyGames" ) );
		if( Double.isNaN( dailyGames ) ) {
			dailyGames = 0;
		}
		long time = System.currentTimeMillis();
		dailyGames *= Math.pow( 0.5 , (time - lastGame) / (12d * 60l * 60l * 1000l) );
		return dailyGames;
	}

}
