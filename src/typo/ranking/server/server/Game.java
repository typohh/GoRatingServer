package typo.ranking.server.server;

import java.util.Collection;
import java.util.Vector;

import typo.ranking.server.shared.Move;

public class Game {

	private long mId;
	private long mBlackId;
	private long mWhiteId;
	private long mPeriodTime;
	private int mPeriodsOriginal;
	private int mPeriodsBlack;
	private int mPeriodsWhite;
	private long mLastMoveDate;
	private boolean mCompleated;
	private Vector<Move> mMoves=new Vector<>();
	private Vector<Move> mDead;
	private long mCreatedDate;
	private double mScore;
	
	
	public Game( long pBlackId , long pWhiteId , long pPeriodTime , int pPeriods ) {
		mBlackId=pBlackId;
		mWhiteId=pWhiteId;
		long time=System.currentTimeMillis();
		mCreatedDate = mLastMoveDate = time;
		mPeriodsOriginal = mPeriodsBlack = mPeriodsWhite = pPeriods;
		mPeriodTime = pPeriodTime;
		mCompleated = false;
		mScore = Double.NaN;
	}
	
	
	public Game() {
	}

	public Vector<Move> getMoves() {
		return mMoves;
	}

	public void addMove( Move pMove ) {
		mMoves.add(pMove);
	}

	public void setDateLastMove( long pTime ) {
		mLastMoveDate=pTime;
	}
	
	public long getPlayerBlack() {
		return mBlackId;
	}

	public long getPlayerWhite() {
		return mWhiteId;
	}

	public long getDateLastMove() {
		return mLastMoveDate;
	}

	public long getDateCreated() {
		return mCreatedDate;
	}

	public int getPlayerPeriods( long pId ) {
		if( pId == getPlayerBlack() ) {
			return mPeriodsBlack;
		} else if( pId == getPlayerWhite() ) {
			return mPeriodsWhite;
		} else {
			throw new Error();
		}
	}

	public long getPeriodTime() {
		return mPeriodTime;
	}

	public long getPeriodCount() {
		return mPeriodsOriginal;
	}

	public long getTurn() {
		if( getMoves().size() % 2 == 0 ) {
			return getPlayerBlack();
		} else {
			return getPlayerWhite();
		}
	}

	public void setPlayerPeriods( long pId , int pPeriods ) {
		if( pId == getPlayerBlack() ) {
			mPeriodsBlack = pPeriods;
		} else if( pId == getPlayerWhite() ) {
			mPeriodsWhite = pPeriods;
		} else {
			throw new Error();
		}
	}

	public void setScore( double pScore ) {
		mScore = pScore;
	}
	
	public double getScore() {
		return mScore;
	}
	
	public long getOpponent( long pId ) {
		if( pId == getPlayerBlack() ) {
			return getPlayerWhite();
		} else if( pId == getPlayerWhite() ) {
			return getPlayerBlack();
		} else {
			throw new Error();
		}
	}

	public long getWinner() {
		if( ( getLastMove().equals( Move.Resign ) ) || ( getLastMove().equals( Move.TimeLoss ) ) ) {
			return getTurn();
		}
		if( mScore != Double.NaN ) {
			if( getScore() > 0 ) {
				return getPlayerBlack();
			} else {
				return getPlayerWhite();
			}
		}
		throw new Error();
	}

	public void setCompleted() {
		mCompleated=true;
	}

	public boolean isCompleted() {
		return mCompleated;
	}

	public boolean isOver() {
		if( getLastMove() == Move.Resign ) {
			return true;
		}
		if( getLastMove() == Move.TimeLoss ) {
			return true;
		}
		if( isScoring() ) {
			return true;
		}
		return false;
	}
	
	public boolean isScoring() {
		return ( getLastMove() == Move.Pass && getSecondLastMove() == Move.Pass );
	}
	
	public boolean isTimeLoss( long pTime ) {
		return getDateLastMove() + getPlayerPeriods( getTurn() ) * getPeriodTime() < pTime;
	}

	public Move getSecondLastMove() {
		if( mMoves.size() < 2 ) {
			return Move.Missing;
		} else {
			return mMoves.get(mMoves.size()-2);
		}
	}

	public Move getLastMove() {
		if( !mMoves.isEmpty() ) {
			return mMoves.lastElement();
		} else {
			return Move.Missing;
		}
	}

	public Collection<Move> getDead() {
		return mDead;
	}

	public void setDead( Collection<Move> pDead ) {
		mDead=new Vector<>(pDead);
	}
	
	public boolean hasDead() {
		return mDead != null;
	}


	public int getNumberOfMoves() {
		return mMoves.size();
	}

}
