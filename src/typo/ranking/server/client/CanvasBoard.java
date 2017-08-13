package typo.ranking.server.client;

import java.util.Arrays;
import java.util.LinkedList;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import typo.ranking.server.shared.Stone;

public class CanvasBoard extends Composite {

	private Canvas mCanvas;
	private int mSize;
	private Stone[] mStones;
	private boolean mBlackNotWhiteTurn;
	private Stone[] mTerritory;
	private int mLastX = -1;
	private int mLastY = -1;

	public double getUnit() {
		return mCanvas.getCoordinateSpaceWidth() / (mSize + 0.5);
	}

	public double getScreenX( double pBoardX ) {
		double zoomScreenX = (mZoomX + 0.75) * getUnit();
		return zoomScreenX + (pBoardX - mZoomX) * (mZoomScale * getUnit());
	}

	public double getScreenY( double pBoardY ) {
		double zoomScreenY = (mZoomY + 0.75) * getUnit();
		return zoomScreenY + (pBoardY - mZoomY) * (mZoomScale * getUnit());
	}

	public double getBoardX( double pScreenX ) {
		return (getUnit() * (-0.75 + (mZoomScale - 1) * mZoomX) + pScreenX) / (getUnit() * mZoomScale);
	}

	public double getBoardY( double pScreenY ) {
		return (getUnit() * (-0.75 + (mZoomScale - 1) * mZoomY) + pScreenY) / (getUnit() * mZoomScale);
	}

	private static final CssColor sBlack = CssColor.make( 0 , 0 , 0 );
	private static final CssColor sWhite = CssColor.make( 255 , 255 , 255 );
	private static final CssColor sGray = CssColor.make( 127 , 127 , 127 );
	private static final CssColor sBeige = CssColor.make( 255 , 160 , 50 );

	private void drawStarPoint( Context2d pContext , int pX , int pY ) {
		double x = getScreenX( pX );
		double y = getScreenY( pY );
		double width = Math.max( 3 , mZoomScale * getUnit() / 10.0 );
		pContext.setFillStyle( sBlack );
		pContext.fillRect( x - width / 2 , y - width / 2 , width , width );
	}

	public void setLastMove( int pX , int pY ) {
		mLastX = pX;
		mLastY = pY;
	}

	private void drawGrid( Context2d context ) {
		context.setStrokeStyle( sBlack );
		context.setLineWidth( Math.max( 1 , mZoomScale * getUnit() / 30.0 ) );
		for( int i = 0 ; i < mSize ; ++i ) {
			double icoordinateX = getScreenX( i );
			double icoordinateY = getScreenY( i );
			context.beginPath();
			context.moveTo( icoordinateX , getScreenY( 0 ) );
			context.lineTo( icoordinateX , getScreenY( mSize - 1 ) );
			context.stroke();
			context.beginPath();
			context.moveTo( getScreenX( 0 ) , icoordinateY );
			context.lineTo( getScreenX( mSize - 1 ) , icoordinateY );
			context.stroke();
		}
		final int[] starPointCoordinates = new int[3];
		starPointCoordinates[0] = 3;
		starPointCoordinates[1] = mSize / 2;
		starPointCoordinates[2] = mSize - 4;
		for( int x : starPointCoordinates ) {
			for( int y : starPointCoordinates ) {
				drawStarPoint( context , x , y );
			}
		}
		// draw star points..
	}

