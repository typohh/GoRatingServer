package typo.ranking.server.shared;

import java.util.List;

public enum Move {

	A1( 0 , 0 ),
	B1( 1 , 0 ),
	B2( 1 , 1 ),
	A2( 0 , 1 ),
	C1( 2 , 0 ),
	C2( 2 , 1 ),
	C3( 2 , 2 ),
	B3( 1 , 2 ),
	A3( 0 , 2 ),
	D1( 3 , 0 ),
	D2( 3 , 1 ),
	D3( 3 , 2 ),
	D4( 3 , 3 ),
	C4( 2 , 3 ),
	B4( 1 , 3 ),
	A4( 0 , 3 ),
	E1( 4 , 0 ),
	E2( 4 , 1 ),
	E3( 4 , 2 ),
	E4( 4 , 3 ),
	E5( 4 , 4 ),
	D5( 3 , 4 ),
	C5( 2 , 4 ),
	B5( 1 , 4 ),
	A5( 0 , 4 ),
	F1( 5 , 0 ),
	F2( 5 , 1 ),
	F3( 5 , 2 ),
	F4( 5 , 3 ),
	F5( 5 , 4 ),
	F6( 5 , 5 ),
	E6( 4 , 5 ),
	D6( 3 , 5 ),
	C6( 2 , 5 ),
	B6( 1 , 5 ),
	A6( 0 , 5 ),
	G1( 6 , 0 ),
	G2( 6 , 1 ),
	G3( 6 , 2 ),
	G4( 6 , 3 ),
	G5( 6 , 4 ),
	G6( 6 , 5 ),
	G7( 6 , 6 ),
	F7( 5 , 6 ),
	E7( 4 , 6 ),
	D7( 3 , 6 ),
	C7( 2 , 6 ),
	B7( 1 , 6 ),
	A7( 0 , 6 ),
	H1( 7 , 0 ),
	H2( 7 , 1 ),
	H3( 7 , 2 ),
	H4( 7 , 3 ),
	H5( 7 , 4 ),
	H6( 7 , 5 ),
	H7( 7 , 6 ),
	H8( 7 , 7 ),
	G8( 6 , 7 ),
	F8( 5 , 7 ),
	E8( 4 , 7 ),
	D8( 3 , 7 ),
	C8( 2 , 7 ),
	B8( 1 , 7 ),
	A8( 0 , 7 ),
	I1( 8 , 0 ),
	I2( 8 , 1 ),
	I3( 8 , 2 ),
	I4( 8 , 3 ),
	I5( 8 , 4 ),
	I6( 8 , 5 ),
	I7( 8 , 6 ),
	I8( 8 , 7 ),
	I9( 8 , 8 ),
	H9( 7 , 8 ),
	G9( 6 , 8 ),
	F9( 5 , 8 ),
	E9( 4 , 8 ),
	D9( 3 , 8 ),
	C9( 2 , 8 ),
	B9( 1 , 8 ),
	A9( 0 , 8 ),
	J1( 9 , 0 ),
	J2( 9 , 1 ),
	J3( 9 , 2 ),
	J4( 9 , 3 ),
	J5( 9 , 4 ),
	J6( 9 , 5 ),
	J7( 9 , 6 ),
	J8( 9 , 7 ),
	J9( 9 , 8 ),
	J10( 9 , 9 ),
	I10( 8 , 9 ),
	H10( 7 , 9 ),
	G10( 6 , 9 ),
	F10( 5 , 9 ),
	E10( 4 , 9 ),
	D10( 3 , 9 ),
	C10( 2 , 9 ),
	B10( 1 , 9 ),
	A10( 0 , 9 ),
	K1( 10 , 0 ),
	K2( 10 , 1 ),
	K3( 10 , 2 ),
	K4( 10 , 3 ),
	K5( 10 , 4 ),
	K6( 10 , 5 ),
	K7( 10 , 6 ),
	K8( 10 , 7 ),
	K9( 10 , 8 ),
	K10( 10 , 9 ),
	K11( 10 , 10 ),
	J11( 9 , 10 ),
	I11( 8 , 10 ),
	H11( 7 , 10 ),
	G11( 6 , 10 ),
	F11( 5 , 10 ),
	E11( 4 , 10 ),
	D11( 3 , 10 ),
	C11( 2 , 10 ),
	B11( 1 , 10 ),
	A11( 0 , 10 ),
	L1( 11 , 0 ),
	L2( 11 , 1 ),
	L3( 11 , 2 ),
	L4( 11 , 3 ),
	L5( 11 , 4 ),
	L6( 11 , 5 ),
	L7( 11 , 6 ),
	L8( 11 , 7 ),
	L9( 11 , 8 ),
	L10( 11 , 9 ),
	L11( 11 , 10 ),
	L12( 11 , 11 ),
	K12( 10 , 11 ),
	J12( 9 , 11 ),
	I12( 8 , 11 ),
	H12( 7 , 11 ),
	G12( 6 , 11 ),
	F12( 5 , 11 ),
	E12( 4 , 11 ),
	D12( 3 , 11 ),
	C12( 2 , 11 ),
	B12( 1 , 11 ),
	A12( 0 , 11 ),
	M1( 12 , 0 ),
	M2( 12 , 1 ),
	M3( 12 , 2 ),
	M4( 12 , 3 ),
	M5( 12 , 4 ),
	M6( 12 , 5 ),
	M7( 12 , 6 ),
	M8( 12 , 7 ),
	M9( 12 , 8 ),
	M10( 12 , 9 ),
	M11( 12 , 10 ),
	M12( 12 , 11 ),
	M13( 12 , 12 ),
	L13( 11 , 12 ),
	K13( 10 , 12 ),
	J13( 9 , 12 ),
	I13( 8 , 12 ),
	H13( 7 , 12 ),
	G13( 6 , 12 ),
	F13( 5 , 12 ),
	E13( 4 , 12 ),
	D13( 3 , 12 ),
	C13( 2 , 12 ),
	B13( 1 , 12 ),
	A13( 0 , 12 ),
	N1( 13 , 0 ),
	N2( 13 , 1 ),
	N3( 13 , 2 ),
	N4( 13 , 3 ),
	N5( 13 , 4 ),
	N6( 13 , 5 ),
	N7( 13 , 6 ),
	N8( 13 , 7 ),
	N9( 13 , 8 ),
	N10( 13 , 9 ),
	N11( 13 , 10 ),
	N12( 13 , 11 ),
	N13( 13 , 12 ),
	N14( 13 , 13 ),
	M14( 12 , 13 ),
	L14( 11 , 13 ),
	K14( 10 , 13 ),
	J14( 9 , 13 ),
	I14( 8 , 13 ),
	H14( 7 , 13 ),
	G14( 6 , 13 ),
	F14( 5 , 13 ),
	E14( 4 , 13 ),
	D14( 3 , 13 ),
	C14( 2 , 13 ),
	B14( 1 , 13 ),
	A14( 0 , 13 ),
	O1( 14 , 0 ),
	O2( 14 , 1 ),
	O3( 14 , 2 ),
	O4( 14 , 3 ),
	O5( 14 , 4 ),
	O6( 14 , 5 ),
	O7( 14 , 6 ),
	O8( 14 , 7 ),
	O9( 14 , 8 ),
	O10( 14 , 9 ),
	O11( 14 , 10 ),
	O12( 14 , 11 ),
	O13( 14 , 12 ),
	O14( 14 , 13 ),
	O15( 14 , 14 ),
	N15( 13 , 14 ),
	M15( 12 , 14 ),
	L15( 11 , 14 ),
	K15( 10 , 14 ),
	J15( 9 , 14 ),
	I15( 8 , 14 ),
	H15( 7 , 14 ),
	G15( 6 , 14 ),
	F15( 5 , 14 ),
	E15( 4 , 14 ),
	D15( 3 , 14 ),
	C15( 2 , 14 ),
	B15( 1 , 14 ),
	A15( 0 , 14 ),
	P1( 15 , 0 ),
	P2( 15 , 1 ),
	P3( 15 , 2 ),
	P4( 15 , 3 ),
	P5( 15 , 4 ),
	P6( 15 , 5 ),
	P7( 15 , 6 ),
	P8( 15 , 7 ),
	P9( 15 , 8 ),
	P10( 15 , 9 ),
	P11( 15 , 10 ),
	P12( 15 , 11 ),
	P13( 15 , 12 ),
	P14( 15 , 13 ),
	P15( 15 , 14 ),
	P16( 15 , 15 ),
	O16( 14 , 15 ),
	N16( 13 , 15 ),
	M16( 12 , 15 ),
	L16( 11 , 15 ),
	K16( 10 , 15 ),
	J16( 9 , 15 ),
	I16( 8 , 15 ),
	H16( 7 , 15 ),
	G16( 6 , 15 ),
	F16( 5 , 15 ),
	E16( 4 , 15 ),
	D16( 3 , 15 ),
	C16( 2 , 15 ),
	B16( 1 , 15 ),
	A16( 0 , 15 ),
	Q1( 16 , 0 ),
	Q2( 16 , 1 ),
	Q3( 16 , 2 ),
	Q4( 16 , 3 ),
	Q5( 16 , 4 ),
	Q6( 16 , 5 ),
	Q7( 16 , 6 ),
	Q8( 16 , 7 ),
	Q9( 16 , 8 ),
	Q10( 16 , 9 ),
	Q11( 16 , 10 ),
	Q12( 16 , 11 ),
	Q13( 16 , 12 ),
	Q14( 16 , 13 ),
	Q15( 16 , 14 ),
	Q16( 16 , 15 ),
	Q17( 16 , 16 ),
	P17( 15 , 16 ),
	O17( 14 , 16 ),
	N17( 13 , 16 ),
	M17( 12 , 16 ),
	L17( 11 , 16 ),
	K17( 10 , 16 ),
	J17( 9 , 16 ),
	I17( 8 , 16 ),
	H17( 7 , 16 ),
	G17( 6 , 16 ),
	F17( 5 , 16 ),
	E17( 4 , 16 ),
	D17( 3 , 16 ),
	C17( 2 , 16 ),
	B17( 1 , 16 ),
	A17( 0 , 16 ),
	R1( 17 , 0 ),
	R2( 17 , 1 ),
	R3( 17 , 2 ),
	R4( 17 , 3 ),
	R5( 17 , 4 ),
	R6( 17 , 5 ),
	R7( 17 , 6 ),
	R8( 17 , 7 ),
	R9( 17 , 8 ),
	R10( 17 , 9 ),
	R11( 17 , 10 ),
	R12( 17 , 11 ),
	R13( 17 , 12 ),
	R14( 17 , 13 ),
	R15( 17 , 14 ),
	R16( 17 , 15 ),
	R17( 17 , 16 ),
	R18( 17 , 17 ),
	Q18( 16 , 17 ),
	P18( 15 , 17 ),
	O18( 14 , 17 ),
	N18( 13 , 17 ),
	M18( 12 , 17 ),
	L18( 11 , 17 ),
	K18( 10 , 17 ),
	J18( 9 , 17 ),
	I18( 8 , 17 ),
	H18( 7 , 17 ),
	G18( 6 , 17 ),
	F18( 5 , 17 ),
	E18( 4 , 17 ),
	D18( 3 , 17 ),
	C18( 2 , 17 ),
	B18( 1 , 17 ),
	A18( 0 , 17 ),
	S1( 18 , 0 ),
	S2( 18 , 1 ),
	S3( 18 , 2 ),
	S4( 18 , 3 ),
	S5( 18 , 4 ),
	S6( 18 , 5 ),
	S7( 18 , 6 ),
	S8( 18 , 7 ),
	S9( 18 , 8 ),
	S10( 18 , 9 ),
	S11( 18 , 10 ),
	S12( 18 , 11 ),
	S13( 18 , 12 ),
	S14( 18 , 13 ),
	S15( 18 , 14 ),
	S16( 18 , 15 ),
	S17( 18 , 16 ),
	S18( 18 , 17 ),
	S19( 18 , 18 ),
	R19( 17 , 18 ),
	Q19( 16 , 18 ),
	P19( 15 , 18 ),
	O19( 14 , 18 ),
	N19( 13 , 18 ),
	M19( 12 , 18 ),
	L19( 11 , 18 ),
	K19( 10 , 18 ),
	J19( 9 , 18 ),
	I19( 8 , 18 ),
	H19( 7 , 18 ),
	G19( 6 , 18 ),
	F19( 5 , 18 ),
	E19( 4 , 18 ),
	D19( 3 , 18 ),
	C19( 2 , 18 ),
	B19( 1 , 18 ),
	A19( 0 , 18 ),
	Pass( 0 , -1 ) ,
	Resign( -1 , -1 ),
	TimeLoss( -2 , -1 ),
	Outside( -3 , -1 ),
	Missing( -4 , -1 );
	
//	Accept( -4 , -1 ),
//	Reject( -5 , -1 );
	
