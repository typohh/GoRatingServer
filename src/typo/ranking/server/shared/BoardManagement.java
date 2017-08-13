package typo.ranking.server.shared;

import java.io.Serializable;
import java.util.logging.Logger;

public class BoardManagement implements Serializable {

	private Move[] mHistory;
	private int[] mLast;
	private int mNumberOfMoves = 0;
	private BoardState mState;
	private BoardState mStateCopy;
	private int mCapturesBlack = 0;
	private int mCapturesWhite = 0;

	public BoardManagement( int pSize ) {
		mState = new BoardState( pSize );
		mStateCopy = new BoardState( pSize );
		mHistory = new Move[2048]; // 1024 moves + 1024 captures..
		mLast = new int[1024]; // 1024 moves..
	}

	private void addToHistory( Move pMove ) {
		mHistory[mLast[mNumberOfMoves]] = pMove;
		++mLast[mNumberOfMoves];
	}

	public Move getMove( int pIndex ) {
		if( pIndex == 0 ) {
			return mHistory[0];
		} else {
			return mHistory[mLast[pIndex - 1]];
		}
	}
	
	private boolean mWasSuperKoViolation;

	public boolean wasSuperKoViolation() {
		return mWasSuperKoViolation;
	}
	
	public boolean play( Move pMove ) {
		if( mState.play( pMove ) ) {
			boolean superKoViolation = false;
			mStateCopy.reset();
			for( int i = 0 ; i < mState.getNumberOfIntersections() ; ++i ) {
				Move move = Move.values()[i];
				if( mStateCopy.get( move ) != mState.get( move ) ) {
					mStateCopy.mMoves.add( move );
				}
			}
			int first = 0;
			for( int i = 0 ; i < mNumberOfMoves ; ++i ) {
				// System.out.println( i + " : " + mStateCopy.mMoves );
				int last = mLast[i];
				if( mHistory[first].isMove() ) {
					mStateCopy.set( mHistory[first] , Stone.getStone( mStateCopy.mBlackNotWhite ) );
					if( mStateCopy.get( mHistory[first] ) != mState.get( mHistory[first] ) ) {
						mStateCopy.mMoves.add( mHistory[first] );
					} else {
						mStateCopy.mMoves.remove( mHistory[first] );
					}
				}
				for( int moveIndex = first + 1 ; moveIndex < last ; ++moveIndex ) {
					Move move = mHistory[moveIndex];
					if( mStateCopy.get( move ) != Stone.getStone( !mStateCopy.mBlackNotWhite ) ) {
						Logger.getLogger( "debug" ).severe( mHistory[0] + " " + mHistory[1] + " " + mHistory[2] );
						throw new Error( "impossible reconstruction, " + mStateCopy.get( move ) + " at " + move );
					}
					mStateCopy.set( move , Stone.Empty );
					if( mStateCopy.get( move ) != mState.get( move ) ) {
						mStateCopy.mMoves.add( move );
					} else {
						mStateCopy.mMoves.remove( move );
					}
				}
				first = last;
				mStateCopy.mBlackNotWhite = !mStateCopy.mBlackNotWhite;
				if( mStateCopy.mMoves.size() == 0 ) {
					superKoViolation = true;
				}
			}
			mStateCopy.mMoves.clear();
			if( !pMove.isMove() ) {
				superKoViolation = false;
			}
			mWasSuperKoViolation=superKoViolation;
			if( superKoViolation ) { // reset to previous move..
				mState.copy( mStateCopy );
				return false;
			} else { // add new moves to history..
				addToHistory( pMove );
				for( int i = 0 ; i < mState.getNumberOfIntersections() ; ++i ) {
					Move move = Move.values()[i];
					if( move.equals( pMove ) ) {
						continue;
					}
					if( mStateCopy.mIntersections[move.ordinal()] != mState.mIntersections[move.ordinal()] ) {
						addToHistory( move );
						if( mState.mBlackNotWhite ) {
							++mCapturesWhite;
						} else {
							++mCapturesBlack;
						}
					}
				}
				++mNumberOfMoves;
				mLast[mNumberOfMoves] = mLast[mNumberOfMoves - 1];
				if( getMove( mNumberOfMoves - 1 ) != pMove ) {
					throw new Error();
				}
				return true;
			}
		}
		return false;
	}

	public int getNumberOfMoves() {
		return mNumberOfMoves;
	}

	public void reset() {
		mState.reset();
		mNumberOfMoves = 0;
		mCapturesBlack = 0;
		mCapturesWhite = 0;
		mLast[0] = 0;
	}

	public Stone getStone( Move pMove ) {
		return mState.get( pMove );
	}

	public boolean getTurn() {
		return getNumberOfMoves() % 2 == 0;
	}

	public int getCaptures( boolean pBlackNotWhite ) {
		if( pBlackNotWhite ) {
			return mCapturesBlack;
		} else {
			return mCapturesWhite;
		}
	}

	public void undo() {
		throw new Error( "client sent move in time, but agreed on server too late." );
	}

	public Move getLastMove() {
		if( getNumberOfMoves() == 0 ) {
			return null;
		}
		return getMove( getNumberOfMoves() - 1 );
	}

	public Move getSecondLastMove() {
		if( getNumberOfMoves() < 2 ) {
			return null;
		}
		return getMove( getNumberOfMoves() - 2 );
	}

	public BoardState getTerritory( boolean pSimplify ) {
		if( pSimplify ) {
			return mState.simplify().scoreChinese();
		} else {
			return mState.scoreChinese();
		}
	}

	public boolean isOver() {
		if( getLastMove() == Move.TimeLoss || getLastMove() == Move.Resign ) {
			return true;
		}
		if( isScoring() ) {
			return true;
		}
		return false;
	}

	public boolean isScoring() {
		return( getLastMove() == Move.Pass && getSecondLastMove() == Move.Pass );
	}

	public BoardState getState() {
		return mState;
	}
}
