package typo.ranking.server.server;

import java.util.Calendar;
import java.util.TimeZone;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import typo.ranking.server.server.HehRating.GlobalParameters;
import typo.ranking.server.server.servlets.EndGameServlet;

public class Player {
	
	private Entity mEntity;

	public Player( Entity pEntity ) {
		mEntity=pEntity;
	}
	
	public Player( String pName ) {
		mEntity=new Entity( "Player" );
		mEntity.setIndexedProperty( "Name" , pName );
		long time=System.currentTimeMillis();
		mEntity.setUnindexedProperty( "DateCreated" , time );
		mEntity.setUnindexedProperty( "DateLastLogin" , time );
		mEntity.setUnindexedProperty( "NumberOfGames" , new Integer( 0 ) );
//		mEntity.setIndexedProperty( "RatingMean" , 0d );
//		mEntity.setUnindexedProperty( "RatingSD" , 3d );
	}

	public Entity getEntity() {
		return mEntity;
	}

	public String getName() {
		return (String) mEntity.getProperty( "Name" );
	}

	public long getDateCreated() {
		return (Long) mEntity.getProperty( "DateCreated" );
	}
	
	public long getLastLogin() {
		return (Long) mEntity.getProperty( "DateLastLogin" );
	}

	public HehRating getRating() { // only set rating when player actually plays a game, this way wont get on leaderboard before playing a game..
		if( mEntity.hasProperty( "RatingMean" )) {
			return new HehRating( (Double)mEntity.getProperty( "RatingMean" ) , (Double)mEntity.getProperty( "RatingSD" ));
		} else {
			GlobalParameters gp=EndGameServlet.getGlobalParameters();
			return new HehRating( gp.getMean() , gp.getStdDev() );
		}
	}
	
	public void setRating( HehRating pRating ) {
		mEntity.setIndexedProperty( "RatingMean" , pRating.getMean()  );
		mEntity.setUnindexedProperty( "RatingSD" , pRating.getStandardDeviation() );
	}
	
	public long getId() {
		return mEntity.getKey().getId();
	}
	
	public int getNumberOfGames() {
		return (int)Storage.convertLong( mEntity.getProperty( "NumberOfGames" ) );
	}

	public void setLastOpponent( long pId ) {
		mEntity.setUnindexedProperty( "LastOpponent" , KeyFactory.createKey( "Player" , pId ));
	}

	public long getLastOpponent() {
		if( mEntity.hasProperty( "LastOpponent" )) {
			return ((Key)mEntity.getProperty( "LastOpponent" )).getId();
		} else {
			return 0;
		}
	}
	
	public void setName( String pName ) {
		mEntity.setIndexedProperty( "Name" , pName );
	}

	public long getActiveGame() {
		Key key=(Key) mEntity.getProperty( "ActiveGame" );
		if( key != null ) {
			return key.getId();
		} else {
			return 0;
		}
	}
	
	public void setWatchingGame( long pId ) {
		if( pId == 0 ) {
			mEntity.removeProperty( "WatchingGame" );
		} else {
			mEntity.setProperty( "WatchingGame" , KeyFactory.createKey( "Game" , pId ) );
		}
	}
	
	public long getWatchingGame() {
		Key key=(Key) mEntity.getProperty( "WatchingGame" );
		if( key != null ) {
			return key.getId();
		} else {
			return 0;
		}
	}
	
	public void setActiveGame( long pId ) {
		if( pId == 0 ) {
			mEntity.removeProperty( "ActiveGame" );
		} else {
			long time=System.currentTimeMillis();
			mEntity.setUnindexedProperty( "NumberOfGames" , 1 + getNumberOfGames() );
			mEntity.setProperty( "ActiveGame" , KeyFactory.createKey( "Game" , pId ) );
			mEntity.setUnindexedProperty( "GamesThisWeek" , getGamesThisWeek( time ) + 1 );
			mEntity.setUnindexedProperty( "GamesLastWeek" , getGamesLastWeek( time ) );
			mEntity.setIndexedProperty( "DateLastGame" , time );
//			if( 1466175600000l < time && time < 1466352000000l ) {
//				mEntity.setUnindexedProperty( "NumberOfTournamentGames" , getNumberOfTournamentGames() + 1 );
//			}
		}
		
	}