	private Move( int pX , int pY ) {
		mX = (short) pX;
		mY = (short) pY;
	}

	private final short mX;
	private final short mY;

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	public static int getIndex( int pX , int pY ) {
		if( pY > pX ) {
			return pY * pY + pY * 2 - pX;
		} else {
			return pX * pX + pY;
		}
	}

	public static Move getMove( int pX , int pY , int pSize ) {
		if( pX < 0 || pX >= pSize || pY < 0 || pY >= pSize ) {
			return Outside;
		}
		return values()[ getIndex( pX , pY ) ];
	}
	
	public static Move getMove( int pX , int pY ) {
		if( pY == -1 ) {
			return Move.values()[ Pass.ordinal() - pX ];
		}
		return values()[ getIndex( pX , pY ) ];
	}
	
	public boolean isMove() {
		return mY != -1;
	}
	
	public String toJson() {
		if( isMove() ) {
			return (char)( 'a' + mX ) + "" + (char) ( 'a' + mY );
		} else {
			return name();
		}
	}
	
	public static Move getMove( String pJson ) {
		if( pJson.length() == 2 ) {
			return getMove( pJson.charAt( 0 ) - 'a' , pJson.charAt( 1 ) - 'a' );
		} else {
			return Move.valueOf( Move.class , pJson );
		}
	}
	
