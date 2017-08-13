package typo.ranking.server.shared;

import java.io.Serializable;

public class Rating implements Serializable {

	protected Rating() {}

	private int mMean;
	private int mSD;

	public int getMean() {
		return mMean;
	}

	public int getSD() {
		return mSD;
	}

	public Rating( int pMean , int pSD ) {
		mMean = pMean;
		mSD = pSD;
	}

	@Override
	public String toString() {
		return mMean + "±" + mSD;
	}

	public String toStringHtml() {
		return mMean + " &plusmn; " + mSD;
	}
}
