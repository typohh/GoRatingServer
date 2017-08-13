package typo.ranking.server.server;

import java.io.Serializable;

import typo.ranking.server.shared.Rating;

public class HehRating implements Serializable {

	private static int sign( boolean pX ) {
		if( pX ) {
			return 1;
		} else {
			return -1;
		}
	}

	private static double squared( double pX ) {
		return pX * pX;
	}

	private static double cubed( double pX ) {
		return pX * pX * pX;
	}

	private static double logisticSigmoid( double pX ) {
		return 1 / (1 + Math.exp( -pX ));
	}

	private static double getMeanUpdate( double oldRating , double newRating , double opponentsRating , int playerWon , double sd , double opponentsSd , double history ) {
		double t1 = (1 + (opponentsSd * opponentsSd * Math.PI) / 8);
		double t2 = Math.sqrt( t1 );
		double t3 = logisticSigmoid( ((newRating - opponentsRating) * playerWon) / t2 );
		double t4 = Math.log( t3 );
		return ((history * (newRating - oldRating)) / (2 * squared( sd ) * (history - t4)) - (playerWon * (1 - t3)) / t2 + (history * squared( newRating - oldRating ) * playerWon * (1 - t3)) / (4 * t2 * squared( sd ) * squared( history - t4 ))) / (-history / (2 * squared( sd ) * (history - t4)) - (history * (newRating - oldRating) * playerWon * (1 - t3)) / (t2 * squared( sd ) * squared( history - t4 )) - ((1 - t3) * t3) / t1 - (history * squared( newRating - oldRating ) * ((2 * squared( 1 - t3 )) / (t1 * cubed( history - t4 )) - ((1 - t3) * t3) / (t1 * squared( history - t4 )))) / (4 * squared( sd )));
	}

	private static double getNewMean( double oldRating , double opponentsRating , boolean playerWon , double sd , double opponentsSd , double history ) {
		int pw = sign( playerWon );
		double newRating = oldRating;
		double scale = 0.5;
		double previousUpdate = 1;
		for( int i = 0 ; i < 10 ; ++i ) {
			double update = getMeanUpdate( oldRating , newRating , opponentsRating , pw , sd , opponentsSd , history );
			if( Math.abs( previousUpdate ) > Math.abs( update ) ) {
				scale = Math.min( 1 , scale * 2 );
			} else {
				scale *= 0.5;
			}
			newRating += update * scale;
			if( update < 0.00001 ) {
				break;
			}
			previousUpdate = update;
		}
		return newRating;
	}

	private static double getNewStandardDeviation( double newRating , double oldRating , double opponentsRating , int playerWon , double sd , double opponentsSd , double history ) {
		double t1 = 1 + (squared( opponentsSd ) * Math.PI) / 8;
		double t2 = logisticSigmoid( ((newRating - opponentsRating) * playerWon) / Math.sqrt( t1 ) );
		double t3 = Math.log( t2 );
		return Math.sqrt( -1 / (-history / (2 * squared( sd ) * (history - t3)) - (history * (newRating - oldRating) * playerWon * (1 - t2)) / (Math.sqrt( t1 ) * squared( sd ) * squared( history - t3 )) - ((1 - t2) * t2) / (t1) - (history * squared( newRating - oldRating ) * ((2 * squared( 1 - t2 )) / ((t1) * cubed( history - t3 )) - ((1 - t2) * t2) / ((t1) * squared( history - t3 )))) / (4 * squared( sd ))) ) / Math.sqrt( 2 );
	}

	public static double getLikelihood( double pPlayerRating , double pPlayerSD , double pOpponentRating , double pOpponentSD , boolean pPlayerWon ) {
		double variance = squared( pPlayerSD ) + squared( pOpponentSD );
		double multiplier = 1 / Math.sqrt( 1 + 9 * variance / squared( Math.PI ) ); // Mark E. Glickman
		return logisticSigmoid( sign( pPlayerWon ) * multiplier * (pPlayerRating - pOpponentRating) );
	}