	public static Move parseGTP( String pMove ) {
		if( pMove.equalsIgnoreCase( "Pass" )) {
			return Pass;
		}
		if( pMove.equalsIgnoreCase( "Resign" )) {
			return Resign;
		}
		int x = Character.toLowerCase( pMove.charAt( 0 ) ) - 'a';
		int y = Integer.parseInt( pMove.substring( 1 ) ) - 1;
		if( x >= ('j' - 'a') ) {
			--x;
		}
		return getMove( x , y );
	}
	
	public String toGtp() {
		if( mY == -1 ) {
			return "Pass";
		}
		char x = (char) ('A' + mX);
		if( x >= 'I' ) {
			++x;
		}
		return x + "" + (int)(1 + mY);
		
	}
	
	public static byte[] toBytes( List<Move> pMoves ) {
		byte[] bytes=new byte[ pMoves.size() * 2 ];
		int index=0;
		for( Move dead : pMoves ) {
			bytes[ index * 2 + 0 ]=(byte)dead.getX();
			bytes[ index * 2 + 1 ]=(byte)dead.getY();
			++index;
		}
		return bytes;
	}
	
	public static Move[] toMoves( byte[] pBytes ) {
		Move[] moves = new Move[pBytes.length / 2];
		for( int i = 0 ; i < moves.length ; ++i ) {
			byte x = pBytes[ i * 2 + 0 ];
			byte y = pBytes[ i * 2 + 1 ];
			moves[i] = Move.getMove( x , y );
		}
		return moves;
	}
	
}