	public int getNumberOfTournamentGames() {
		return (int)Storage.convertLong( mEntity.getProperty( "NumberOfTournamentGames" ) );
	}

	public static long getThisWeekBegins( long pTime ) {
		final Calendar c=Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
		c.setTimeInMillis( pTime );
		c.set( Calendar.HOUR_OF_DAY , 0 );
		c.set( Calendar.MINUTE , 0 );
		c.set( Calendar.SECOND , 0 );
		c.set( Calendar.MILLISECOND , 0 );
		while( c.get( Calendar.DAY_OF_WEEK ) != Calendar.MONDAY ) {
			c.add( Calendar.DATE , -1 );
		}
		return c.getTimeInMillis();
	}
	
	public long getGamesThisWeek( long pTime ) {
		if( getDateLastGame() > getThisWeekBegins( pTime ) ) {
			return Storage.convertLong( mEntity.getProperty( "GamesThisWeek" ) ); 				
		} else {
			return 0;
		}
	}
	
	public long getGamesLastWeek( long pTime ) {
		if( getDateLastGame() > getThisWeekBegins( pTime ) ) {
			return Storage.convertLong( mEntity.getProperty( "GamesLastWeek" ) ); 				
		} else if( getDateLastGame() > getThisWeekBegins( pTime ) - sWeek ) {
			return Storage.convertLong( mEntity.getProperty( "GamesThisWeek" ) ); 				
		} else {
			return 0;
		}
	}

	public static final long sWeek=7l * 24l * 60l * 60l * 1000l;
	
	public double getGamesPerWeek( long pTime ) {
		double intoThisWeek=( pTime - getThisWeekBegins( pTime ) ) / (double)sWeek;
		if( intoThisWeek < -0.01 || intoThisWeek > 1.01 ) {
			throw new Error( pTime + " " + getThisWeekBegins( pTime ) + " " + intoThisWeek );
		}
		return getGamesThisWeek( pTime ) + ( 1 - intoThisWeek ) * getGamesLastWeek( pTime );
	}
	
	public long getDateLastGame() {
		return Storage.convertLong( mEntity.getProperty( "DateLastGame" ) );
	}
	
	public void setActiveSearch( boolean pBlitz , boolean pFast ) {
		if( pBlitz || pFast ) {
			mEntity.setUnindexedProperty( "Blitz" , pBlitz );
			mEntity.setUnindexedProperty( "Fast" , pFast );
			if( !mEntity.hasProperty( "SearchStartTime" )) {
				mEntity.setIndexedProperty( "SearchStartTime" , System.currentTimeMillis() );
			}
		} else {
			mEntity.removeProperty( "Blitz" );
			mEntity.removeProperty( "Fast" );
			mEntity.removeProperty( "SearchStartTime" );
		}
	}
	
	public boolean isLookingForBlitz() {
		Boolean blitz=(Boolean)mEntity.getProperty( "Blitz" );
		return blitz != null && blitz.booleanValue();
	}
	
	public boolean isLookingForFast() {
		Boolean fast=(Boolean)mEntity.getProperty( "Fast" );
		return fast != null && fast.booleanValue();
	}

	public boolean isBot() {
		Boolean bot=(Boolean)mEntity.getProperty( "Bot" );
		return bot != null && bot.booleanValue();
	}
	
	public long getSearchStartTime() {
		return Storage.convertLong( mEntity.getProperty( "SearchStartTime" ) );
	}

	public void setChannelId( long pChannelId , boolean pBot ) {
		mEntity.setUnindexedProperty( "ChannelId" , pChannelId ); 
		if( pBot ) {
			mEntity.setIndexedProperty( "Bot" , true );
		} else {
			mEntity.removeProperty( "Bot" );
		}
	}

	public long getChannelId() {
		return Storage.convertLong( mEntity.getProperty( "ChannelId" ) );
	}

	public void fixGamesPerWeek( int pThisWeekGames , int pLastWeekGames ) {
		mEntity.setUnindexedProperty( "GamesThisWeek" , pThisWeekGames );
		mEntity.setUnindexedProperty( "GamesLastWeek" , pLastWeekGames );
	}
	
}
