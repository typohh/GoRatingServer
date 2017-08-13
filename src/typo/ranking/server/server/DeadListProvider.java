package typo.ranking.server.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

import typo.ranking.server.server.servlets.DogFood;
import typo.ranking.server.shared.BoardManagement;
import typo.ranking.server.shared.BoardState;
import typo.ranking.server.shared.JsonWriter;
import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.Stone;

public class DeadListProvider {
	
	private static Logger logger = Logger.getLogger( "debug" );

	public static ArrayList<Move> waitForDeadList( Game pGame ) {
		BoardManagement bm = (BoardManagement) Storage.get().getCache( "board" + pGame.getId() );
		if( bm == null ) {
			bm = DogFood.createBoard( pGame );
			Storage.get().putCache( "board" + pGame.getId() , bm );
		}			
		ArrayList<Move> deadList = new ArrayList<Move>();
		try {
			JsonWriter jw = new JsonWriter();
			Vector<String> black = new Vector<String>();
			Vector<String> white = new Vector<String>();
			for( int x = 0 ; x < 19 ; ++x ) {
				for( int y = 0 ; y < 19 ; ++y ) {
					Move move = Move.getMove( x , y );
					Stone stone = bm.getStone( move );
					if( stone == Stone.Black ) {
						black.add( move.toJson() );
					} else if( stone == Stone.White ) {
						white.add( move.toJson() );
					}
				}
			}
			jw.add( "black" , black );
			jw.add( "white" , white );
			String charset = "UTF-8";
			URLConnection connection = new URL( "http://104.196.49.221/cgi-bin/deadstones.sh" ).openConnection();
			connection.setConnectTimeout( 5000 ); // 5 seconds to open it..
			connection.setReadTimeout( 30000 ); // 30 seconds to read it..
			connection.setDoOutput( true ); // Triggers POST.
			connection.setRequestProperty( "Accept-Charset" , charset );
			connection.setRequestProperty( "Content-Type" , "application/json;charset=" + charset );
			OutputStream output = connection.getOutputStream();
			output.write( jw.toString().getBytes( charset ) );
			output.flush();
			output.close();
			StringBuffer response = new StringBuffer();
			InputStream input = connection.getInputStream();
			for( int c = input.read() ; c != -1 ; c = input.read() ) {
				response.append( (char) c );
			}
			for( String moveText : response.substring( response.indexOf( "[" ) + 1 , response.indexOf( "]" ) ).split( "," ) ) {
				String moveTextTrimmed = moveText.trim();
				if( moveTextTrimmed.length() != 0 ) {
					deadList.add( Move.getMove( moveTextTrimmed.substring( 1 , moveTextTrimmed.length() - 1 ) ) );
				}
			}
		} catch( IOException pE ) {
			logger.severe( "failed to get dead list " + pE.getMessage() );
			BoardState territoryMap = bm.getTerritory( true );
			for( Move move : Move.values() ) {
				if( move.isMove() && !bm.getStone( move ).isEmptyOrKo() ) {
					if( territoryMap.get( move ) != bm.getStone( move ) ) {
						deadList.add( move );
					}
				}
			}
		}
		return deadList;
	}
	
}
