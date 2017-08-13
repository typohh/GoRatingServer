package typo.ranking.server.client;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.user.client.Window;

import typo.ranking.server.shared.BoardManagement;
import typo.ranking.server.shared.BoardState;
import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.Rating;
import typo.ranking.server.shared.Stone;
import typo.ranking.server.shared.Version;
import typo.ranking.server.shared.messages.DeadListMessage;
import typo.ranking.server.shared.messages.GameDataMessage;
import typo.ranking.server.shared.messages.GameListMessage;
import typo.ranking.server.shared.messages.GameUpdateMessage;
import typo.ranking.server.shared.messages.LeaderBoardMessage;
import typo.ranking.server.shared.messages.UserDataMessage;

public abstract class ClientState {

	private UserDataMessage mUserData;
	private BoardState mTerritoryMap;
	private GameDataMessage mGameData;
	private BoardManagement mGameManagement = new BoardManagement( 19 );
	private int mRepeatFailure;
	private GameListMessage mActiveGames = null;

	private GameDataMessage mGameDataInactive=null;
	
	private boolean activeGame( long pGameId ) {
		if( mGameData != null && mGameData.getGameId() == pGameId ) {
			return true;
		}
		if( mGameDataInactive != null && mGameDataInactive.getGameId() == pGameId ) {
			GameDataMessage tmp=mGameData;
			mGameData=mGameDataInactive;
			mGameDataInactive=tmp;
			return true;
		}
		return false;
	}
	
	private ArrayList<JavaScriptMessage> mUnProcessed=new ArrayList<JavaScriptMessage>();
	
	private void handleInput( String pMessage ) {
		JavaScriptMessage jsm=JsonUtils.safeEval( pMessage );
		mRest.ack( mUserId , jsm.getMessageId() );
		if( handleInput( jsm )) {
			for( int i=0 ; i<mUnProcessed.size() ; ++i ) {
				JavaScriptMessage j=mUnProcessed.get( i );
				if( handleInput( j )) {
					mUnProcessed.remove( i );
					i=-1; // start it all over since we managed to process 1 message..
				}
			}
		} else {
			mUnProcessed.add( jsm );
		}
		if( !mUnProcessed.isEmpty() ) {
			Util.log( Level.INFO , mUnProcessed.size() + " messages still pending processing." );
		}
	}
	
