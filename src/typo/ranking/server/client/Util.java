package typo.ranking.server.client;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;

public class Util {
	
	final static Logger logger = Logger.getLogger( "debug" );

	public static Throwable unwrap( Throwable pE ) {
		if( pE instanceof UmbrellaException ) {
			UmbrellaException ue = (UmbrellaException) pE;
			if( ue.getCauses().size() == 1 ) {
				return unwrap( ue.getCauses().iterator().next() );
			}
		}
		return pE;
	}

	private static String getMessage (Throwable throwable) {
	    String ret="";
	    while (throwable!=null) {
	            if (throwable instanceof com.google.gwt.event.shared.UmbrellaException){
	                    for (Throwable thr2 :((com.google.gwt.event.shared.UmbrellaException)throwable).getCauses()){
	                            if (ret != "")
	                                    ret += "\nCaused by: ";
	                            ret += thr2.toString();
	                            ret += "\n  at "+getMessage(thr2);
	                    }
	            } else if (throwable instanceof com.google.web.bindery.event.shared.UmbrellaException){
	                    for (Throwable thr2 :((com.google.web.bindery.event.shared.UmbrellaException)throwable).getCauses()){
	                            if (ret != "")
	                                    ret += "\nCaused by: ";
	                            ret += thr2.toString();
	                            ret += "\n  at "+getMessage(thr2);
	                    }
	            } else {
	                    if (ret != "")
	                            ret += "\nCaused by: ";
	                    ret += throwable.toString();
	                    for (StackTraceElement sTE : throwable.getStackTrace())
	                            ret += "\n  at "+sTE;
	            }
	            throwable = throwable.getCause();
	    }

	    return ret;
	}
	
	public static void put( String pKey , String pValue ) {
		Storage storage = Storage.getLocalStorageIfSupported();
		if( storage != null ) {
			storage.setItem( pKey , pValue );
		} else {
			Cookies.setCookie( pKey , pValue , new Date( System.currentTimeMillis() + 1000l * 60l * 60l * 24l * 356l * 2l ) );
		}
	}

	public static String get( String pKey ) {
		Storage storage = Storage.getLocalStorageIfSupported();
		if( storage != null ) {
			String value = storage.getItem( pKey );
			if( value != null ) { // should be no need in the future to fall back..
				return value;
			}
		}
		return Cookies.getCookie( pKey );
	}

	public static void log( Level pLevel , Throwable pThrowable ) {
		Throwable unwrapped = unwrap( pThrowable );
		logger.log( pLevel , pThrowable.getMessage() , unwrapped );
	}

	public static void log( Level pLevel , String pMessage ) {
		logger.log( pLevel , pMessage );
	}

	
}
