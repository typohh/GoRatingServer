package typo.ranking.server.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.Rating;

public class JavaScriptMessage extends JavaScriptObject {

	protected JavaScriptMessage() {}

	public final native JsArray<JavaScriptMessage> getGames() /*-{
		return this.games;
	}-*/;

	public final native JsArray<JavaScriptMessage> getPlayers() /*-{
		return this.players;
	}-*/;

	public final native String getType() /*-{
		return this.type;
	}-*/;

	private final native String getGameIdString() /*-{
		return this.gameId;
	}-*/;

	public final long getGameId() {
		return Long.parseLong( getGameIdString() );
	}

	public final native int getMovesLength() /*-{
		return this.moves.length;
	}-*/;

	public final native int getNumberOfMoves() /*-{
		return this.numberOfMoves;
	}-*/;

	private final native String getMoveString( int pIndex ) /*-{
		return this.moves[pIndex];
	}-*/;

	public final Move getMove( int pIndex ) {
		return Move.getMove( getMoveString( pIndex ) );
	}

	private final native String getDeadString( int pIndex ) /*-{
		return this.dead[pIndex];
	}-*/;

	public final native int getNumberOfDead() /*-{
		return this.dead.length;
	}-*/;

	public final Move getDead( int pIndex ) {
		return Move.getMove( getDeadString( pIndex ) );
	}

	public final native boolean hasDead() /*-{
		return this.hasDead;
	}-*/;

	private final native String getDateCreatedString() /*-{
		return this.dateCreated;
	}-*/;

	public final long getDateCreated() {
		return Long.parseLong( getDateCreatedString() );
	}

	public final native int getTimePerPeriod() /*-{
		return this.timePerPeriod;
	}-*/;

	public final native int getTimeSinceLastMove() /*-{
		return this.lastMoveTime;
	}-*/;

	public final native String getBlackName() /*-{
		return this.blackName;
	}-*/;

	private final native int getBlackRatingMean() /*-{
		return this.blackRatingMean;
	}-*/;

	private final native int getBlackRatingSD() /*-{
		return this.blackRatingSD;
	}-*/;

	public final Rating getBlackRating() {
		return new Rating( getBlackRatingMean() , getBlackRatingSD() );
	}

	public final native String getWhiteName() /*-{
		return this.whiteName;
	}-*/;

	private final native int getWhiteRatingMean() /*-{
		return this.whiteRatingMean;
	}-*/;

	private final native int getWhiteRatingSD() /*-{
		return this.whiteRatingSD;
	}-*/;

	public final Rating getWhiteRating() {
		return new Rating( getWhiteRatingMean() , getWhiteRatingSD() );
	}

	public final native int getMoveNumber() /*-{
		return this.moveNumber;
	}-*/;

	private final native String getMoveString() /*-{
		return this.move;
	}-*/;

	public final Move getMove() {
		return Move.getMove( getMoveString() );
	}

	public final native int getBlackPeriods() /*-{
		return this.blackPeriods;
	}-*/;

	public final native int getWhitePeriods() /*-{
		return this.whitePeriods;
	}-*/;

	private final native String getUserIdString() /*-{
		return this.userId;
	}-*/;

	public final long getUserId() {
		return Long.parseLong( getUserIdString() );
	}

	private final native String getMessageIdString() /*-{
		return this.messageId;
	}-*/;

	public final long getMessageId() {
		return Long.parseLong( getMessageIdString() );
	}

	public final native String getName() /*-{
		return this.name;
	}-*/;

	private final native int getRatingMean() /*-{
		return this.ratingMean;
	}-*/;

	private final native int getRatingSD() /*-{
		return this.ratingSD;
	}-*/;

	public final Rating getRating() {
		return new Rating( getRatingMean() , getRatingSD() );
	}

	public final native int getNumberOfGames() /*-{
		return this.numberOfGames;
	}-*/;

	public final native String getActiveString() /*-{
		return this.active;
	}-*/;

	public final long getActive() {
		return Long.parseLong( getActiveString() );
	}

	public final native boolean getBlitz() /*-{
		return this.blitz;
	}-*/;

	public final native boolean getFast() /*-{
		return this.fast;
	}-*/;

	public final native double getScore() /*-{
		return this.score;
	}-*/;

	public final native double getRecentGames() /*-{
		return this.recentGames;
	}-*/;

	public final native int getPosition() /*-{
		return this.position;
	}-*/;
}
