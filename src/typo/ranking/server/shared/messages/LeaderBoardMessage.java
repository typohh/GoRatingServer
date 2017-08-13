package typo.ranking.server.shared.messages;

import java.util.ArrayList;

import typo.ranking.server.shared.JsonWriter;
import typo.ranking.server.shared.Rating;

public class LeaderBoardMessage implements Message {
	
	public static class LeaderBoardEntry  {
		
		private int mPosition;
		private String mName;
		private Rating mRating;
		private double mRecentGames;

		public LeaderBoardEntry() { }
		
		public LeaderBoardEntry( int pPosition , String pName , Rating pRating , double pRecentGames ) {
			mPosition=pPosition;
			mName = pName;
			mRating = pRating;
			mRecentGames=pRecentGames;
		}

		public int compareTo( LeaderBoardEntry pO ) {
			return Integer.compare( pO.mRating.getMean() , mRating.getMean() );
		}

		public String getName() {
			return mName;
		}
		
		public Rating getRating() {
			return mRating;
		}

		public int getPosition() {
			return mPosition;
		}

		public double getRecentGames() {
			return mRecentGames;
		}
		
		public String toJson() {
			JsonWriter jw=new JsonWriter();
			jw.add( "position" , mPosition );
			jw.add( "name" , mName );
			jw.add( "ratingMean" , mRating.getMean() );
			jw.add( "ratingSD" , mRating.getSD() );
			jw.add( "recentGames" , mRecentGames );
			return jw.toString();
		}
		
	}
	
	private ArrayList<LeaderBoardEntry> mPlayers=new ArrayList<LeaderBoardEntry>();
	private long mMessageId;

	public int getNumberOfPlayers() {
		return mPlayers.size();
	}
	
	public LeaderBoardEntry getPlayer( int pIndex ) {
		return mPlayers.get( pIndex );
	}
	
	public void add( int pPosition , String pName , Rating pRating , double pRecentGames ) {
		mPlayers.add( new LeaderBoardEntry( pPosition , pName , pRating , pRecentGames ));
	}
	
	public LeaderBoardMessage( long pMessageId ) {
		mMessageId=pMessageId;
	}

	@Override
	public long getMessageId() {
		return mMessageId;
	}

	@Override
	public String toJson() {
		JsonWriter jw=new JsonWriter();
		jw.add( "type" , "leaderboard" );
		jw.add( "messageId" , Long.toString( mMessageId ));
		ArrayList<String> entries=new ArrayList<String>();
		for( LeaderBoardEntry e : mPlayers ) {
			entries.add( e.toJson() );
		}
		jw.addNative( "players" , entries );
		return jw.toString();
	}
	
}