	private void drawStones( Context2d context ) {
		int radius = (int) (getScreenX( 0.5 ) - getScreenX( 0 ));
		for( int x = 0 ; x < mSize ; ++x ) {
			for( int y = 0 ; y < mSize ; ++y ) {
				Stone stone = getStone( x , y );
				if( stone == Stone.Black || stone == Stone.White ) {
					double centerX = getScreenX( x );
					double centerY = getScreenY( y );
					if( mShadowLoaded ) {
						double width = 1.2 * radius * 2 * 90 / 48.0;
						double height = 1.2 * radius * 2 * 79 / 48.0;
						context.drawImage( mShadowImage , centerX - width / 2 , centerY - height / 2 , width , height );
					}
				}
			}
		}
		for( int x = 0 ; x < mSize ; ++x ) {
			for( int y = 0 ; y < mSize ; ++y ) {
				Stone stone = getStone( x , y );
				double centerX = getScreenX( x );
				double centerY = getScreenY( y );
				if( stone == Stone.Black || stone == Stone.White ) {
					if( getTerritory( x , y ) == getStone( x , y ).reverse() ) {
						context.setGlobalAlpha( 0.33 );						
					}
					if( mBlackStoneLoaded && mWhiteStoneLoaded ) {
						if( stone == Stone.Black ) {
							context.drawImage( mBlackStoneImage , centerX - radius , centerY - radius , radius * 2 , radius * 2 );
						} else {
							context.drawImage( mWhiteStoneImage , centerX - radius , centerY - radius , radius * 2 , radius * 2 );
						}
					} else {
						context.beginPath();
						if( stone == Stone.Black ) {
							context.setFillStyle( sBlack );
						} else {
							context.setFillStyle( sWhite );
						}
						context.arc( centerX , centerY , radius , 0 , Math.PI * 2 );
						context.closePath();
						context.fill();
						context.beginPath();
						context.setStrokeStyle( sGray );
						context.setLineWidth( Math.min( 1 , radius / 15.0 ) );
						context.arc( centerX , centerY , radius , 0 , Math.PI * 2 );
						context.stroke();
					}
					context.setGlobalAlpha( 1.0 );						
				}
				boolean showCursor = mCursorEnabled && stone == Stone.Empty && x == mCursorX && y == mCursorY;
				if( showCursor ) {
					if( mBlackNotWhiteTurn ) {
						context.setStrokeStyle( sBlack );
					} else {
						context.setStrokeStyle( sWhite );
					}
					if( showCursor ) {
						context.setGlobalAlpha( 0.33 );
					}
					context.beginPath();
					if( mBlackNotWhiteTurn ) {
						context.setFillStyle( sBlack );
					} else {
						context.setFillStyle( sWhite );
					}
					context.arc( centerX , centerY , radius , 0 , Math.PI * 2 );
					context.closePath();
					context.fill();
					context.beginPath();
					context.setStrokeStyle( sGray );
					context.setLineWidth( Math.min( 1 , radius / 15.0 ) );
					context.arc( centerX , centerY , radius , 0 , Math.PI * 2 );
					context.stroke();
					context.setGlobalAlpha( 1.0 );
				}
				if( mLastX == x && mLastY == y ) {
					context.beginPath();
					context.setStrokeStyle( sGray );
					context.setLineWidth( radius / 6.0 );
					context.arc( centerX , centerY , radius / 2 , 0 , Math.PI * 2 );
					context.stroke();
				}
				if( stone == Stone.Ko ) {
					context.beginPath();
					context.setStrokeStyle( CssColor.make( 0 , 0 , 0 ) );
					context.fillRect( centerX - radius / 4 , centerY - radius / 4 , radius / 2 , radius / 2 );
				}
			}
		}
	}

	private void drawTerritory( Context2d context ) {
		int radius = (int) (getScreenX( 0.2 ) - getScreenX( 0 ));
		for( int x = 0 ; x < mSize ; ++x ) {
			for( int y = 0 ; y < mSize ; ++y ) {
				Stone stone = getTerritory( x , y );
				if( stone == Stone.Black || stone == Stone.White ) {
					double centerX = getScreenX( x );
					double centerY = getScreenY( y );
					context.beginPath();
					if( stone == Stone.Black ) {
						context.setFillStyle( sBlack );
					} else {
						context.setFillStyle( sWhite );
					}
					context.rect( centerX - radius , centerY - radius , radius * 2 , radius * 2 );
					context.closePath();
					context.fill();
					context.beginPath();
					context.setStrokeStyle( sGray );
					context.setLineWidth( Math.min( 1 , radius / 15.0 ) );
					context.rect( centerX - radius , centerY - radius , radius * 2 , radius * 2 );
					context.stroke();
				}
			}
		}
	}

//	final Logger logger = Logger.getLogger( "debug" );

	private void drawBackground( Context2d pContext ) {
		if( mBackgroundLoaded ) {
			pContext.drawImage( mBackgroundImage , 0 , 0 , getSize() , getSize() );
		} else {
			pContext.setFillStyle( sBeige );
			pContext.fillRect( 0 , 0 , mCanvas.getCoordinateSpaceWidth() , mCanvas.getCoordinateSpaceHeight() );
		}
	}

