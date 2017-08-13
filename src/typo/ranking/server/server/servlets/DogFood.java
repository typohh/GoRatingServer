package typo.ranking.server.server.servlets;

import java.util.ArrayList;
import java.util.logging.Logger;

import typo.ranking.server.server.Game;
import typo.ranking.server.server.Player;
import typo.ranking.server.server.Storage;
import typo.ranking.server.shared.BoardManagement;
import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.OldVersionException;
import typo.ranking.server.shared.messages.DisconnectMessage;
import typo.ranking.server.shared.messages.GameListMessage;
import typo.ranking.server.shared.messages.GameUpdateMessage;
import typo.ranking.server.shared.messages.LeaderBoardMessage;

public class DogFood {

	private static Logger logger = Logger.getLogger( "debug" );

	public static BoardManagement createBoard( Game pGame ) {
		BoardManagement bm = new BoardManagement( 19 );
		for( Move move : pGame.getMoves() ) {
			if( !bm.play( move ) ) {
				throw new Error( "how can this be! " + move );
			}
		}
		return bm;
	}

	public static void setBoard( Game pGame , BoardManagement pBoard ) {
		Storage.get().putCache( "board" + pGame.getId() , pBoard );
	}

	public static void changeName( long pUserId ) {
		UserServlet.changeName( pUserId );
	}

	public static void sendMove( long pUserId , short pMoveNumber , Move pMove ) {
		Player player = Storage.get().getPlayerById( pUserId );
		long gameId = player.getActiveGame();
		Game game = Storage.get().getGameById( gameId );
		if( game == null ) {
			logger.severe( "player " + player.getName() + " sending move with no active game." );
			return; // probably time loss...
		}
		if( game.getTurn() != player.getId() && game.getLastMove() == Move.TimeLoss ) {
			return;
		}
		if( game.getTurn() != player.getId() ) {
			throw new Error( "playing out of turn" );
		}
		if( game.getMoves().length != pMoveNumber ) {
			throw new Error( "trying to send move : " + pMoveNumber + "=" + pMove + " when new move would be " + game.getMoves().length );
		}
		if( pMove == Move.Outside ) {
			throw new Error( "wtf, client sending " + pMove );
		}
		long time = System.currentTimeMillis();
		if( game.isTimeLoss( time ) ) {
			EndGameServlet.scheduleGameEnd( game );
			return;
		}
		BoardManagement bm = (BoardManagement) Storage.get().getCache( "board" + gameId );
		if( bm == null || bm.getNumberOfMoves() != game.getNumberOfMoves() ) {
			if( bm != null ) {
				logger.severe( "wrong number of moves in cached board state." );
			}
			bm = DogFood.createBoard( game );
		}
		if( !bm.play( pMove ) ) {
			throw new Error( "illegal move by " + player.getName() + " at move " + pMoveNumber + "=" + pMove.toJson() + " in game " + game.getId() + " super ko violation " + bm.wasSuperKoViolation() );
		}
		Storage.get().putCache( "board" + gameId , bm );
		if( pMove != Move.TimeLoss && pMove != Move.Resign ) {
			long timeSinceLastMove = time - game.getDateLastMove();
			int lostPeriods = (int) (timeSinceLastMove / game.getPeriodTime());
			if( lostPeriods > 0 ) { // we determined earlier its not a time loss, so lets accept it got in time..
				game.setPlayerPeriods( player.getId() , Math.max( 1 , game.getPlayerPeriods( player.getId() ) - lostPeriods ) );
			}
		}
		game.setDateLastMove( time );
		game.addMove( pMove );
		Storage.get().put( game );

		GameUpdateMessage update = new GameUpdateMessage( ChannelServlet.generateMessageId() , game.getId() , game.getMoves().length - 1 , game.getLastMove() , game.getPlayerPeriods( game.getPlayerBlack() ) , game.getPlayerPeriods( game.getPlayerWhite() ) , 0 );
		ChannelServlet.updateWatchersAndPlayers( game , update );
		
		if( game.isOver() ) { // apparently it was last move, schedule for completion
			EndGameServlet.scheduleGameEnd( game );
			return;
		}
	}

