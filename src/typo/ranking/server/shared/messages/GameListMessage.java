package typo.ranking.server.shared.messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import typo.ranking.server.shared.JsonWriter;
import typo.ranking.server.shared.Rating;

public class GameListMessage implements Message {

	public static class GameStats {
		
		private long mGameId;
		private String mBlackName;
		private String mWhiteName;
		private Rating mBlackRating;
		private Rating mWhiteRating;
		private int mNumberOfMoves;
		private int mByomi;
		
		protected GameStats() { }

		public GameStats( long pGameId , String pBlackName , String pWhiteName , Rating pBlackRating , Rating pWhiteRating , int pNumberOfMoves , int pByomi ) {
			mGameId = pGameId;
			mBlackName = pBlackName;
			mWhiteName = pWhiteName;
			mBlackRating = pBlackRating;
			mWhiteRating = pWhiteRating;
			mNumberOfMoves = pNumberOfMoves;
			mByomi = pByomi;
		}

		
		public long getGameId() {
			return mGameId;
		}

		
		public String getBlackName() {
			return mBlackName;
		}

		
		public String getWhiteName() {
			return mWhiteName;
		}

		
		public Rating getBlackRating() {
			return mBlackRating;
		}

		
		public Rating getWhiteRating() {
			return mWhiteRating;
		}

		
		public int getNumberOfMoves() {
			return mNumberOfMoves;
		}

		
		public int getByomi() {
			return mByomi;
		}
		
		public String toJson() {
			JsonWriter jw = new JsonWriter();
			jw.add( "gameId" , Long.toString( mGameId ) );
			jw.add( "blackName" , mBlackName );
			jw.add( "whiteName" , mWhiteName );
			jw.add( "blackRatingMean" , mBlackRating.getMean() );
			jw.add( "blackRatingSD" , mBlackRating.getSD() );
			jw.add( "whiteRatingMean" , mBlackRating.getMean() );
			jw.add( "whiteRatingSD" , mBlackRating.getSD() );
			jw.add( "numberOfMoves" , mNumberOfMoves );
			jw.add( "timePerPeriod" , mByomi );
			return jw.toString();
		}
		
		
	}

	private long mMessageId;
	
	public GameListMessage( long pMessageId ) {
		mMessageId=pMessageId;
	}
	
	private ArrayList<GameStats> mGameStats = new ArrayList<GameStats>();

	public void addGame( long pGameId , String pBlackName , String pWhiteName , Rating pBlackRating , Rating pWhiteRating , int pNumberOfMoves , int pByomi ) {
		mGameStats.add( new GameStats( pGameId , pBlackName , pWhiteName , pBlackRating , pWhiteRating , pNumberOfMoves , pByomi ) );
	}

	public void sort() {
		Collections.sort( mGameStats , new Comparator<GameStats>() {

			@Override
			public int compare( GameStats pA , GameStats pB ) {
				return Integer.compare( pA.getBlackRating().getMean() + pA.getWhiteRating().getMean() , pB.getBlackRating().getMean() + pB.getWhiteRating().getMean() );
			}
			
		} );
	}
	
	public int getNumberOfGames() {
		return mGameStats.size();
	}
	
	public GameStats getGame( int pIndex ) {
		return mGameStats.get( pIndex );
	}

	@Override
	public long getMessageId() {
		return mMessageId;
	}

	@Override
	public String toJson() {
		JsonWriter jw = new JsonWriter();
		jw.add( "type" , "gamelist" );
		jw.add( "messageId" , Long.toString( mMessageId ) );
		ArrayList<String> games=new ArrayList<String>();
		for( GameStats gs : mGameStats ) {
			games.add( gs.toJson() );
		}
		jw.addNative( "games" , games );
		return jw.toString();
	}
	
}
