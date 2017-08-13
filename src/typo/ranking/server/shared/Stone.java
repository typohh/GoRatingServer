package typo.ranking.server.shared;

public enum Stone {
	Black , White , Ko , Empty;

	public char toChar() {
		switch( this ) {
			case Black :
				return 'b';
			case White :
				return 'w';
			case Empty :
				return ' ';
			case Ko :
				return 'k';
		}
		throw new Error();
	}

	public Stone reverse() {
		if( this == Black ) {
			return White;
		}
		if( this == White ) {
			return Black;
		}
		return this;
	}
	
	public static Stone getStone( boolean pBlackNotWhite ) {
		if( pBlackNotWhite ) {
			return Black;
		} else {
			return White;
		}
	}

	public boolean isEmptyOrKo() {
		return this == Empty || this == Ko;
	}
	
}