	private void drawShadows( Context2d pContext ) {
		if( !isZoomed() ) {
			return;
		}
		if( mZoomX > 0.5 ) {
			// Create gradients
			double x1 = mZoomScale * getUnit();
			CanvasGradient lingrad = pContext.createLinearGradient( 0 , 0 , x1 , 0 );
			lingrad.addColorStop( 0.0f , "rgba(0,0,0,1)" );
			lingrad.addColorStop( 1.0f , "rgba(0,0,0,0)" );
			pContext.beginPath();
			pContext.setFillStyle( lingrad );
			pContext.fillRect( 0 , 0 , x1 , mCanvas.getCoordinateSpaceWidth() );
		}
		if( mZoomX < mSize - 1.5 ) {
			// Create gradients
			double x0 = mCanvas.getCoordinateSpaceWidth() - mZoomScale * getUnit();
			double x1 = mCanvas.getCoordinateSpaceWidth();
			CanvasGradient lingrad = pContext.createLinearGradient( x0 , 0 , x1 , 0 );
			lingrad.addColorStop( 0.0f , "rgba(0,0,0,0)" );
			lingrad.addColorStop( 1.0f , "rgba(0,0,0,1)" );
			pContext.beginPath();
			pContext.setFillStyle( lingrad );
			pContext.fillRect( x0 , 0 , x1 , mCanvas.getCoordinateSpaceWidth() );
		}
		if( mZoomY > 0.5 ) {
			// Create gradients
			double y1 = mZoomScale * getUnit();
			CanvasGradient lingrad = pContext.createLinearGradient( 0 , 0 , 0 , y1 );
			lingrad.addColorStop( 0.0f , "rgba(0,0,0,1)" );
			lingrad.addColorStop( 1.0f , "rgba(0,0,0,0)" );
			pContext.beginPath();
			pContext.setFillStyle( lingrad );
			pContext.fillRect( 0 , 0 , mCanvas.getCoordinateSpaceHeight() , y1 );
		}
		if( mZoomY < mSize - 1.5 ) {
			// Create gradients
			double y0 = mCanvas.getCoordinateSpaceHeight() - mZoomScale * getUnit();
			double y1 = mCanvas.getCoordinateSpaceHeight();
			CanvasGradient lingrad = pContext.createLinearGradient( 0 , y0 , 0 , y1 );
			lingrad.addColorStop( 0.0f , "rgba(0,0,0,0)" );
			lingrad.addColorStop( 1.0f , "rgba(0,0,0,1)" );
			pContext.beginPath();
			pContext.setFillStyle( lingrad );
			pContext.fillRect( 0 , y0 , mCanvas.getCoordinateSpaceHeight() , y1 );
		}
	}

	public void setTurn( boolean pBlackNotWhite ) {
		mBlackNotWhiteTurn = pBlackNotWhite;
	}

	public void draw() {
		Context2d context = mCanvas.getContext2d();
		drawBackground( context );
		drawGrid( context );
		drawStones( context );
		drawTerritory( context );
		drawShadows( context );
	}

	public Stone getStone( int pX , int pY ) {
		return mStones[mSize * pY + pX];
	}

	public void setStone( int pX , int pY , Stone pStone ) {
		mStones[(mSize * pY) + pX] = pStone;
	}

	public void setTerritory( int pX , int pY , Stone pStone ) {
		mTerritory[(mSize * pY) + pX] = pStone;
	}

	public Stone getTerritory( int pX , int pY ) {
		return mTerritory[mSize * pY + pX];
	}

	public void resetTerritory() {
		Arrays.fill( mTerritory , Stone.Empty );
	}

	public void setBoardSize( int pSize ) {
		mSize = pSize;
		mStones = new Stone[pSize * pSize];
		mTerritory = new Stone[pSize * pSize];
		for( int i = 0 ; i < mStones.length ; ++i ) {
			mStones[i] = Stone.Empty;
			mTerritory[i] = Stone.Empty;
		}
		mZoomX = pSize / 2;
		mZoomY = pSize / 2;
		mZoomScale = 1;
	}

	public void setViewSize( int pPixels ) {
		mCanvas.setPixelSize( pPixels , pPixels );
		mCanvas.setCoordinateSpaceWidth( pPixels );
		mCanvas.setCoordinateSpaceHeight( pPixels );
	}

	public int getSize() {
		return mCanvas.getCoordinateSpaceWidth();
	}

	public int getBoardSize() {
		return mSize;
	}

	public void zoomAt( double pX , double pY , double pScale ) {
		mZoomX = pX;
		mZoomY = pY;
		mZoomScale = pScale;
		draw();
	}

	public void unZoom() {
		mZoomX = (mSize - 1) / 2;
		mZoomY = (mSize - 1) / 2;
		mZoomScale = 1;
		draw();
	}

	private double mZoomX;
	private double mZoomY;
	private double mZoomScale;

	public static interface ClickListener {

		public void onClick( double pX , double pY );
	}

	private LinkedList<ClickListener> mListeners = new LinkedList<ClickListener>();

	public void addListener( ClickListener pListener ) {
		mListeners.add( pListener );
	}

	private static final double sMaximumError = 3.0 / 4.0;
	private int mCursorX = -1;
	private int mCursorY = -1;
	private boolean mCursorEnabled = false;

	public void setCursor( boolean pEnabled ) {
		mCursorEnabled = pEnabled;
		if( !mCursorEnabled ) {
			mCursorX = -1;
			mCursorY = -1;
			draw();
		}
	}

	private ImageElement mBackgroundImage;
	private boolean mBackgroundLoaded = false;
	private ImageElement mBlackStoneImage;
	private boolean mBlackStoneLoaded = false;
	private ImageElement mWhiteStoneImage;
	private boolean mWhiteStoneLoaded = false;
	private ImageElement mShadowImage;
	private boolean mShadowLoaded = false;

