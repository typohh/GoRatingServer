package typo.ranking.server.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.OldVersionException;
import typo.ranking.server.shared.Version;

public class RestfulServlet extends HttpServlet {

	Logger logger = Logger.getLogger( "debug" );

	@Override
	protected void doGet( HttpServletRequest pReq , HttpServletResponse pResp ) throws ServletException , IOException {
		try {
			String cmd = pReq.getParameter( "c" );
			if( cmd.equals( "rfp" ) ) {
				long userId = Long.parseLong( pReq.getParameter( "u" ) );
				boolean blitz = Boolean.parseBoolean( pReq.getParameter( "b" ) );
				boolean fast = Boolean.parseBoolean( pReq.getParameter( "f" ) );
				DogFood.registerForPairing( userId , blitz , fast );
				return;
			}
			if( cmd.equals( "rwg" ) ) {
				long userId = Long.parseLong( pReq.getParameter( "u" ) );
				long gameId = Long.parseLong( pReq.getParameter( "g" ) );
				DogFood.registerWatchGame( userId , gameId );
				return;
			}
			if( cmd.equals( "sm" ) ) { // send move..
				long userId = Long.parseLong( pReq.getParameter( "u" ) );
				short moveNumber = Short.parseShort( pReq.getParameter( "n" ) );
				Move move = Move.getMove( pReq.getParameter( "m" ) );
				try {
					DogFood.sendMove( userId , moveNumber , move );
				} catch( Error pE ) {
					logger.severe( "resyncing because " + pE.getMessage() );
					DogFood.initialize( userId );
				}
				return;
			}
			if( cmd.equals( "a" ) ) {
				long userId = Long.parseLong( pReq.getParameter( "u" ) );
				long messageId = Long.parseLong( pReq.getParameter( "m" ) );
				DogFood.ack( userId , messageId );
				return;
			}
			if( cmd.equals( "i" ) ) {
				long userId = Long.parseLong( pReq.getParameter( "u" ) );
				DogFood.initialize( userId );
				return;
			}
			if( cmd.equals( "cui" ) ) {
				PrintWriter writer = pResp.getWriter();
				writer.print( Long.toString( DogFood.createUserId() ) );
				writer.flush();
				return;
			}
			if( cmd.equals( "cn" ) ) {
				long userId = Long.parseLong( pReq.getParameter( "u" ) );
				DogFood.changeName( userId );
				return;
			}
			if( cmd.equals( "gcs" ) ) {
				long userId = Long.parseLong( pReq.getParameter( "u" ) );
				long active = Long.parseLong( pReq.getParameter( "a" ) );
				double version = Double.parseDouble( pReq.getParameter( "v" ) );
				if( version != Version.sVersionHuman && version != Version.sVersionBot ) {
					pResp.sendError( 412 , "Old version of protocol." );
				}
				PrintWriter writer = pResp.getWriter();
				try {
					writer.print( DogFood.getChannelSecret( userId , active , version == Version.sVersionBot ) );
				} catch( OldVersionException pE ) {
				}
				writer.flush();
				return;
			}
			if( cmd.equals( "ggd" ) ) {
				PrintWriter writer = pResp.getWriter();
				writer.print( DogFood.getGameData().toJson() );
				writer.flush();
				return;
			}
			if( cmd.equals( "glb" ) ) {
				PrintWriter writer = pResp.getWriter();
				writer.print( DogFood.getLeaderboard().toJson() );
				writer.flush();
				return;
			}
			logger.severe( "unknown command : " + cmd );
		} catch( Throwable pT ) {
			logger.log( Level.SEVERE , "rest failed" , pT );
		}
		pResp.sendError( 500 , "ohnoes! something went wrong..." );
	}
}
