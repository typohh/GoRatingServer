package typo.ranking.server.server;

import java.util.Collection;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import typo.ranking.server.shared.Move;

public class Game {

	private Entity mEntity;

	public Game( long pBlackId , long pWhiteId , long pPeriodTime , int pPeriods ) {
		mEntity=new Entity( "Game" );
		mEntity.setIndexedProperty( "PlayerBlack" , KeyFactory.createKey( "Player" , pBlackId ));
		mEntity.setIndexedProperty( "PlayerWhite" , KeyFactory.createKey( "Player" , pWhiteId ));
		long time=System.currentTimeMillis();
		mEntity.setUnindexedProperty( "DateLastMove" , new Long( time ) );
		
		mEntity.setUnindexedProperty( "PeriodsBlack" , pPeriods );
		mEntity.setUnindexedProperty( "PeriodsWhite" , pPeriods );
		
		mEntity.setUnindexedProperty( "PeriodsCount" , pPeriods );
		mEntity.setUnindexedProperty( "PeriodsTime" , pPeriodTime );

		mEntity.setIndexedProperty( "Compleated" , Boolean.FALSE );
	}
	
	
	public Game( Entity pEntity ) {
		mEntity = pEntity;
	}

	private Move[] mMovesCache;
	
	public Move[] getMoves() {
		if( mMovesCache == null ) {
			Blob blob = (Blob) mEntity.getProperty( "Moves" );
			if( blob != null ) {
				byte[] bytes = blob.getBytes();
				Move[] moves = new Move[bytes.length / 2];
				for( int i = 0 ; i < moves.length ; ++i ) {
					byte x = bytes[ i * 2 + 0 ];
					byte y = bytes[ i * 2 + 1 ];
					moves[i] = Move.getMove( x , y );
				}
				mMovesCache=moves;
			} else {
				mMovesCache=new Move[ 0 ];
			}
		}
		return mMovesCache;
	}

	public void addMove( Move pMove ) {
		Move[] moves = getMoves();
		Move[] tmp;
		tmp = new Move[moves.length + 1];
		System.arraycopy( moves , 0 , tmp , 0 , moves.length );
		tmp[tmp.length - 1] = pMove;
		moves=tmp;
		byte[] bytes=new byte[ moves.length * 2 ];
		for( int i=0 ; i<moves.length ; ++i ) {
			Move move=moves[ i ];
			bytes[ i * 2 + 0 ]=(byte)move.getX();
			bytes[ i * 2 + 1 ]=(byte)move.getY();
		}
		mMovesCache=moves;
		mEntity.setUnindexedProperty( "Moves" , new Blob( bytes ) );
	}

	public void setDateLastMove( long pTime ) {
		mEntity.setUnindexedProperty( "DateLastMove" , pTime );
	}
	
	public long getPlayerBlack() {
		return ((Key) mEntity.getProperty( "PlayerBlack" )).getId();
	}

	public long getPlayerWhite() {
		return ((Key) mEntity.getProperty( "PlayerWhite" )).getId();
	}

	public long getDateLastMove() {
		return Storage.convertLong( mEntity.getProperty( "DateLastMove" ));
	}

	public long getDateCreated() {
		return Storage.convertLong( mEntity.getProperty( "DateCreated" ));
	}

	public int getPlayerPeriods( long pId ) {
		if( pId == getPlayerBlack() ) {
			return (int)Storage.convertLong( mEntity.getProperty( "PeriodsBlack" ));
		} else if( pId == getPlayerWhite() ) {
			return (int)Storage.convertLong( mEntity.getProperty( "PeriodsWhite" ));
		} else {
			throw new Error();
		}
	}

	public long getPeriodTime() {
		return Storage.convertLong( mEntity.getProperty( "PeriodsTime" ));
	}

	public long getPeriodCount() {
		return (int)Storage.convertLong( mEntity.getProperty( "PeriodsCount" ));
	}

	public long getTurn() {
		if( getMoves().length % 2 == 0 ) {
			return getPlayerBlack();
		} else {
			return getPlayerWhite();
		}
	}

	public Entity getEntity() {
		return mEntity;
	}


	public long getId() {
		return mEntity.getKey().getId();
	}

	public void setPlayerPeriods( long pId , int pPeriods ) {
		if( pId == getPlayerBlack() ) {
			mEntity.setUnindexedProperty( "PeriodsBlack" , pPeriods );
		} else if( pId == getPlayerWhite() ) {
			mEntity.setUnindexedProperty( "PeriodsWhite" , pPeriods );
		} else {
			throw new Error();
		}
	}

	public void setScore( double pScore ) {
		mEntity.setUnindexedProperty( "Score" , pScore );
	}
	
	public double getScore() {
		return Storage.convertDouble( mEntity.getProperty( "Score" ) );
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
		Move lastMove=getMoves()[ getMoves().length - 1 ];
		if( ( lastMove.equals( Move.Resign ) ) || ( lastMove.equals( Move.TimeLoss ) ) ) {
			return getTurn();
		}
		if( mEntity.hasProperty( "Score" )) {
			if( getScore() > 0 ) {
				return getPlayerBlack();
			} else {
				return getPlayerWhite();
			}
		}
		throw new Error();
	}

	public void setCompleted() {
		mEntity.setIndexedProperty( "Compleated" , Boolean.TRUE );
		mEntity.setIndexedProperty( "DateCreated" , new Long( System.currentTimeMillis() ) );
	}

	public boolean isCompleted() {
		return mEntity.hasProperty( "Compleated" ) && mEntity.getProperty( "Compleated" ).equals( Boolean.TRUE );
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
		Move[] moves=getMoves();
		if( moves.length < 2 ){
			return null;
		} else {
			return moves[ moves.length - 2 ];
		}
	}


	public Move getLastMove() {
		Move[] moves=getMoves();
		if( moves.length == 0 ){
			return null;
		} else {
			return moves[ moves.length - 1 ];
		}
	}

	public Move[] getDead() {
		Blob blob = (Blob) mEntity.getProperty( "Deads" );
		if( blob != null ) {
			byte[] bytes = blob.getBytes();
			Move[] moves = new Move[bytes.length / 2];
			for( int i = 0 ; i < moves.length ; ++i ) {
				byte x = bytes[ i * 2 + 0 ];
				byte y = bytes[ i * 2 + 1 ];
				moves[i] = Move.getMove( x , y );
			}
			return moves;
		} else {
			return new Move[ 0 ];
		}
	}

	public void setDead( Collection<Move> pDead ) {
		byte[] bytes=new byte[ pDead.size() * 2 ];
		int index=0;
		for( Move dead : pDead ) {
			bytes[ index * 2 + 0 ]=(byte)dead.getX();
			bytes[ index * 2 + 1 ]=(byte)dead.getY();
			++index;
		}
		mEntity.setUnindexedProperty( "Deads" , new Blob( bytes ) );
	}
	
	public boolean hasDead() {
		return mEntity.hasProperty( "Deads" );
	}


	public int getNumberOfMoves() {
		return getMoves().length;
	}

}
