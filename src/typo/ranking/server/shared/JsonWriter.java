package typo.ranking.server.shared;

import java.util.ArrayList;
import java.util.Vector;

public class JsonWriter {

	private StringBuffer mBuffer = new StringBuffer( "{" );

	public void add( String pKey , String pValue ) {
		if( mBuffer.length() != 1 ) {
			mBuffer.append( "," );
		}
		mBuffer.append( "\"" + pKey + "\":\"" + pValue + "\"" );
	}

	public void addNative( String pKey , String pValue ) {
		if( mBuffer.length() != 1 ) {
			mBuffer.append( "," );
		}
		mBuffer.append( "\"" + pKey + "\":" + pValue );
	}

	public void add( String pKey , double pValue ) {
		addNative( pKey , Double.toString( pValue ) );
	}

	public void add( String pKey , int pValue ) {
		addNative( pKey , Integer.toString( pValue ) );
	}

	public void add( String pKey , boolean pValue ) {
		addNative( pKey , Boolean.toString( pValue ) );
	}
	
	@Override
	public String toString() {
		return mBuffer.toString() + "}";
	}

	public void add( String pKey , Vector<String> pStrings ) {
		StringBuffer buffer = new StringBuffer( "[" );
		for( String s : pStrings ) {
			if( buffer.length() != 1 ) {
				buffer.append( "," );
			}
			buffer.append( "\"" + s + "\"" );
		}
		addNative( pKey , buffer.toString() + "]" );
	}

	public void addNative( String pKey , ArrayList<String> pStrings ) {
		StringBuffer buffer = new StringBuffer( "[" );
		for( String s : pStrings ) {
			if( buffer.length() != 1 ) {
				buffer.append( "," );
			}
			buffer.append( s );
		}
		addNative( pKey , buffer.toString() + "]" );
	}

}