	private boolean handleInput( JavaScriptMessage message ) {
		if( !message.getType().equals( "userdata" ) && mUserData == null ) { // only accept user data as first message..
			Util.log( Level.WARNING , "received " + message.getType() + " before userdata" );
			return false;
		}
		if( message.getType().equals( "disconnect" ) ) {
			if( mChannelId != message.getActive() ) {
				Util.log( Level.INFO , "client relogged elsewhere." );
				mUserData = null;
				mGameData = null;
				mGameManagement.reset();
				mTerritoryMap = null;
				mChannel.close();
				mChannelId = 0;
				disconnected();
				// logged in elsewhere..
			}
			return true;
		}
		if( message.getType().equals( "gameupdate" ) ) {
			if( !activeGame( message.getGameId() )) { // we cant accept it yet..
				return false;
			}
			GameUpdateMessage update = new GameUpdateMessage( message.getMessageId() , message.getGameId() , message.getMoveNumber() , message.getMove() , message.getBlackPeriods() , message.getWhitePeriods() , message.getTimeSinceLastMove() );
			if( update.getMoveNumber() > mGameData.getNumberOfMoves() ) {
				return false;
			}
			if( update.getMoveNumber() < mGameData.getNumberOfMoves() ) {
				Util.log( Level.WARNING , "discarding move " + update.getMoveNumber() + " because already at move " + mGameData.getNumberOfMoves() );
				return true;
			}
			boolean ownMove = mGameManagement.getNumberOfMoves() == mGameData.getNumberOfMoves() + 1;
			if( ownMove && update.getMove() == Move.TimeLoss ) {
				mGameManagement.reset();
			}
			mGameData.apply( update );
			processGameData();
			if( !ownMove || update.getMove() == Move.TimeLoss ) {
				updateAction( update.getMove() , isPlaying() && (isBlacksTurn() != isPlayerBlack()) );
			}
			return true;
		}
		if( message.getType().equals( "deadlist" ) ) {
			if( !activeGame( message.getGameId() ) ) {
				return false;
			}
			DeadListMessage deadList = new DeadListMessage( message.getMessageId() , message.getGameId() , message.getScore() );
			for( int i = 0 ; i < message.getNumberOfDead() ; ++i ) {
				deadList.addDead( message.getDead( i ) );
			}
			mGameData.apply( deadList );
			processGameData();
			return true;
		}
		if( message.getType().equals( "userdata" ) ) {
			mUserData = new UserDataMessage( message.getMessageId() , message.getName() , message.getUserId() , message.getRating() , message.getNumberOfGames() , message.getBlitz() , message.getFast() );
			mUserId = mUserData.getUserId();
			if( mUserData.getUserId() != mUserId ) {
				throw new Error( "whaat, different user id from the one we created the secret with." );
			}
			Util.put( "uid" , Long.toString( mUserData.getUserId() ) );
			update();
			return true;
		}
		if( message.getType().equals( "gamelist" ) ) {
			mActiveGames = new GameListMessage( message.getMessageId() );
			JsArray<JavaScriptMessage> games=message.getGames();
			for( int i=0 ; i<games.length() ; ++i ) {
				JavaScriptMessage game=games.get( i );
				mActiveGames.addGame( game.getGameId(), game.getBlackName(), game.getWhiteName(), game.getBlackRating() , game.getWhiteRating(), game.getNumberOfMoves() , game.getTimePerPeriod() );
			}
			updateGameList();
			return true;
		}
		if( message.getType().equals( "leaderboard" )) {
			LeaderBoardMessage lbm=new LeaderBoardMessage( message.getMessageId() );
			JsArray<JavaScriptMessage> players=message.getPlayers();
			for( int i=0 ; i<players.length() ; ++i ) {
				JavaScriptMessage player=players.get( i );
				lbm.add( player.getPosition() , player.getName() , player.getRating() , player.getRecentGames() );
			}
			updateLeaderboard( lbm );
			return true;
		}
		if( message.getType().equals( "gamedata" ) ) {
			mGameManagement.reset();
			if( !activeGame( message.getGameId() )) {
				mGameDataInactive=mGameData;
				mGameData = new GameDataMessage( message.getMessageId() );
				mGameData.setDateCreated( message.getDateCreated() );
				mGameData.setGameId( message.getGameId() );
				mGameData.getPlayer( true ).set( message.getBlackName() , message.getBlackRating() , message.getBlackPeriods() );
				mGameData.getPlayer( false ).set( message.getWhiteName() , message.getWhiteRating() , message.getWhitePeriods() );
				mGameData.setTimePerPeriod( message.getTimePerPeriod() );
				mGameData.setTimeSinceLastMove( message.getTimeSinceLastMove() );
			} else if( message.getMovesLength() > mGameData.getNumberOfMoves() ) { // never information than we had..
				mGameData.setTimeSinceLastMove( message.getTimeSinceLastMove() );
			}
			for( int i = mGameData.getNumberOfMoves() ; i < message.getMovesLength() ; ++i ) {
				mGameData.addMove( message.getMove( i ) );
			}
			if( message.hasDead() ) {
				mGameData.setHasDead();
				for( int i = 0 ; i < message.getNumberOfDead() ; ++i ) {
					mGameData.addDead( message.getDead( i ) );
				}
			}
			if( isPlaying() ) { // we know server disabled all searches the moment we started a game..
				mUserData.setSearch( false , false );
			}
			processGameData();
			gameLoaded();
			return true;
		}
		if( message.getType().equals( "ping" ) ) {
			return true;
		}
		Util.log( Level.SEVERE , "unknown type of message : " + message.getType() );
		throw new Error( "lets crash this party");
	}

	private void sendMove() {
		mRest.sendMove( mUserData.getUserId() , (short) (mGameManagement.getNumberOfMoves() - 1) , mGameManagement.getLastMove() );
	}

	private void processGameData() {
		for( int i = mGameManagement.getNumberOfMoves() ; i < mGameData.getNumberOfMoves() ; ++i ) {
			if( !mGameManagement.play( mGameData.getMove( i ) ) ) {
				throw new Error( "illegal move " + mGameData.getMove( i ) );
			}
		}
		if( mGameManagement.isScoring() ) {
			if( mGameData.hasDead() ) {
				if( mTerritoryMap == null ) {
					BoardState bs = new BoardState( mGameManagement.getState() );
					for( Move move : mGameData.getDeadStones() ) {
						bs.set( move , Stone.Empty );
					}
					mTerritoryMap = bs.scoreChinese();
				}
			}
		} else {
			mTerritoryMap = null;
		}
		update();
	}

