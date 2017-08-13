package typo.ranking.server.server;

public class HeartBeat {
	
	public static long sNoValue = Long.MIN_VALUE;

	public static long getOutstandingRequest( long pUserId ) {
		Long outstanding = (Long) Storage.get().getCache( "outstanding" + pUserId );
		if( outstanding != null ) {
			return outstanding;
		}
		return sNoValue;
	}
	
	public static void setOutstandingRequest( long pUserId ) {
		Storage.get().putCache( "outstanding" + pUserId , System.currentTimeMillis() );
	}
	
	public static long getPing( long pUserId ) {
		Long ping = (Long) Storage.get().getCache( "ping" + pUserId );
		if( ping != null ) {
			return ping;
		}
		return sNoValue;
	}

	public static void setPing( long pUserId ) {
		Storage.get().putCache( "ping" + pUserId , System.currentTimeMillis() );
		Storage.get().delCache( "outstanding" + pUserId );
	}

}