	public static double getProbability( HehRating pPlayer , HehRating pOpponent ) {
		double variance = squared( pPlayer.mStandardDeviation ) + squared( pOpponent.mStandardDeviation );
		double multiplier = 1 / Math.sqrt( 1 + Math.PI * variance / 8 );
		return logisticSigmoid( multiplier * (pPlayer.mMean - pOpponent.mMean) );
	}

	private double mMean;
	private double mStandardDeviation;

	public static void update( HehRating pWinner , HehRating pLoser , double pWeight , GlobalParameters pParameters ) {
		double winnerMean = getNewMean( pWinner.mMean , pLoser.mMean , true , pWinner.mStandardDeviation , pLoser.mStandardDeviation , pParameters.mSamples );
		double winnerStandardDeviation = getNewStandardDeviation( winnerMean , pWinner.mMean , pLoser.mMean , 1 , pWinner.mStandardDeviation , pLoser.mStandardDeviation , pParameters.mSamples );
		double loserMean = getNewMean( pLoser.mMean , pWinner.mMean , false , pLoser.mStandardDeviation , pWinner.mStandardDeviation , pParameters.mSamples );
		double loserStandardDeviation = getNewStandardDeviation( loserMean , pLoser.mMean , pWinner.mMean , -1 , pLoser.mStandardDeviation , pWinner.mStandardDeviation , pParameters.mSamples );
		pWinner.mMean += pWeight * (winnerMean - pWinner.mMean);
		pWinner.mStandardDeviation += pWeight * (winnerStandardDeviation - pWinner.mStandardDeviation);
		pLoser.mMean += pWeight * (loserMean - pLoser.mMean);
		pLoser.mStandardDeviation += pWeight * (loserStandardDeviation - pLoser.mStandardDeviation);
		pParameters.add( pWinner.getMean() , pWinner.getWeight() );
		pParameters.add( pLoser.getMean() , pLoser.getWeight() );
	}

	public HehRating( double pMean , double pStandardDeviation ) {
		mMean = pMean;
		mStandardDeviation = pStandardDeviation;
	}

	public double getMean() {
		return mMean;
	}

	public double getStandardDeviation() {
		return mStandardDeviation;
	}

	@Override
	public String toString() {
		return (int) (100 * mMean) / 100.0 + "+-" + (100 * mStandardDeviation) / 100.0;
	}

	public Rating getRating() {
		return new Rating( (int) Math.round( 1500 + mMean * 173 ) , (int) (Math.round( mStandardDeviation * 173 )) );
	}

	public double getWeight() {
		return 1 / (mStandardDeviation * mStandardDeviation);
	}

	public static class GlobalParameters implements Serializable {

		public GlobalParameters( double pSamples ) {
			mSamples = pSamples;
		}

		public GlobalParameters( double pMean , double pM2 , double pWeightSum , double pSamples ) {
			mSamples = pSamples;
		}

		private double mSamples;
		private double mMean = 0;
		private double mM2 = 3 * 18;
		private double mWeightSum = 18;

		public void add( double pMean , double pWeight ) {
			double temp = pWeight + mWeightSum;
			double delta = pMean - mMean;
			double R = delta * pWeight / temp;
			mMean += R;
			mM2 += mWeightSum * delta * R;
			mWeightSum = temp;
		}

		public double getStdDev() {
			return Math.sqrt( mM2 / mWeightSum );
		}

		public double getMean() {
			return mMean;
		}

		public double getLogSamples() {
			return mSamples;
		}

		public double getCumulativeMean() {
			return mMean;
		}

		public double getM2() {
			return mM2;
		}

		public double getWeightSum() {
			return mWeightSum;
		}
	}

	public static void main( String[] pArg ) {
		for( int i = 0 ; i < 30 ; ++i ) {
			HehRating a = new HehRating( (1500 - 1500) / 173.0 , 67 / 173.0 );
			HehRating b = new HehRating( (1500 + i * 10 - 1500) / 173.0 , 65 / 173.0 );
			double before = a.getMean();
			update( a , b , 1 , new GlobalParameters( 30 ) );
			System.out.println( (i * 10) + " : " + (a.getMean() - before) * 173.0 );
		}
	}
}
