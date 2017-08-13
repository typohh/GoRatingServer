package typo.ranking.server.shared;

import java.io.Serializable;
import java.util.Arrays;

public class BoardState implements Serializable {

	private static final long serialVersionUID = 1L;
	protected int mSize;
	protected Stone[] mIntersections;
	protected boolean mBlackNotWhite;
	protected transient MoveHashSet mMoves;

	public BoardState() {}

	public MoveHashSet getMoves() {
		if( mMoves == null ) {
			mMoves = new MoveHashSet();
		}
		return mMoves;
	}

	public BoardState( int pSize ) {
		mIntersections = new Stone[pSize * pSize];
		// mTmp = new boolean[pSize * pSize];
		mMoves = new MoveHashSet();
		mSize = pSize;
		mBlackNotWhite = true;
		Arrays.fill( mIntersections , Stone.Empty );
	}

	public BoardState( BoardState pState ) {
		mIntersections = new Stone[pState.mIntersections.length];
		System.arraycopy( pState.mIntersections , 0 , mIntersections , 0 , pState.mIntersections.length );
		mSize = pState.mSize;
		mBlackNotWhite = pState.mBlackNotWhite;
		mMoves = new MoveHashSet();
	}

	public int getNumberOfIntersections() {
		return mSize * mSize;
	}

	public void set( Move pMove , Stone pStone ) {
		if( pMove == null || pStone == null ) {
			throw new Error( pMove + " " + pStone );
		}
		mIntersections[pMove.ordinal()] = pStone;
	}

	public Stone get( Move pMove ) {
		if( pMove == null ) {
			throw new Error( "how can this be null?!? " );
		}
		return mIntersections[pMove.ordinal()];
	}

	protected void getSpaceAndEdges( Move pMove ) {
		if( pMove.equals( Move.Outside ) ) {
			return;
		}
		if( getMoves().contains( pMove ) ) {
			return;
		}
		Stone stone = mIntersections[pMove.ordinal()];
		// System.out.println( "examining " + pMove + " = " + stone );
		if( stone.equals( Stone.Empty ) || stone.equals( Stone.Ko ) ) {
			getMoves().add( pMove );
			getSpaceAndEdges( Move.getMove( pMove.getX() - 1 , pMove.getY() , mSize ) );
			getSpaceAndEdges( Move.getMove( pMove.getX() + 1 , pMove.getY() , mSize ) );
			getSpaceAndEdges( Move.getMove( pMove.getX() , pMove.getY() - 1 , mSize ) );
			getSpaceAndEdges( Move.getMove( pMove.getX() , pMove.getY() + 1 , mSize ) );
			return;
		}
		getMoves().add( pMove );
	}

	protected void getGroupAndLiberties( Move pMove , boolean pBlackNotWhite ) {
		if( pMove.equals( Move.Outside ) ) {
			return;
		}
		if( getMoves().contains( pMove ) ) {
			return;
		}
		Stone stone = mIntersections[pMove.ordinal()];
		// System.out.println( "examining " + pMove + " = " + stone );
		if( stone.equals( Stone.Empty ) || stone.equals( Stone.Ko ) ) {
			getMoves().add( pMove );
			// System.out.println( "added " + pMove + " (" + mMoves.size() + ")" );
			return;
		}
		if( Stone.getStone( pBlackNotWhite ) == stone ) {
			getMoves().add( pMove );
			// System.out.println( "added " + pMove + " (" + mMoves.size() + ")" );
			getGroupAndLiberties( Move.getMove( pMove.getX() - 1 , pMove.getY() , mSize ) , pBlackNotWhite );
			getGroupAndLiberties( Move.getMove( pMove.getX() + 1 , pMove.getY() , mSize ) , pBlackNotWhite );
			getGroupAndLiberties( Move.getMove( pMove.getX() , pMove.getY() - 1 , mSize ) , pBlackNotWhite );
			getGroupAndLiberties( Move.getMove( pMove.getX() , pMove.getY() + 1 , mSize ) , pBlackNotWhite );
		}
	}

	protected boolean hasLiberties() {
		for( Move move = getMoves().first() ; move != null ; move = getMoves().next( move ) ) {
			if( mIntersections[move.ordinal()] == Stone.Empty ) {
				return true;
			}
		}
		return false;
	}

