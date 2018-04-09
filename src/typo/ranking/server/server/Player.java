package typo.ranking.server.server;

import java.util.Calendar;
import java.util.TimeZone;

import typo.ranking.server.server.HehRating.GlobalParameters;
import typo.ranking.server.server.servlets.EndGameServlet;

public class Player {

	private long mId;
	private String mName;
	private long mCreatedDate;
	private long mLoginDate;
	private int mNumberOfGames;
	private HehRating mRating;
	private long mLastOpponent;
	private long mActiveGame;
	private long mWatchingGame;
	private boolean mBot;
	
	
	private boolean mSearchBlitz;
	private boolean mSearchFast;
	private long mSearchTime;
	
	public Player() {
	}
	
	public Player( String pName ) {
		mName=pName;
		long time=System.currentTimeMillis();
		mCreatedDate=time;
		mLoginDate=time;
		mNumberOfGames=0;
		GlobalParameters gp=EndGameServlet.getGlobalParameters();
		mRating = new HehRating( gp.getMean() , gp.getStdDev() );
	}

	public String getName() {
		return mName;
	}

	public long getDateCreated() {
		return mCreatedDate;
	}
	
	public long getLastLogin() {
		return mLoginDate;
	}

	public HehRating getRating() { // only set rating when player actually plays a game, this way wont get on leaderboard before playing a game..
		return mRating;
	}
	
	public void setRating( HehRating pRating ) {
		mRating=pRating;
	}
	
	public int getNumberOfGames() {
		return mNumberOfGames;
	}

	public void setLastOpponent( long pId ) {
		mLastOpponent=pId;
	}

	public long getLastOpponent() {
		return mLastOpponent;
	}
	
	public void setName( String pName ) {
		mName=pName;
	}

	public long getActiveGame() {
		return mActiveGame;
	}
	
	public void setWatchingGame( long pId ) {
		mWatchingGame=pId;
	}
	
	public long getWatchingGame() {
		return mWatchingGame;
	}
	
	public void setActiveGame( long pId ) {
		mActiveGame=0;
		if( pId != 0 ) {
			mNumberOfGames+=1;
			mActiveGame=pId;
		}
		
	}

	public void setActiveSearch( boolean pBlitz , boolean pFast ) {
		if( pBlitz || pFast ) {
			mSearchBlitz = pBlitz;
			mSearchFast = pFast;
			if( mSearchTime == 0 ) {
				mSearchTime=System.currentTimeMillis();
			}
		} else {
			mSearchBlitz=false;
			mSearchFast=false;
			mSearchTime=0;
		}
	}
	
	public boolean isLookingForBlitz() {
		return mSearchBlitz;
	}
	
	public boolean isLookingForFast() {
		return mSearchFast;
	}

	public boolean isBot() {
		return mBot;
	}
	
	public long getSearchStartTime() {
		return mSearchTime;
	}

}