	// state callbacks..
	public abstract void newVersionAvailable();

	public abstract void update();

	public abstract void updateGameList();

	public abstract void updateAction( Move pMove , boolean pByPlayer );

	public abstract void reconnected();

	public abstract void disconnected();

	public abstract void gameLoaded();

	public abstract void updateLeaderboard( LeaderBoardMessage pList );

	public abstract void initialized();

	public abstract void errorOccured();
	
	// state interface..
	public void fetchActiveGames() {
		mRest.getGameData();
	}

	public int getNumberOfActiveGames() {
		return mActiveGames.getNumberOfGames();
	}

	public boolean playMove( Move pMove ) {
		if( pMove == null ) {
			throw new Error( "null move, wtf");
		}
		if( !isPlayersTurn() ) {
			throw new Error( "how can this be, playing out of turn." );
		}
		if( !mGameManagement.play( pMove ) ) { // illegal move, cancel all actions..
			return false;
		}
		sendMove();
		if( pMove == Move.TimeLoss ) {
			mGameData.getPlayer( isPlayerBlack() ).set( mGameData.getPlayer( isPlayerBlack() ).getName() , mGameData.getPlayer( isPlayerBlack() ).getRating() , 0 );
		}
		update();
		updateAction( pMove , true );
		return true;
	}

	public boolean isPlayerBlack() {
		return mUserData.getName().equals( mGameData.getPlayer( true ).getName() );
	}

	public int getNumberOfMoves() {
		return mGameManagement.getNumberOfMoves();
	}

	public GameListMessage.GameStats getActiveGame( int pIndex ) {
		return mActiveGames.getGame( pIndex );
	}

	public void loadGame( int pIndex ) {
		mRest.registerWatchGame( mUserData.getUserId() , mActiveGames.getGame( pIndex ).getGameId() );
	}

	public void setSearching( boolean pBlitz , boolean pFast ) {
		if( mUserData == null ) {
			throw new Error( "cant search with no user dumbass" );
		}
		if( isGameInProgress() && isPlaying() ) {
			return; // cant do this now..
		}
		mUserData.setSearch( pBlitz , pFast );
		mRest.registerForPairing( mUserData.getUserId() , pBlitz , pFast );
	}

	public boolean isPlayersTurn() {
		if( !isGameInProgress() || !isPlaying() ) {
			return false;
		}
		return isPlayerBlack() == isBlacksTurn();
	}

	public boolean isScoring() {
		return mGameManagement.isScoring();
	}

	public boolean isOver() {
		return mGameManagement.isOver();
	}

	public void reset() {
		if( !isGameInProgress() || !isPlaying() ) { // was watching, lets stop with the updates..
			mRest.registerWatchGame( mUserData.getUserId() , 0 );
		}
		mGameData = null;
		mGameManagement.reset();
		mTerritoryMap = null;
		update();
	}

	public void randomizeName() {
		mRest.changeName( mUserData.getUserId() );
	}

	public boolean isSearchingBlitz() {
		return mUserData.isSearchingBlitz();
	}

	public boolean isSearchingFast() {
		return mUserData.isSearchingFast();
	}

	public boolean isConnected() {
		return mUserData != null;
	}

	public long getGameId() {
		return mGameData.getGameId();
	}

	public Move getLastMove() {
		return mGameManagement.getLastMove();
	}

	public boolean isPlaying() {
		if( !isGameInProgress() ) {
			throw new Error( "asking if playing when no game, wtf!" );
		}
		return mUserData != null && mGameData != null && (mUserData.getName().equals( mGameData.getPlayer( true ).getName() ) || mUserData.getName().equals( mGameData.getPlayer( false ).getName() ));
	}

	public boolean isGameInProgress() {
		return mGameData != null;
	}

	public Stone getStone( Move pMove ) {
		return mGameManagement.getStone( pMove );
	}

	public Stone getTerritory( Move pMove ) {
		if( mTerritoryMap != null ) {
			return mTerritoryMap.get( pMove );
		} else {
			return Stone.Empty;
		}
	}

