/**
 * 
 */
package typo.ranking.server.shared;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class MoveHashSet extends AbstractSet<Move> implements Set<Move> , Iterable<Move> , Serializable {

	private static final long serialVersionUID = 1L;
	
	private int mSize=0;
	private int mFirst=-2;
	private int[] mLookupForward=null;
	private int[] mLookupBackward=null;
	private Move[] mLookupReverse=null;
	
	public Move next( Move pCurrent ) {
		int index;
		if( pCurrent != null ) {
			index=mLookupForward[ pCurrent.ordinal() ];
		} else {
			index=mFirst;
		}
		if( index != -2 ) {
			return (Move) mLookupReverse[ index ];
		} else {
			return null;
		}
	}
	
    public MoveHashSet() {
    	mLookupForward=new int[ Move.values().length ];
    	Arrays.fill( mLookupForward , -1 );
    	mLookupBackward=new int[ Move.values().length ];
    	Arrays.fill( mLookupBackward , -1 );
    	mLookupReverse=new Move[ Move.values().length ];
    	for( Move e : Move.values() ) {
    		mLookupReverse[ e.ordinal() ]=e;
    	}
    }
    
	public void addAll( MoveHashSet pC ) {
    	for( Move e=pC.next( null ) ; e != null ; e=pC.next( e ) ) {
    		add( e );
    	}
	}

	public Move first() {
		if( mFirst == -2 ) {
			return null;
		}
    	return mLookupReverse[ mFirst ];
    }
    
    @Override
	public Iterator<Move> iterator() {
    	return new Iterator<Move>() {
    		
    		int mIndex=mFirst;

			@Override
			public boolean hasNext() {
				return mIndex != -2;
			}

			@Override
			public Move next() {
				Move e=mLookupReverse[ mIndex ];
				mIndex=mLookupForward[ mIndex ];
				return e;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
    		
    	};
    }
    
    @Override
	public int size() {
    	return mSize;
    }

    @Override
	public boolean isEmpty() {
    	return mSize == 0;
    }

    public boolean contains( Move pO ) {
    	return mLookupForward[ pO.ordinal() ] != -1;
    }

	@Override
	public boolean remove( Object pO ) {
    	return remove( (Move)pO );
	}

	@Override
	public boolean add( Move pO ) {
    	if( mLookupForward[ pO.ordinal() ] == -1 ) {
    		if( mFirst != -2 ) {
        		mLookupBackward[ mFirst ]=pO.ordinal();
    		}
			mLookupForward[ pO.ordinal() ]=mFirst;
			mLookupBackward[ pO.ordinal() ]=-2;
    		mFirst=pO.ordinal();
    		++mSize;
    		return true;
    	} else {
    		return false;
    	}
    }

    public boolean remove( Move pO ) {
    	if( mLookupForward[ pO.ordinal() ] != -1 ) {
    		int before=mLookupBackward[ pO.ordinal() ];
    		int after=mLookupForward[ pO.ordinal() ];
    		if( before != -2 ) {
    			mLookupForward[ before ]=after;
    		} else {
    			mFirst=after;
    		}
    		mLookupForward[ pO.ordinal() ]=-1;
    		if( after != -2 ) {
    			mLookupBackward[ after ]=before;
    		}
    		mLookupBackward[ pO.ordinal() ]=-1;
    		--mSize;
    		return true;
    	} else {
    		return false;
    	}
    }
    
    

//    @Override
//	public boolean contains( Object pArg0 ) {
//		return super.contains(pArg0);
//	}

	@Override
	public void clear() {
    	int index=mFirst;
    	while( index != -2 ) {
    		int next=mLookupForward[ index ];
    		mLookupForward[ index ]=-1;
    		mLookupBackward[ index ]=-1;
    		index=next;
    	}
    	mSize=0;
    	mFirst=-2;
    }

   
}