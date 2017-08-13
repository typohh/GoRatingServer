package typo.ranking.server.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
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

public class RssServlet extends HttpServlet {

	Logger logger = Logger.getLogger( "debug" );

	@Override
	protected void doGet( HttpServletRequest pReq , HttpServletResponse pResp ) throws ServletException , IOException {
		pResp.setContentType("text/rss+xml; charset=UTF-8");
		pResp.setCharacterEncoding("UTF-8");
		PrintWriter writer = pResp.getWriter();
		writer.append( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" );
		writer.append( "<rss version=\"2.0\">\n" );
		writer.append( "<channel>\n" );
		writer.append( "<link>http://goratingserver.appspot.com</link>\n" );
		writer.append( "<description>Recent games played on the server</description>\n" );
		final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
		for( Game game : Storage.get().listRecentGames( 50 ) ) {
			Player black = Storage.get().getPlayerById( game.getPlayerBlack() );
			Player white = Storage.get().getPlayerById( game.getPlayerWhite() );
			writer.append( "<item>\n" );
			writer.append( "<title>" + white.getName() + " vs " + black.getName() + "</title>\n" );
			writer.append( "<link>http://goratingserver.appspot.com/sgf/" + game.getId() + ".sgf</link>\n" );
			String result="?";
			if( game.isCompleted() ) {
				if( game.getWinner() == black.getId() ) {
					result="B";
				} else {
					result="W";
				}
				if( game.getLastMove() == Move.TimeLoss ) {
					result += "+Time";
				} else if( game.getLastMove() == Move.Resign ) {
					result += "+Resign";
				} else {
					result += "+" + Math.abs( game.getScore() );
				}
			}
			writer.append( "<description>" + sdf.format( game.getDateCreated() ) + " : " + white.getName() + "(W," + white.getRating().getRating() + ") vs " + black.getName() + "(B," + black.getRating().getRating() + ") = " + result + " </description>\n" );
			writer.append( "</item>\n" );
		}
		writer.append( "</channel>" );
		writer.append( "</rss>" );
	}
}
