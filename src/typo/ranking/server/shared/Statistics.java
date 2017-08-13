package typo.ranking.server.shared;

import java.io.Serializable;

public class Statistics implements Serializable {
	
	public Statistics() {
	}
	
	private int mNumberOfPlayersLooking;
	private double mDailyAverage;
	
	public int getNumberOfPlayersLooking() {
		return mNumberOfPlayersLooking;
	}
	
	public void setNumberOfPlayersLooking( int pNumberOfPlayersLooking ) {
		mNumberOfPlayersLooking = pNumberOfPlayersLooking;
	}
	
	public double getDailyAverage() {
		return mDailyAverage;
	}
	
	public void setDailyAverage( double pDailyAverage ) {
		mDailyAverage = pDailyAverage;
	}
	
}
