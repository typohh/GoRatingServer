package typo.ranking.server.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

import typo.ranking.server.shared.Rating;

public class PlayerInfo {

	// private GameData mData;
	private long mTimeStart;
	private boolean mBlackNotWhite;
	private long mTimePerPeriod;
	private int mNumberOfPeriods;
	private boolean mActive;
	private Panel mVerticalPanel;
	private Label mNameLabel;
	private Label mRatingLabel;
	private Label mTimeLabel;
	private Label mCapturesLabel;
	private String mBackgroundColor;
	private Style mStyle;

	public String getColor( boolean pBlackNotWhite ) {
		if( pBlackNotWhite ) {
			return "Black";
		} else {
			return "White";
		}
	}

	public PlayerInfo( boolean pBlackNotWhite ) {
		mBlackNotWhite = pBlackNotWhite;
		mNameLabel = Label.wrap( RootPanel.get( "playerName" + getColor( pBlackNotWhite ) ).getElement() );
		mRatingLabel = Label.wrap( RootPanel.get( "playerRating" + getColor( pBlackNotWhite ) ).getElement() );
		mTimeLabel = Label.wrap( RootPanel.get( "playerTime" + getColor( pBlackNotWhite ) ).getElement() );
		mCapturesLabel = Label.wrap( RootPanel.get( "playerCaptures" + getColor( pBlackNotWhite ) ).getElement() );
		mStyle = RootPanel.get( "info" + getColor( mBlackNotWhite ) ).getElement().getStyle();
		mBackgroundColor = mStyle.getBackgroundColor();
	}

	public String twoDigit( long pNumber ) {
		if( pNumber < 10 ) {
			return "0" + pNumber;
		} else {
			return "" + pNumber;
		}
	}

	public Style getStyle() {
		return mStyle;
	}
	
	public void setData( String pName , Rating pRating , int pCaptures , long pStartTime , int pPeriods , long pTimePerPeriod , boolean pActive ) {
		mNameLabel.setText( pName );
		mRatingLabel.setText( pRating.toString() );
		mCapturesLabel.setText( Integer.toString( pCaptures ) );
		mTimeStart = pStartTime;
		mNumberOfPeriods = pPeriods;
		mTimePerPeriod = pTimePerPeriod;
		mActive = pActive;
		updateTime();
	}

	public long getTimeRemaining() {
		return mTimePerPeriod * mNumberOfPeriods - Math.max(0,System.currentTimeMillis() - mTimeStart);
	}

	public long getPeriodTimeRemaining() {
		return getTimeRemaining() % mTimePerPeriod;
	}

	public long getTimePerPeriod() {
		return mTimePerPeriod;
	}
	
	public void updateTime() {
		int periodCount;
		long periodTime;
		if( mActive ) {
			long remaining = getTimeRemaining();
			if( remaining < 0 ) {
				periodCount = 0;
				periodTime = 0;
			} else {
				periodCount = 1 + (int) (getTimeRemaining() / mTimePerPeriod);
				periodTime = getPeriodTimeRemaining();
			}
			mStyle.setBackgroundColor( "rgba(15, 126, 60, 0.7)" );			
//			mStyle.setBackgroundColor( "rgba(255, 255, 0, 0.7)" );
		} else {
			mStyle.setBackgroundColor( mBackgroundColor );
			periodCount = mNumberOfPeriods;
			periodTime = mTimePerPeriod;
		}
		mTimeLabel.setText( "(" + (periodCount) + ") " + twoDigit( periodTime / (60 * 60 * 1000) ) + ":" + twoDigit( (periodTime / (60 * 1000)) % 60 ) + ":" + twoDigit( (periodTime / 1000) % 60 ) );
		if( periodTime < 1000 * 4 ) {
			if( (periodTime / 1000) % 2 == 0 ) {
				mStyle.setBackgroundColor( "rgba(255, 0, 0, 0.7)" );
			} else {
//				mStyle.setBackgroundColor( "rgba(255, 51, 0, 0.7)" );
				mStyle.setBackgroundColor( "rgba(15, 126, 60, 0.7)" );			
//				mStyle.setBackgroundColor( mBackgroundColor );
			}
		}
	}

	public int getNumberOfPerdiods() {
		return mNumberOfPeriods;
	}
}
