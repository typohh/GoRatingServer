package typo.ranking.server.shared.messages;

import java.io.Serializable;

import typo.ranking.server.shared.JsonWriter;
import typo.ranking.server.shared.Rating;

public class UserDataMessage implements Serializable , Message {
	
	private UserDataMessage() { }
	
	private boolean mBlitz;
	private boolean mFast;

	private long mMessageId;
	private String mName;
	private long mUserId;
	private Rating mRating;
	private int mNumberOfGames;
	private long mServerTime;
	
	public UserDataMessage( long pMessageId , String pName , long pUserId , Rating pRating , int pNumberOfGames , boolean pBlitz, boolean pFast ) {
		mMessageId=pMessageId;
		mName=pName;
		mUserId=pUserId;
		mRating=pRating;
		mNumberOfGames=pNumberOfGames;
		mBlitz=pBlitz;
		mFast=pFast;
	}

	public boolean isSearchingBlitz() {
		return mBlitz;
	}
	
	public boolean isSearchingFast() {
		return mFast;
	}
	
	public long getUserId() {
		return mUserId;
	}
	
	public String getName() {
		return mName;
	}
	
	public Rating getRating() {
		return mRating;
	}
	
	public int getNumberOfGames() {
		return mNumberOfGames;
	}

	public void setName( String pName ) {
		mName=pName;
	}

	@Override
	public String toJson() {
		JsonWriter jw=new JsonWriter();
		jw.add( "type" , "userdata" );
		jw.add( "messageId" , Long.toString( mMessageId ) );
		jw.add( "blitz" , mBlitz );
		jw.add( "fast" , mFast );
		jw.add( "name" , mName );
		jw.add( "userId" , Long.toString( mUserId ) );
		jw.add( "ratingMean" , mRating.getMean() );
		jw.add( "ratingSD" , mRating.getSD() );
		jw.add( "numberOfGames" , mNumberOfGames );
		return jw.toString();
	}

	public void setSearch( boolean pBlitz , boolean pFast ) {
		mBlitz=pBlitz;
		mFast=pFast;
	}

	@Override
	public long getMessageId() {
		return mMessageId;
	}
	
}