	public static void registerForPairing( long pUserId , boolean pBlitz , boolean pFast ) {
		UserServlet.registerPairing( pUserId , pBlitz , pFast );
	}

	public static GameListMessage getGameData() {
		ArrayList<Game> activeGames = Storage.get().listActiveGames();
		GameListMessage result = new GameListMessage( ChannelServlet.generateMessageId() );
		for( Game game : activeGames ) {
			Player playerBlack = Storage.get().getPlayerById( game.getPlayerBlack() );
			Player playerWhite = Storage.get().getPlayerById( game.getPlayerWhite() );
			if( playerBlack == null || playerWhite == null ) {
				throw new Error( "missing player " + (playerBlack != null) + " " + (playerWhite != null) );
			}
			result.addGame( game.getId() , playerBlack.getName() , playerWhite.getName() , playerBlack.getRating().getRating() , playerWhite.getRating().getRating() , game.getMoves().length , (int) game.getPeriodTime() / 1000 );
		}
		result.sort();
		return result;
	}

	public static LeaderBoardMessage getLeaderboard() {
		LeaderBoardMessage lbm = new LeaderBoardMessage( ChannelServlet.generateMessageId() );
		int position = 1;
		long time = System.currentTimeMillis();
		for( Player player : Storage.get().listPlayers() ) {
			try {
				if( player.getGamesPerWeek( time ) / 7 > 0.1 && player.getNumberOfGames() > 2 ) {
					lbm.add( position , player.getName() , player.getRating().getRating() , player.getGamesPerWeek( time ) );
					++position;
				}
			} catch( Error pE ) {
				logger.severe( "failed to create leaderboard entry for " + player.getName() + " because " + pE.getMessage() );
			}
		}
		return lbm;
	}

	public static void registerWatchGame( long pUserId , long pGameId ) {
		UserServlet.registerWatching( pUserId , pGameId );
	}

	public static String getChannelSecret( long pUserId , long pChannelId , boolean pBot ) throws OldVersionException {
		if( pUserId == 0 ) { // construct new user..
			throw new Error();
		}
		ChannelServlet.sendMessage( pUserId , new DisconnectMessage( ChannelServlet.generateMessageId() , pChannelId ) );
		Player player = Storage.get().getPlayerById( pUserId );
		if( player == null ) { // just make sure the user exists before creating channel..
			throw new Error();
		}
		ChannelServlet.setChannelId( player.getId() , pChannelId );
		UserServlet.registerChannel( player.getId() , pChannelId , pBot );
		return ChannelServlet.getChannelSecret( pChannelId );
	}

	public static void ack( long pUserId , long pMessageId ) {
		// logger.severe( "received ack by " + pUserId + " for " + pMessageId );
		PairingServlet.isAlive( pUserId );
		ChannelServlet.receivedAck( pUserId , pMessageId );
	}

	public static void initialize( long pUserId ) {
		Player player = Storage.get().getPlayerById( pUserId );
		ChannelServlet.sendMessage( pUserId , ChannelServlet.constructUserData( player ) );
		if( player.getActiveGame() != 0 ) {
			ChannelServlet.sendMessage( pUserId , ChannelServlet.constructGameData( Storage.get().getGameById( player.getActiveGame() ) ) );
		} else if( player.getWatchingGame() != 0 ) {
			Game game=Storage.get().getGameById( player.getWatchingGame() );
			ChannelServlet.sendMessage( pUserId , ChannelServlet.constructGameData( game ) );
			UserServlet.setPlayersLastMoveWatched( player.getId() , game.getId() , game.getNumberOfMoves() );
		}
	}

	public static long createUserId() {
		Player player = UserServlet.createUser();
		return player.getId();
	}
}
