package typo.ranking.server.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class Storage {

	public static long convertLong( Object pObject ) {
		if( pObject == null ) {
			return 0;
		}
		if( pObject instanceof Long ) {
			return (Long) pObject;
		}
		if( pObject instanceof Integer ) {
			return (Integer) pObject;
		}
		if( pObject instanceof Short ) {
			return (Short) pObject;
		}
		throw new Error( pObject.getClass().getName() );
	}

	public static double convertDouble( Object pObject ) {
		if( pObject == null ) {
			return Double.NaN;
		}
		if( pObject instanceof Double ) {
			return (Double) pObject;
		}
		if( pObject instanceof Float ) {
			return (Float) pObject;
		}
		throw new Error( pObject.getClass().getName() );
	}

	private DatastoreService mStore;
	private MemcacheService mCache;
	private static Storage sStorage;
	private static final Object sLock = new Object();

	private Storage() {
		mStore = DatastoreServiceFactory.getDatastoreService();
		mCache = MemcacheServiceFactory.getMemcacheService();
	}

	public static Storage get() {
		if( sStorage == null ) {
			synchronized( sLock ) {
				if( sStorage == null ) {
					sStorage = new Storage();
				}
			}
		}
		return sStorage;
	}

	public Transaction startTransaction() {
		return mStore.beginTransaction();
	}

	public Player getPlayerById( long pId ) {
		Key key = KeyFactory.createKey( "Player" , pId );
		Entity entity = null;
		if( mCache.contains( key ) ) { // have in cache..
			entity = (Entity) mCache.get( key );
		} else { // cache miss..
			try {
				logger.info( "reading datastore player " + pId );
				entity = mStore.get( key );
			} catch( EntityNotFoundException pE ) {
				mCache.put( key , null );
				return null;
			}
			mCache.put( key , entity );
		}
		return new Player( entity );
	}

	// public Game getGameByPassCache( long pId ) {
	// if( pId == 0 ) {
	// return null;
	// }
	// Key key = KeyFactory.createKey( "Game" , pId );
	// Entity entity=null;
	// try {
	// logger.info( "reading datastore game " + pId );
	// entity = mStore.get( key );
	// } catch( EntityNotFoundException pE ) {
	// mCache.put( key , null );
	// return null;
	// }
	// mCache.put( key , entity );
	// return new Game( entity );
	// }
	public Game getGameById( long pId ) {
		if( pId == 0 ) {
			return null;
		}
		Key key = KeyFactory.createKey( "Game" , pId );
		Entity entity = null;
		if( mCache.contains( key ) ) { // have in cache..
			entity = (Entity) mCache.get( key );
		} else { // cache miss..
			try {
				logger.info( "reading datastore game " + pId );
				entity = mStore.get( key );
			} catch( EntityNotFoundException pE ) {
				mCache.put( key , null );
				return null;
			}
			mCache.put( key , entity );
		}
		return new Game( entity );
	}

	public void put( Player pPlayer ) {
		Key key = mStore.put( pPlayer.getEntity() );
		mCache.put( key , pPlayer.getEntity() );
	}

	public void put( Game pGame ) {
		Key key = mStore.put( pGame.getEntity() );
		mCache.put( key , pGame.getEntity() );
	}

	public void putCache( String pKey , Object pValue ) {
		mCache.put( pKey , pValue );
	}

	public Object getCache( String pKey ) {
		return mCache.get( pKey );
	}

	final static Logger logger = Logger.getLogger( "debug" );

	public Object deserialize( Object pObject ) throws ClassNotFoundException {
		if( pObject instanceof Blob ) {
			try {
				return new ObjectInputStream( new ByteArrayInputStream( ((Blob) pObject).getBytes() ) ).readObject();
			} catch( IOException pE ) {
				throw new Error( pE );
			}
		}
		return pObject;
	}

	public Object serialize( Object pObject ) {
		if( pObject instanceof Boolean ) {
			return pObject;
		}
		if( pObject instanceof Short ) {
			return pObject;
		}
		if( pObject instanceof Integer ) {
			return pObject;
		}
		if( pObject instanceof Long ) {
			return pObject;
		}
		if( pObject instanceof Float ) {
			return pObject;
		}
		if( pObject instanceof Double ) {
			return pObject;
		}
		if( pObject instanceof String ) {
			return pObject;
		}
		if( pObject instanceof ArrayList<?> ) {
			return pObject;
		}
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream( bytes );
			output.writeObject( pObject );
			output.flush();
			return new Blob( bytes.toByteArray() );
		} catch( IOException pE ) {
			throw new Error( pE );
		}
	}

	public Object getValue( String pKey ) {
		Object o = getCache( pKey );
		if( o != null ) {
			return o;
		}
		try {
			logger.info( "reading datastore " + pKey );
			Entity e = mStore.get( KeyFactory.createKey( "Pair" , pKey ) );
			o = deserialize( e.getProperty( "Value" ) );
			putCache( pKey , o );
			return o;
		} catch( EntityNotFoundException pE ) {
		} catch( ClassNotFoundException pE ) {
			logger.severe( pE.getMessage() );
		}
		return null;
	}

	public void putValue( String pKey , Serializable pO ) {
		putCache( pKey , pO );
		Entity entity = new Entity( "Pair" , pKey );
		entity.setProperty( "Value" , serialize( pO ) );
		mStore.put( entity );
	}

	public boolean hasNameBy( String pName ) {
		Filter propertyFilter = new FilterPredicate( "Name" , FilterOperator.EQUAL , pName );
		Query q = new Query( "Player" ).setFilter( propertyFilter ).setKeysOnly();
		PreparedQuery pq = mStore.prepare( q );
		return pq.asSingleEntity() != null;
	}

	public ArrayList<Player> listActiveSearch() {
		Filter propertyFilter = new FilterPredicate( "SearchStartTime" , FilterOperator.GREATER_THAN_OR_EQUAL , Long.MIN_VALUE );
		Query q = new Query( "Player" ).setFilter( propertyFilter ).setKeysOnly();
		PreparedQuery pq = mStore.prepare( q );
		ArrayList<Key> keys = new ArrayList<Key>();
		for( Entity e : pq.asIterable() ) {
			keys.add( e.getKey() );
		}
		Map<Key, Object> cached = mCache.getAll( keys );
		ArrayList<Key> keys2 = new ArrayList<Key>();
		for( Key key : keys ) {
			if( !cached.containsKey( key ) ) {
				keys2.add( key );
			}
		}
		Map<Key, Entity> cached2 = mStore.get( keys2 );
		mCache.putAll( cached2 );
		ArrayList<Player> players = new ArrayList<Player>();
		for( Key key : keys ) {
			if( cached.containsKey( key ) ) {
				players.add( new Player( (Entity) cached.get( key ) ) );
			} else {
				players.add( new Player( cached2.get( key ) ) );
			}
			if( players.get( players.size() - 1 ).getSearchStartTime() == 0 ) {
				players.remove( players.size() - 1 );
			}
		}
		return players;
	}

	public ArrayList<Player> listPlayers() {
		Query q = new Query( "Player" ).setKeysOnly().addSort( "RatingMean" , SortDirection.DESCENDING );
		PreparedQuery pq = mStore.prepare( q );
		ArrayList<Key> keys = new ArrayList<Key>();
		for( Entity e : pq.asIterable() ) {
			keys.add( e.getKey() );
		}
		Map<Key, Object> cached = mCache.getAll( keys );
		ArrayList<Player> players=new ArrayList<Player>();
		for( Key key : keys ) {
			Entity e = (Entity) cached.get( key );
			if( e == null ) {
				try {
					e = mStore.get( key );
				} catch( EntityNotFoundException pE ) {
					throw new Error( pE );
				}
				mCache.put( key , e );
			}
			Player player = new Player( e );
			players.add( player );
		}
		return players;
	}
	
	public PreparedQuery doQuery( Query pQuery ) {
		return mStore.prepare( pQuery );
	}
	
	public ArrayList<Game> listRecentGames( int pNumber ) {
		Query q = new Query( "Game" ).setKeysOnly();
		q.addSort( "DateCreated" , SortDirection.DESCENDING );
		PreparedQuery pq = mStore.prepare( q );
		ArrayList<Key> keys = new ArrayList<Key>();
		for( Entity e : pq.asIterable() ) {
			keys.add( e.getKey() );
			if( keys.size() >= pNumber ) {
				break;
			}
		}
		Map<Key, Object> cached = mCache.getAll( keys );
		ArrayList<Key> keys2 = new ArrayList<Key>();
		for( Key key : keys ) {
			if( !cached.containsKey( key ) ) {
				keys2.add( key );
			}
		}
		Map<Key, Entity> cached2 = mStore.get( keys2 );
		mCache.putAll( cached2 );
		ArrayList<Game> games = new ArrayList<Game>();
		for( Key key : keys ) {
			if( cached.containsKey( key ) ) {
				games.add( new Game( (Entity) cached.get( key ) ) );
			} else {
				games.add( new Game( cached2.get( key ) ) );
			}
			if( !games.get( games.size() - 1 ).isCompleted() ) {
				games.remove( games.size() - 1 );
			}
		}
		return games;
	}

	public void delCache( String pString ) {
		mCache.delete( pString );
	}

	public void delValue( String pKey ) {
		delCache( pKey );
		mStore.delete( KeyFactory.createKey( "Pair" , pKey ) );
	}

	public ArrayList<Game> listActiveGames() {
		Filter propertyFilter = new FilterPredicate( "Compleated" , FilterOperator.EQUAL , Boolean.FALSE );
		Query q = new Query( "Game" ).setFilter( propertyFilter ).setKeysOnly();
		PreparedQuery pq = mStore.prepare( q );
		ArrayList<Key> keys = new ArrayList<Key>();
		for( Entity e : pq.asIterable() ) {
			keys.add( e.getKey() );
		}
		Map<Key, Object> cached = mCache.getAll( keys );
		ArrayList<Key> keys2 = new ArrayList<Key>();
		for( Key key : keys ) {
			if( !cached.containsKey( key ) ) {
				keys2.add( key );
			}
		}
		Map<Key, Entity> cached2 = mStore.get( keys2 );
		mCache.putAll( cached2 );
		ArrayList<Game> games = new ArrayList<Game>();
		for( Key key : keys ) {
			if( cached.containsKey( key ) ) {
				games.add( new Game( (Entity) cached.get( key ) ) );
			} else {
				games.add( new Game( cached2.get( key ) ) );
			}
			if( games.get( games.size() - 1 ).isCompleted() ) {
				games.remove( games.size() - 1 );
			}
		}
		return games;
	}
	
	public ArrayList<Long> getWatchingz( long pGameId ) {
		Filter propertyFilter = new FilterPredicate( "WatchingGame" , FilterOperator.EQUAL , KeyFactory.createKey( "Game" , pGameId ) );
		Query q = new Query( "Player" ).setFilter( propertyFilter ).setKeysOnly();
		PreparedQuery pq = mStore.prepare( q );
		ArrayList<Long> keys = new ArrayList<Long>();
		for( Entity e : pq.asIterable() ) {
			keys.add( e.getKey().getId() );
		}
		return keys;
	}

}
