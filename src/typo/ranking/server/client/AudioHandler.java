package typo.ranking.server.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.Timer;

public class AudioHandler {

	private Audio mAudio;
	
	public AudioHandler( LanguageMessages pMessages ) {
		mAudioUrl = pMessages.audioFile();
		mStoneLength = Integer.parseInt( pMessages.audioStone() );
		mOneLength = Integer.parseInt( pMessages.audioOne() );
		mTwoLength = Integer.parseInt( pMessages.audioTwo() );
		mThreeLength = Integer.parseInt( pMessages.audioThree() );
		mReadyLength = Integer.parseInt( pMessages.audioReady() );
		mPassLength = Integer.parseInt( pMessages.audioPass() );
		mResignLength = Integer.parseInt( pMessages.audioResign() );
		mLastOvertimeLength = Integer.parseInt( pMessages.audioLastOvertime() );
		
		mStoneStart=1;
		mOneStart=mStoneStart + mStoneLength + 1000;
		mTwoStart=mOneStart + mOneLength + 1000;
		mThreeStart=mTwoStart + mTwoLength + 1000;
		mReadyStart=mThreeStart + mThreeLength + 1000;
		mPassStart=mReadyStart + mReadyLength + 1000;
		mResignStart=mPassStart + mPassLength + 1000;
		mLastOvertimeStart=mResignStart + mResignLength + 1000;
	}

	private String mAudioUrl;
	
	private int mStoneStart;
	private int mStoneLength;
	
	private int mOneStart;
	private int mOneLength;
	
	private int mTwoStart;
	private int mTwoLength;
	
	private int mThreeStart;
	private int mThreeLength;
	
	private int mReadyStart;
	private int mReadyLength;
	
	private int mPassStart;
	private int mPassLength;
	
	private int mResignStart;
	private int mResignLength;
	
	private int mLastOvertimeStart;
	private int mLastOvertimeLength;

	Timer mPauser=new Timer() {
		@Override
		public void run() {
			mAudio.pause();
		}
	};
	
	public void warmup() {
		if( mAudio != null ) {
			return;
		}
		mAudio = Audio.createIfSupported();
		if( mAudio != null ) {
			mAudio.setSrc( GWT.getModuleBaseURL() + "sounds/" + mAudioUrl );
			mAudio.load();
			// hack to play some empty, in order to ensure its loaded..
//			mAudio.setCurrentTime( ( mStoneStart + mStoneLength ) / 1000.0 );
//			mAudio.play();
//			mPauser.schedule( 0 );
			
			String volumeText = Util.get( "volume" );
			if( volumeText != null ) {
				mVolume = Integer.parseInt( volumeText );
			} else {
				mVolume = 5;
			}
			updateVolume( getVolume() );
		}
	}

	public double getVolume() {
		return mVolume / 10.0;
	}

	private int mVolume;

	private void updateVolume( double pVolume ) {
		mAudio.setVolume( pVolume );
	}

	public void changeVolume( boolean pIncrease ) {
		if( pIncrease ) {
			if( mVolume < 10 ) {
				++mVolume;
			}
		} else {
			if( mVolume > 0 ) {
				--mVolume;
			}
		}
		updateVolume( getVolume() );
		Util.put( "volume" , Integer.toString( mVolume ) );
	}

	public void playOne() {
		if( mAudio != null ) {
			mAudio.setCurrentTime( mOneStart / 1000.0 );
			mAudio.play();
			mPauser.schedule( mOneLength );
		}
	}

	public void playTwo() {
		if( mAudio != null ) {
			mAudio.setCurrentTime( mTwoStart / 1000.0 );
			mAudio.play();
			mPauser.schedule( mTwoLength );
		}
	}

	public void playThree() {
		if( mAudio != null ) {
			mAudio.setCurrentTime( mThreeStart / 1000.0 );
			mAudio.play();
			mPauser.schedule( mThreeLength );
		}
	}

	public void playReady() {
		if( mAudio != null ) {
			mAudio.setCurrentTime( mReadyStart / 1000.0 );
			mAudio.play();
			mPauser.schedule( mReadyLength );
		}
	}

	public void playPass() {
		if( mAudio != null ) {
			mAudio.setCurrentTime( mPassStart / 1000.0 );
			mAudio.play();
			mPauser.schedule( mPassLength );
		}
	}

	public void playStone() {
		if( mAudio != null ) {
			mAudio.setCurrentTime( mStoneStart / 1000.0 );
			mAudio.play();
			mPauser.schedule( mStoneLength );
		}
	}

	public void playResign() {
		if( mAudio != null ) {
			mAudio.setCurrentTime( mResignStart / 1000.0 );
			mAudio.play();
			mPauser.schedule( mResignLength );
		}
	}
	
	public void playLastOvertime() {
		if( mAudio != null ) {
			mAudio.setCurrentTime( mLastOvertimeStart / 1000.0 );
			mAudio.play();
			mPauser.schedule( mLastOvertimeLength );
		}
	}
	
}
