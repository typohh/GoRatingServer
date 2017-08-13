package typo.ranking.server.shared.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import typo.ranking.server.shared.JsonWriter;
import typo.ranking.server.shared.Move;

public class DeadListMessage implements Message {

	private long mGameId;
	private ArrayList<Move> mDeadList=new ArrayList<Move>();
	private long mMessageId;
	private double mScore;
	
	private DeadListMessage() { }
	
	public DeadListMessage( long pMessageId , long pGameId , double pScore ) {
		mGameId=pGameId;
		mMessageId=pMessageId;
		mScore=pScore;
	}
	
	public void addDead( Move pMove ) {
		mDeadList.add( pMove );
	}
	
	@Override
	public String toJson() {
		Vector<String> deadList=new Vector<String>();
		for( Move move : mDeadList ) {
			deadList.add( move.toJson() );
		}
		JsonWriter jw = new JsonWriter();
		jw.add( "type" , "deadlist" );
		jw.add( "messageId" , Long.toString( mMessageId ) );
		jw.add( "gameId" , Long.toString( mGameId ) );
		jw.add( "score" , mScore );
		jw.add( "dead" , deadList );
		return jw.toString();
	}

	public Collection<Move> getMoves() {
		return mDeadList;
	}

	@Override
	public long getMessageId() {
		return mMessageId;
	}
	
}
