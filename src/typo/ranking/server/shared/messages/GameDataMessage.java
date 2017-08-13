package typo.ranking.server.shared.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import typo.ranking.server.shared.JsonWriter;
import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.Rating;

public class GameDataMessage implements Serializable , Message {

	public static class PlayerData implements Serializable {

		private PlayerData() { }
		
		private String mName;
		private Rating mRating;
		private int mPeriods;

		public void set( String pName , Rating pRating , int pPeriods ) {
			mName=pName;
			mRating=pRating;
			mPeriods=pPeriods;
		}

		public String getName() {
			return mName;
		}
		
		public Rating getRating() {
			return mRating;
		}
		
		public int getPeriods() {
			return mPeriods;
		}
		
//		public String toJson() {
//			JsonWriter jw=new JsonWriter();
//			jw.add( "name" , mName );
//			jw.add( "ratingMean" , mRating.getMean() );
//			jw.add( "ratingSD" , mRating.getSD() );
//			jw.add( "periods" , mPeriods );
//			return jw.toString();
//		}
//		
	}

	private long mMessageId;
	private long mDateCreated;
	private long mGameId;
	private ArrayList<Move> mMoves=new ArrayList<Move>();
	private PlayerData mBlack=new PlayerData();
	private PlayerData mWhite=new PlayerData();
	private long mTimePerPeriod;
	private ArrayList<Move> mDeadStones=new ArrayList<Move>();
	private boolean mHasDead=false;
	private int mTimeSinceLastMove;
	
	
	private transient long mLastMoveTime;
	
	@Override
	public String toJson() {
		JsonWriter jw=new JsonWriter();
		jw.add( "type" , "gamedata" );
		jw.add( "messageId" , Long.toString( mMessageId ) );
		jw.add( "gameId" , Long.toString( mGameId ) );
		jw.add( "dateCreated" , Long.toString( mDateCreated ) );
		jw.add( "blackName" , mBlack.mName );
		jw.add( "blackRatingMean" , mBlack.mRating.getMean() );
		jw.add( "blackRatingSD" , mBlack.mRating.getSD() );
		jw.add( "blackPeriods" , mBlack.mPeriods );
		jw.add( "whiteName" , mWhite.mName );
		jw.add( "whiteRatingMean" , mWhite.mRating.getMean() );
		jw.add( "whiteRatingSD" , mWhite.mRating.getSD() );
		jw.add( "whitePeriods" , mWhite.mPeriods );
		jw.add( "timePerPeriod" , (int)mTimePerPeriod );
		jw.add( "timeSinceLastMove" , mTimeSinceLastMove );
		Vector<String> moves=new Vector<String>();
		for( Move m : mMoves ) {
			moves.add( m.toJson() );
		}
		jw.add( "moves" , moves );
		Vector<String> dead=new Vector<String>();
		for( Move m : mDeadStones ) {
			dead.add( m.toJson() );
		}
		jw.add( "dead" , dead );
		jw.add( "hasDead" , mHasDead );
		return jw.toString();
	}
	
	private GameDataMessage() {
	}
	
	public GameDataMessage( long pMesssageId ) {
		mMessageId=pMesssageId;
	}
	
	public PlayerData getPlayer( boolean pBlackNotWhite ) {
		if( pBlackNotWhite ) {
			return mBlack;
		} else {
			return mWhite;
		}
	}
	
	public void setHasDead() {
		mHasDead=true;
	}
	
	public void addDead( Move pMove ) {
		mHasDead=true;
		mDeadStones.add( pMove );
	}
	
	public Collection<Move> getDeadStones() {
		return mDeadStones;
	}
	
	public void setTimeSinceLastMove( int pTime ) {
		mTimeSinceLastMove=pTime;
		mLastMoveTime=System.currentTimeMillis() - pTime;
	}
	
	public boolean hasDead() {
		return mHasDead;
	}
	
	public void addMove( Move pMove ) {
		mMoves.add( pMove );
	}
	
	public void setDateCreated( long pDateCreated ) {
		mDateCreated=pDateCreated;
	}
	
	public void setTimePerPeriod( long pTimePerPeriod ) {
		mTimePerPeriod=pTimePerPeriod;
	}

	public long getTimePerPeriod() {
		return mTimePerPeriod;
	}
	
	public int getNumberOfMoves() {
		return mMoves.size();
	}
	
	public long getLastMoveTime() {
		return mLastMoveTime;
	}
	
	public void apply( GameUpdateMessage pUpdate ) {
		if( pUpdate.getGameId() != mGameId ) {
			throw new Error( "wrong game id in update " + pUpdate.getGameId() + " vs " + mGameId );
		}
		if( pUpdate.getMoveNumber() != getNumberOfMoves() ) {
			throw new Error( "out of sync game update, received " + pUpdate.getMoveNumber() + " when needed " + getNumberOfMoves() );
		}
		mMoves.add( pUpdate.getMove() );
		mBlack.mPeriods=pUpdate.getPeriodsBlack();
		mWhite.mPeriods=pUpdate.getPeriodsWhite();
		setTimeSinceLastMove( pUpdate.getTimeSinceLastMove() );
	}

	public void apply( DeadListMessage pDeadList ) {
		mHasDead=true;
		for( Move move : pDeadList.getMoves() ) {
			addDead( move );
		}
	}
	
	public Move getMove( int pIndex ) {
		return mMoves.get( pIndex );
	}

	public boolean getTurn() {
		return mMoves.size() % 2 == 0;
	}

	public void setGameId( long pGameId ) {
		mGameId=pGameId;
	}

	public long getGameId() {
		return mGameId;
	}

	@Override
	public long getMessageId() {
		return mMessageId;
	}
	
	public long getTimeSinceLastMove() {
		return mTimeSinceLastMove;
	}

	public Move getLastMove() {
		if( !mMoves.isEmpty() ) {
			return mMoves.get( mMoves.size() - 1 );
		}
		return null;
	}
	
}
