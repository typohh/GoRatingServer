package typo.ranking.server.shared.messages;

import java.io.Serializable;

import typo.ranking.server.shared.JsonWriter;
import typo.ranking.server.shared.Move;

public class GameUpdateMessage implements Serializable , Message {

	private GameUpdateMessage() {}

	private long mMessageId;
	private long mGameId;
	private short mMoveNumber;
	private Move mMove;
	private int mBlackPeriods;
	private int mWhitePeriods;
	private int mTimeSinceLastMove;

	public GameUpdateMessage( long pMessageId , long pGameId , int pMoveNumber , Move pMove , int pBlackPeriods , int pWhitePeriods , int pTimeSinceLastMove ) {
		mMessageId=pMessageId;
		mGameId=pGameId;
		mMoveNumber = (short) pMoveNumber;
		mMove = pMove;
		mBlackPeriods = pBlackPeriods;
		mWhitePeriods = pWhitePeriods;
		mTimeSinceLastMove=pTimeSinceLastMove;
	}

	public long getGameId() {
		return mGameId;
	}

	public int getMoveNumber() {
		return mMoveNumber;
	}

	public Move getMove() {
		return mMove;
	}

	public int getPeriodsBlack() {
		return mBlackPeriods;
	}

	public int getPeriodsWhite() {
		return mWhitePeriods;
	}

	@Override
	public String toJson() {
		JsonWriter jw = new JsonWriter();
		jw.add( "type" , "gameupdate" );
		jw.add( "messageId" , Long.toString( mMessageId ) );
		jw.add( "gameId" , Long.toString( mGameId ) );
		jw.add( "moveNumber" , mMoveNumber );
		jw.add( "move" , mMove.toJson() );
		jw.add( "blackPeriods" , mBlackPeriods );
		jw.add( "whitePeriods" , mWhitePeriods );
		jw.add( "timeSinceLastMove" , mTimeSinceLastMove );
		return jw.toString();
	}

	@Override
	public long getMessageId() {
		return mMessageId;
	}

	public int getTimeSinceLastMove() {
		return mTimeSinceLastMove;
	}
}