	public void capture( Move pMove , boolean pBlackNotWhite ) {
		// System.out.println( "constructing group and liberties for " + pMove + " " + Stone.getStone( pBlackNotWhite ));
		getGroupAndLiberties( Move.getMove( pMove.getX() , pMove.getY() , mSize ) , pBlackNotWhite );
		if( !hasLiberties() ) {
			// System.out.println( "capturing " + Stone.getStone( pBlackNotWhite ) + " stones. (" + mMoves.size() + ")" );
			for( Move move = getMoves().first() ; move != null ; move = getMoves().next( move ) ) {
				// System.out.println( "potential " + move + " with " + mIntersections[ move.ordinal() ] );
				if( mIntersections[move.ordinal()] == Stone.getStone( pBlackNotWhite ) ) {
					mIntersections[move.ordinal()] = Stone.Empty;
					// System.out.println( "captured " + move );
				}
			}
		}
		getMoves().clear();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for( int y = 0 ; y < getSize() ; ++y ) {
			for( int x = 0 ; x < getSize() ; ++x ) {
				buffer.append( get( Move.getMove( x , y ) ).toChar() );
			}
			buffer.append( "\n" );
		}
		return buffer.toString();
	}

	public boolean play( Move pMove ) {
		// System.out.println( Stone.getStone( mBlackNotWhite ) + " move at " + pMove );
		if( !pMove.isMove() ) {
			mBlackNotWhite = !mBlackNotWhite;
			return true;
		}
		if( mIntersections[pMove.ordinal()] != Stone.Empty ) {
			return false;
		}
		mIntersections[pMove.ordinal()] = Stone.getStone( mBlackNotWhite );
		capture( Move.getMove( pMove.getX() - 1 , pMove.getY() , mSize ) , !mBlackNotWhite );
		capture( Move.getMove( pMove.getX() + 1 , pMove.getY() , mSize ) , !mBlackNotWhite );
		capture( Move.getMove( pMove.getX() , pMove.getY() - 1 , mSize ) , !mBlackNotWhite );
		capture( Move.getMove( pMove.getX() , pMove.getY() + 1 , mSize ) , !mBlackNotWhite );
		// System.out.println( "constructing group and liberties for " + pMove + " " + Stone.getStone( mBlackNotWhite ));
		getGroupAndLiberties( Move.getMove( pMove.getX() , pMove.getY() , mSize ) , mBlackNotWhite );
		if( !hasLiberties() ) {
			// System.out.println( pMove + " has no liberties" );
			mIntersections[pMove.ordinal()] = Stone.Empty;
			getMoves().clear();
			return false;
		}
		getMoves().clear();
		mBlackNotWhite = !mBlackNotWhite;
		return true;
	}

	public void reset() {
		Arrays.fill( mIntersections , Stone.Empty );
		mBlackNotWhite = true;
		if( getMoves().size() != 0 ) {
			throw new Error();
		}
	}

	public void copy( BoardState pState ) {
		if( mSize != pState.mSize ) {
			throw new Error();
		}
		mBlackNotWhite = pState.mBlackNotWhite;
		System.arraycopy( pState.mIntersections , 0 , mIntersections , 0 , pState.mIntersections.length );
	}

	public int getSize() {
		return mSize;
	}

