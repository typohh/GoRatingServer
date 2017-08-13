package typo.ranking.server.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import typo.ranking.server.server.Game;
import typo.ranking.server.server.Player;
import typo.ranking.server.server.Storage;
import typo.ranking.server.shared.Move;

public class SgfServlet extends HttpServlet {

	Logger logger = Logger.getLogger( "debug" );

	@Override
	protected void doGet( HttpServletRequest pReq , HttpServletResponse pResp ) throws ServletException , IOException {
		String path = pReq.getPathTranslated();
//		logger.severe( "requesting sgf " + path );
		if( !path.toLowerCase().endsWith( ".sgf" ) ) {
			throw new Error();
		}
		int index = path.substring( 0 , path.length() - 4 ).lastIndexOf( '/' );
		if( index == -1 ) {
			index = path.substring( 0 , path.length() - 4 ).lastIndexOf( '\\' ); // hack because local shit..
		}
		if( index == -1 ) {
			throw new Error( "cant parse " + path.substring( 0 , path.length() - 4 ) );
		}
		long gameId = Long.parseLong( path.substring( index + 1 , path.length() - 4 ) );
		Game game = Storage.get().getGameById( gameId );
		Player playerBlack = Storage.get().getPlayerById( game.getPlayerBlack() );
		Player playerWhite = Storage.get().getPlayerById( game.getPlayerWhite() );
		pResp.setCharacterEncoding( "UTF-8" );
		pResp.setContentType( "sgf/txt" );
		PrintWriter writer = pResp.getWriter();
		final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		writer.append( "(;" );
		writer.append( "GM[1]" );
		writer.append( "CA[UTF-8]" );
		writer.append( "SZ[19]" );
		writer.append( "KM[7.5]" );
		writer.append( "RU[Chinese]" );
		writer.append( "PC[https://goratingserver.appspot.com/]" );
		writer.append( "DT[" + dateFormat.format( game.getDateCreated() ) + "]" );
		writer.append( "PB[" + playerBlack.getName() + "]" );
		writer.append( "PW[" + playerWhite.getName() + "]" );
		writer.append( "OT[" + game.getPeriodCount() + "x" + (game.getPeriodTime() / 1000) + "byo-yomi]" );
		if( game.isCompleted() ) {
			boolean winnerBlack = game.getWinner() == playerBlack.getId();
			if( game.getLastMove() == Move.TimeLoss ) {
				if( winnerBlack ) {
					writer.append( "RE[B+Time]" );
				} else {
					writer.append( "RE[W+Time]" );
				}
			} else if( game.getLastMove() == Move.Resign ) {
				if( winnerBlack ) {
					writer.append( "RE[B+Res]" );
				} else {
					writer.append( "RE[W+Res]" );
				}
			} else {
				if( winnerBlack ) {
					writer.append( "RE[B+" + game.getScore() + "]" );
				} else {
					writer.append( "RE[W+" + (-game.getScore()) + "]" );
				}
			}
		}
		writer.append( "C[ B=" + playerBlack.getRating().getRating().toString() + " W=" + playerWhite.getRating().getRating().toString() + "]" );
		boolean blackTurn=true;
		for( Move move : game.getMoves() ) {
			if( blackTurn ) {
				if( move.isMove() ) {
					writer.append( ";B[" + move.toJson() + "]" );
				} else if( move != Move.Resign && move != Move.TimeLoss ){
					writer.append( ";B[]" );
				}
			} else {
				if( move.isMove() ) {
					writer.append( ";W[" + move.toJson() + "]" );
				} else if( move != Move.Resign && move != Move.TimeLoss ){
					writer.append( ";W[]" );
				}
			}
			blackTurn=!blackTurn;
		}
		if( game.getLastMove() == Move.Pass && game.getSecondLastMove() == Move.Pass ) {
			if( game.hasDead() ) {
				Move[] deads=game.getDead();
				if( deads.length > 0  ) {
					writer.append( "TR" );
					for( Move dead : deads ) {
						writer.append( "[" + dead.toJson() + "]" );
					}
				}
			}
		}
		
		writer.append( ")" );
		writer.flush();
	}

}