	public CanvasBoard() {
		Image imageBackground = new Image();
		imageBackground.addLoadHandler( new LoadHandler() {

			@Override
			public void onLoad( LoadEvent pEvent ) {
				mBackgroundLoaded = true;
			}
		} );
		imageBackground.setUrl( GWT.getModuleBaseURL() + "images/shinkaya.jpg" );
		imageBackground.setVisible( false );
		RootPanel.get().add( imageBackground );
		mBackgroundImage = ImageElement.as( imageBackground.getElement() );
		Image imageBlackStone = new Image();
		imageBlackStone.addLoadHandler( new LoadHandler() {

			@Override
			public void onLoad( LoadEvent pEvent ) {
				mBlackStoneLoaded = true;
			}
		} );
		imageBlackStone.setUrl( GWT.getModuleBaseURL() + "images/black.png" );
		imageBlackStone.setVisible( false );
		RootPanel.get().add( imageBlackStone );
		mBlackStoneImage = ImageElement.as( imageBlackStone.getElement() );
		Image imageWhiteStone = new Image();
		imageWhiteStone.addLoadHandler( new LoadHandler() {

			@Override
			public void onLoad( LoadEvent pEvent ) {
				mWhiteStoneLoaded = true;
			}
		} );
		imageWhiteStone.setUrl( GWT.getModuleBaseURL() + "images/white.png" );
		imageWhiteStone.setVisible( false );
		RootPanel.get().add( imageWhiteStone );
		mWhiteStoneImage = ImageElement.as( imageWhiteStone.getElement() );
		Image imageShadow = new Image();
		imageShadow.addLoadHandler( new LoadHandler() {

			@Override
			public void onLoad( LoadEvent pEvent ) {
				mShadowLoaded = true;
			}
		} );
		imageShadow.setUrl( GWT.getModuleBaseURL() + "images/shadow.png" );
		imageShadow.setVisible( false );
		RootPanel.get().add( imageShadow );
		mShadowImage = ImageElement.as( imageShadow.getElement() );
		mCanvas = Canvas.createIfSupported();
		if( mCanvas == null ) {
			Label label = new Label( "This site does not work on the museum artifact you are using." );
			initWidget( label );
			return;
		}
		mCanvas.addMouseOutHandler( new MouseOutHandler() {

			@Override
			public void onMouseOut( MouseOutEvent pEvent ) {
				updateCursor( -1 , -1 );
			}
		} );
		mCanvas.addMouseMoveHandler( new MouseMoveHandler() {

			@Override
			public void onMouseMove( MouseMoveEvent pEvent ) {
				int canvasX = (int) pEvent.getX();
				int canvasY = (int) pEvent.getY();
				double gridX0 = getBoardX( canvasX );
				double gridY0 = getBoardY( canvasY );
				int gridX = (int) Math.round( gridX0 );
				int gridY = (int) Math.round( gridY0 );
				double error = Math.hypot( gridX - gridX0 , gridY - gridY0 );
				if( gridX < 0 || gridX >= mSize ) {
					updateCursor( -1 , -1 );
					return;
				}
				if( gridY < 0 || gridY >= mSize ) {
					updateCursor( -1 , -1 );
					return;
				}
				if( error > sMaximumError ) {
					updateCursor( -1 , -1 );
					return;
				}
				updateCursor( gridX , gridY );
			}
		} );
		setBoardSize( 19 );
		unZoom();
		mCanvas.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				int canvasX = pEvent.getX();
				int canvasY = pEvent.getY();
				double gridX0 = getBoardX( canvasX );
				double gridY0 = getBoardY( canvasY );
				int gridX = (int) Math.round( gridX0 );
				int gridY = (int) Math.round( gridY0 );
				if( gridX < 0 || gridX >= mSize ) {
					return;
				}
				if( gridY < 0 || gridY >= mSize ) {
					return;
				}
				for( ClickListener listener : mListeners ) {
					listener.onClick( gridX , gridY );
				}
			}
		} );
		// All composites must call initWidget() in their constructors.
		initWidget( mCanvas );
		// Give the overall composite a style name.
		setStyleName( "goban" );
		getElement().getStyle().setDisplay( Display.INLINE_BLOCK );
		getElement().getStyle().setBorderWidth( 0 , Unit.PX );
		getElement().getStyle().setPadding( 0 , Unit.PX );
		getElement().getStyle().setMargin( 0 , Unit.PX );
	}

	public boolean isZoomed() {
		return mZoomScale != 1;
	}

	private void updateCursor( int pX , int pY ) {
		if( !mCursorEnabled ) {
			return;
		}
		if( mCursorX == pX && mCursorY == pY ) {
			return;
		}
		mCursorX = pX;
		mCursorY = pY;
		draw();
	}
}
