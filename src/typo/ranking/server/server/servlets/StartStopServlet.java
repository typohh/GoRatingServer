package typo.ranking.server.server.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StartStopServlet extends HttpServlet {

	Logger logger = Logger.getLogger( StartStopServlet.class.getName() );

	@Override
	protected void service( HttpServletRequest pRequest , HttpServletResponse pResponse ) throws ServletException , IOException {
		logger.info( "server startup/shutdown " );
	}
}