	public int getNumberOfGames() {
		if( mUserData != null ) {
			return mUserData.getNumberOfGames();
		} else {
			return 0;
		}
	}

	public long getUserId() {
		if( mUserId != 0 ) {
			return mUserId;
		}
		String useridText = Util.get( "uid" );
		long userid = 0;
		if( useridText != null ) {
			userid = Long.parseLong( useridText );
		}
		if( Window.Location.getQueryString().startsWith( "?p=" ) ) {
			userid = Long.parseLong( Window.Location.getQueryString().substring( 3 ) );
		}
		return userid;
	}

	public String getUserName() {
		return mUserData.getName();
	}

	public Rating getUserRating() {
		return mUserData.getRating();
	}

	public int getUserNumberOfGames() {
		return mUserData.getNumberOfGames();
	}

	public boolean isBlacksTurn() {
		return getNumberOfMoves() % 2 == 0;
	}

	public double getScore() {
		if( !isDeadListAvailable() ) {
			throw new Error( "asking for score when dont have list of dead stones");
		}
		return mTerritoryMap.getScore();
	}

	public boolean isDeadListAvailable() {
		return mTerritoryMap != null;
	}

	public String getName( boolean pBlackNotWhite ) {
		return mGameData.getPlayer( pBlackNotWhite ).getName();
	}

	public Rating getRating( boolean pBlackNotWhite ) {
		return mGameData.getPlayer( pBlackNotWhite ).getRating();
	}

	public int getCaptures( boolean pBlackNotWhite ) {
		return mGameManagement.getCaptures( pBlackNotWhite );
	}

	public long getLastMoveTime() {
		return mGameData.getLastMoveTime();
	}

	public int getPeriods( boolean pBlackNotWhite ) {
		return mGameData.getPlayer( pBlackNotWhite ).getPeriods();
	}

	public long getTimePerPeriod() {
		return mGameData.getTimePerPeriod();
	}

	public void fetchLeaderboard() {
		mRest.getLeaderboard();
	}

	private Rest mRest=new Rest() {

		@Override
		public void responseReceived( RequestType pType , String pMessage ) {
			if( pType == RequestType.getChannelSecret ) {
				mChannel.join( pMessage );
			} else if( pType == RequestType.createUserId ) {
				mUserId = Long.parseLong( pMessage );
				initialized();
				requestSecret();
			} else {
				handleInput( pMessage );
			}
			
		}

		@Override
		public void errorReceived( int pStatusCode , String pResponse ) {
			if( pStatusCode == 402 && pResponse.equals( "Old version of protocol." ) ) {
				newVersionAvailable();
				return;
			}
			if( pStatusCode == 500 ) { 
				// something went wrong inside server..
			} else {
			}
			Util.log( Level.SEVERE , pStatusCode + " : " + pResponse );
			errorOccured();
		}

		@Override
		public void disconnected() {
			ClientState.this.disconnected();
		}

		@Override
		public void reconnected() {
			ClientState.this.reconnected();
		}
		
	};
	private Channel mChannel = new Channel() {

		@Override
		public void onOpen() {
			if( mUserData == null ) {
				mRest.initialize( mUserId );
			}
		}

		@Override
		public void onMessage( String pMessage ) {
			handleInput( pMessage );
		}

		@Override
		public void onError( int pCode , String pDescription ) {
			if( pCode == 401 ) {
				Util.log( Level.INFO , "client side reconnecting" );
			} else {
				Util.log( Level.SEVERE , "client side channel error " + pCode + " : " + pDescription );
			}
			requestSecret();
		}

		@Override
		public void onClose() {
			// Util.log( Level.INFO , "client side channel closed." );
			// requestSecret();
		}
	};
	private long mChannelId;
	private long mUserId;

	private void requestSecret() {
		if( mChannelId != 0 ) {
			mChannel.close();
		}
		mChannelId = new Random().nextLong();
		mRest.getChannelSecret( mUserId , mChannelId , Version.sVersionHuman );
	}

	public void initialize() {
		mUserId = getUserId();
		if( mUserId == 0 ) {
			mRest.createUserId();
		} else {
			initialized();
			requestSecret();
		}
	}

	public int getNumberOfPeriods() {
		return mGameData.getPlayer( isPlayerBlack() ).getPeriods();
	}
	
}