	public BoardState simplify() {
		BoardState state = new BoardState( this );
		MoveHashSet space = new MoveHashSet();
		MoveHashSet black = new MoveHashSet();
		MoveHashSet white = new MoveHashSet();
		MoveHashSet done = new MoveHashSet();
		boolean update = true;
		while( update ) {
			done.clear();
			update = false;
			for( int x = 0 ; x < state.getSize() ; ++x ) {
				for( int y = 0 ; y < state.getSize() ; ++y ) {
					if( !state.getMoves().isEmpty() ) {
						throw new Error();
					}
					Move move = Move.getMove( x , y );
					if( done.contains( move ) ) {
						continue;
					}
					// System.out.println( "analyzing " + move );
					if( state.get( move ).isEmptyOrKo() ) {
						state.getSpaceAndEdges( move );
					}
					int blackCount = 0;
					int whiteCount = 0;
					for( Move m = state.getMoves().first() ; m != null ; m = state.getMoves().next( m ) ) { // find what color the edge stones to empty area are..
						if( state.get( m ) == Stone.White ) {
							++whiteCount;
						} else if( state.get( m ) == Stone.Black ) {
							++blackCount;
						} else {
							done.add( m );
						}
					}
					if( blackCount == 0 || whiteCount == 0 ) {
						state.getMoves().clear();
						continue;
					}
					space.addAll( state.getMoves() );
					state.getMoves().clear();
					// find adjacent black groups..
					for( Move m = space.first() ; m != null ; m = space.next( m ) ) {
						if( state.get( m ) == Stone.Black ) {
							state.getGroupAndLiberties( m , true );
						}
					}
					for( Move m = state.getMoves().first() ; m != null ; m = state.getMoves().next( m ) ) {
						if( state.get( m ) == Stone.Black ) {
							black.add( m );
						}
					}
					state.getMoves().clear();
					// find adjacent white groups..
					for( Move m = space.first() ; m != null ; m = space.next( m ) ) {
						if( state.get( m ) == Stone.White ) {
							state.getGroupAndLiberties( m , false );
						}
					}
					for( Move m = state.getMoves().first() ; m != null ; m = state.getMoves().next( m ) ) {
						if( state.get( m ) == Stone.White ) {
							white.add( m );
						}
					}
					state.getMoves().clear();
					// if( space.size() - ( whiteCount + blackCount ) == 1 ) {
					// for( Move m = space.first() ; m != null ; m = space.next( m ) ) {
					// if( state.get( m ).isEmptyOrKo() ) {
					// state.mBlackNotWhite=blackTurn;// kill lighter group..
					// state.play( m );
					// blackTurn=!blackTurn;
					// }
					// }
					// } else {
					if( white.size() > black.size() ) { // kill lighter group..
						update = true;
						for( Move m = black.first() ; m != null ; m = black.next( m ) ) {
							state.set( m , Stone.Empty );
						}
					} else {
						update = true;
						for( Move m = white.first() ; m != null ; m = white.next( m ) ) {
							state.set( m , Stone.Empty );
						}
					}
					// }
					space.clear();
					black.clear();
					white.clear();
				}
			}
		}
		return state;
	}

	public BoardState scoreChinese() {
		BoardState board = new BoardState( mSize );
		Arrays.fill( board.mIntersections , Stone.Ko );
		for( int x = 0 ; x < board.getSize() ; ++x ) {
			for( int y = 0 ; y < board.getSize() ; ++y ) {
				Move move = Move.getMove( x , y );
				if( get( move ) == Stone.Black ) {
					board.set( move , Stone.Black );
				} else if( get( move ) == Stone.White ) {
					board.set( move , Stone.White );
				} else if( board.get( move ) == Stone.Ko ) { // still undecided..
					getSpaceAndEdges( move );
					boolean containsBlack = false;
					boolean containsWhite = false;
					for( Move m = getMoves().first() ; m != null ; m = getMoves().next( m ) ) { // find what color the edge stones to empty area are..
						if( get( m ) == Stone.White ) {
							containsWhite = true;
						} else if( get( m ) == Stone.Black ) {
							containsBlack = true;
						}
					}
					for( Move m = getMoves().first() ; m != null ; m = getMoves().next( m ) ) {
						if( get( m ).isEmptyOrKo() ) { // this was empty in original, so decide according to known edge colors..
							if( containsBlack && !containsWhite ) {
								board.set( m , Stone.Black );
							} else if( containsWhite && !containsBlack ) {
								board.set( m , Stone.White );
							} else {
								board.set( m , Stone.Empty );
							}
						}
					}
					getMoves().clear();
				}
			}
		}
		return board;
	}

	public double getScore() {
		int score = 0;
		for( int i = 0 ; i < mIntersections.length ; ++i ) {
			if( mIntersections[i] == Stone.Black ) {
				++score;
			} else if( mIntersections[i] == Stone.White ) {
				--score;
			}
		}
		return score - 7.5;
	}
}
