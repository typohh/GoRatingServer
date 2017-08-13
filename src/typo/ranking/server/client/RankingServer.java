package typo.ranking.server.client;

import java.util.logging.Level;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import typo.ranking.server.shared.Move;
import typo.ranking.server.shared.Rating;
import typo.ranking.server.shared.Version;
import typo.ranking.server.shared.messages.GameListMessage;
import typo.ranking.server.shared.messages.LeaderBoardMessage;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class RankingServer implements EntryPoint {

	private ClientState mState = new ClientState() {

		@Override
		public void newVersionAvailable() {
			RootPanel.get( "dialogError" ).getElement().setInnerText( mMessages.newVersionAvailable() );
			RootPanel.get( "dialogErrorWrapper" ).getElement().getStyle().setDisplay( Display.BLOCK );
		}

		@Override
		public void update() {
			updateButtons();
			updateBoard();
			if( mState.isGameInProgress() ) {
				if( mState.isOver() ) {
					mInfoBlack.setData( mState.getName( true ) , mState.getRating( true ) , mState.getCaptures( true ) , mState.getLastMoveTime() , mState.getPeriods( true ) , mState.getTimePerPeriod() , false );
					mInfoWhite.setData( mState.getName( false ) , mState.getRating( false ) , mState.getCaptures( false ) , mState.getLastMoveTime() , mState.getPeriods( false ) , mState.getTimePerPeriod() , false );
				} else {
					mInfoBlack.setData( mState.getName( true ) , mState.getRating( true ) , mState.getCaptures( true ) , mState.getLastMoveTime() , mState.getPeriods( true ) , mState.getTimePerPeriod() , mState.isBlacksTurn() ^ (getLastMove() == Move.TimeLoss) );
					mInfoWhite.setData( mState.getName( false ) , mState.getRating( false ) , mState.getCaptures( false ) , mState.getLastMoveTime() , mState.getPeriods( false ) , mState.getTimePerPeriod() , !mState.isBlacksTurn() ^ (getLastMove() == Move.TimeLoss) );
				}
				if( isPlaying() ) {
					if( mMultipleByoyomis && mState.getNumberOfPeriods() == 1 ) {
						mAudio.playLastOvertime();
						mMultipleByoyomis = false;
					} else if( mState.getNumberOfPeriods() > 1 ) {
						mMultipleByoyomis = true;
					}
				}
			} else {
				mInfoBlack.setData( mMessages.black() , new Rating( 1500 , 173 * 3 ) , 0 , 0 , 3 , 10000 , false );
				mInfoWhite.setData( mMessages.white() , new Rating( 1500 , 173 * 3 ) , 0 , 0 , 3 , 10000 , false );
			}
		}

		@Override
		public void updateGameList() {
			mGamesList.resize( Math.max( 2 , getNumberOfActiveGames() * 2 + 1 ) , 4 );
			mGamesList.setStylePrimaryName( "tableActiveGame" );
			mGamesList.setText( 0 , 0 , mMessages.players() );
			mGamesList.setText( 0 , 1 , mMessages.rating() );
			mGamesList.setText( 0 , 2 , mMessages.moveNumber() );
			mGamesList.setText( 0 , 3 , mMessages.byoyomi() );
			mGamesList.getCellFormatter().setStylePrimaryName( 0 , 0 , "cellTitle" );
			mGamesList.getCellFormatter().setStylePrimaryName( 0 , 1 , "cellTitle" );
			mGamesList.getCellFormatter().setStylePrimaryName( 0 , 2 , "cellTitle" );
			mGamesList.getCellFormatter().setStylePrimaryName( 0 , 3 , "cellTitle" );
			if( getNumberOfActiveGames() == 0 ) {
				mGamesList.setText( 1 , 0 , mMessages.noGames() );
				mGamesList.setText( 1 , 1 , "" );
				mGamesList.setText( 1 , 2 , "" );
				mGamesList.setText( 1 , 3 , "" );
				mGamesList.getCellFormatter().setStylePrimaryName( 1 , 0 , "cellData cellDataFirst" );
				mGamesList.getCellFormatter().setStylePrimaryName( 1 , 1 , "cellData cellDataFirst" );
				mGamesList.getCellFormatter().setStylePrimaryName( 1 , 2 , "cellData cellDataFirst" );
				mGamesList.getCellFormatter().setStylePrimaryName( 1 , 3 , "cellData cellDataFirst" );
			} else {
				for( int i = 0 ; i < getNumberOfActiveGames() ; ++i ) {
					GameListMessage.GameStats gd = getActiveGame( i );
					mGamesList.setText( i * 2 + 1 , 0 , gd.getWhiteName() );
					mGamesList.setText( i * 2 + 1 , 1 , gd.getWhiteRating().toString() );
					mGamesList.setText( i * 2 + 1 , 2 , "" + gd.getNumberOfMoves() );
					mGamesList.setText( i * 2 + 1 , 3 , "" + (gd.getByomi() / 1000) );
					mGamesList.setText( i * 2 + 2 , 0 , gd.getBlackName() );
					mGamesList.setText( i * 2 + 2 , 1 , gd.getBlackRating().toString() );
					mGamesList.setText( i * 2 + 2 , 2 , "" );
					mGamesList.setText( i * 2 + 2 , 3 , "" );
					mGamesList.getCellFormatter().setStylePrimaryName( i * 2 + 1 , 0 , "cellData cellDataFirst" );
					mGamesList.getCellFormatter().setStylePrimaryName( i * 2 + 1 , 1 , "cellData cellDataFirst" );
					mGamesList.getCellFormatter().setStylePrimaryName( i * 2 + 1 , 2 , "cellData cellDataFirst" );
					mGamesList.getCellFormatter().setStylePrimaryName( i * 2 + 1 , 3 , "cellData cellDataFirst" );
					mGamesList.getCellFormatter().setStylePrimaryName( i * 2 + 2 , 0 , "cellData cellDataSecond" );
					mGamesList.getCellFormatter().setStylePrimaryName( i * 2 + 2 , 1 , "cellData cellDataSecond" );
					mGamesList.getCellFormatter().setStylePrimaryName( i * 2 + 2 , 2 , "cellData cellDataSecond" );
					mGamesList.getCellFormatter().setStylePrimaryName( i * 2 + 2 , 3 , "cellData cellDataSecond" );
				}
			}
		}

		@Override
		public void updateLeaderboard( LeaderBoardMessage pList ) {
			mLeaderBoardList.resize( pList.getNumberOfPlayers() , 4 );
			for( int i = 0 ; i < pList.getNumberOfPlayers() ; ++i ) {
				LeaderBoardMessage.LeaderBoardEntry e = pList.getPlayer( i );
				mLeaderBoardList.setText( i , 0 , e.getPosition() + "." );
				mLeaderBoardList.setText( i , 1 , e.getName() );
				mLeaderBoardList.setText( i , 2 , e.getRating().toString() );
				int gamesPerDayx10 = (int) (10 * e.getRecentGames() / 7);
				String number = gamesPerDayx10 / 10 + "." + gamesPerDayx10 % 10;
				mLeaderBoardList.setText( i , 3 , number );
				mLeaderBoardList.getCellFormatter().setStyleName( i , 0 , "cellLeaderBoard" );
				mLeaderBoardList.getCellFormatter().setStyleName( i , 1 , "cellLeaderBoard" );
				mLeaderBoardList.getCellFormatter().setStyleName( i , 2 , "cellLeaderBoard" );
				mLeaderBoardList.getCellFormatter().setStyleName( i , 3 , "cellLeaderBoardActivity cellLeaderBoard" );
				// mLeaderBoardList.setText( i , 4 , Integer.toString( e.getNumberOfTournamentGames() ) );
				// mLeaderBoardList.getCellFormatter().setStyleName( i , 4 , "cellLeaderBoardActivity" );
			}
		}

		@Override
		public void updateAction( Move pMove , boolean pByPlayer ) {
			if( pMove.isMove() ) {
				mAudio.playStone();
			} else if( !pByPlayer ) {
				if( pMove == Move.Pass ) {
					mAudio.playPass();
					if( isPlaying() ) {
						showInfoFading( mMessages.opponentPassed() );
					} else if( isBlacksTurn() ) {
						showInfoFading( mMessages.whitePassed() );
					} else {
						showInfoFading( mMessages.blackPassed() );
					}
				} else if( pMove == Move.Resign ) {
					mAudio.playResign();
				}
				mBoard.unZoom();
			}
			mNextCountdown = 3;
			mMessageTimer.schedule( 0 );
		}

		@Override
		public void reconnected() {
			showInfo( "" );
			updateButtons();
		}

		@Override
		public void gameLoaded() {
			showInfo( "" );
			if( isPlaying() && getNumberOfMoves() < 2 ) {
				mAudio.playReady();
			}
			update();
			mMessageTimer.schedule( 0 );
		}

		@Override
		public void disconnected() {
			showInfoBlinking( mMessages.reconnecting() );
		}

		@Override
		public void errorOccured() {
			RootPanel.get( "dialogErrorWrapper" ).getElement().getStyle().setDisplay( Display.BLOCK );
			RootPanel.get( "dialogError" ).getElement().setInnerText( mMessages.errorOccured() );
		}

		@Override
		public void initialized() {
			initializeFaq( getUserId() );
			showInfo( "" );
		}
	};
	// buttons and stuff..
	private CanvasBoard mBoard;
	private PlayerInfo mInfoWhite;
	private PlayerInfo mInfoBlack;
	private Button mActionResign;
	private Button mActionPass;
	private Button mActionZoomOut;
	private Button mActionClose;
	private Button mActionShare;
	private Button mShareDownload;
	private Button mShareJGoBoard;
	private Button mShareEidoGo;
	private Button mShareClose;
	private Grid mLeaderBoardList;
	private Grid mGamesList;
	private Button mLeaderBoardClose;
	private Label mGreetingText;
	private Button mGameListClose;
	private Button mGreetingReconnect;
	private Button mGreetingRandomize;
	private Button mGreetingPlayBlitz;
	private Button mGreetingPlayFast;
	private Button mGreetingWatch;
	private Button mGreetingLeaderBoard;
	private Button mGreetingFaq;
	private Button mFaqClose;
	private HTML mGreetingText2;
	private Button mVolumeUp;
	private Button mVolumeDown;
	private LanguageMessages mMessages = GWT.create( LanguageMessages.class );
	private AudioHandler mAudio;
	private boolean mIsTouch;

	@Override
	public void onModuleLoad() {
		if( Window.Location.getHostName().equals( "goratingserver.appspot.com" ) ) {
			long userId = mState.getUserId();
			if( userId != 0 ) {
				Window.Location.replace( "http://goratingserver.com/?p=" + userId );
			} else {
				Window.Location.replace( "http://goratingserver.com" );
			}
			return;
		}
		if( Window.Location.getQueryString().startsWith( "?p=" ) ) {
			String userid = Window.Location.getQueryString().substring( 3 );
			Util.put( "uid" , userid );
			String href = Window.Location.getHref();
			String query = Window.Location.getQueryString();
			Window.Location.replace( href.substring( 0 , href.length() - query.length() ) );
			return;
		}
		GWT.setUncaughtExceptionHandler( new GWT.UncaughtExceptionHandler() {

			@Override
			public void onUncaughtException( Throwable pE ) {
				mState.reset();
				Util.log( Level.SEVERE , pE );
				RootPanel.get( "dialogErrorWrapper" ).getElement().getStyle().setDisplay( Display.BLOCK );
				RootPanel.get( "dialogError" ).getElement().setInnerText( mMessages.errorOccured() );
			}
		} );
		showInfoBlinking( mMessages.loading() );
		mGreetingText = Label.wrap( RootPanel.get( "greetingText" ).getElement() );
//		mGreetingText.setText( mMessages.loading() );
//		mGreetingText.setStyleName( "loader" , true );
		mBoard = new CanvasBoard();
		mInfoWhite = new PlayerInfo( false );
		mInfoBlack = new PlayerInfo( true );
		RootPanel.get( "dialogInfo" ); // need to grab this before the board..
		RootPanel.get( "board" ).add( mBoard );
		mIsTouch = isTouch();
		mBoard.setCursor( !mIsTouch );
		mBoard.addListener( new CanvasBoard.ClickListener() {

			@Override
			public void onClick( double pX , double pY ) {
				if( mState.isPlayersTurn() && !mState.isOver() ) {
					if( !mBoard.isZoomed() && mIsTouch ) {
						mBoard.zoomAt( pX , pY , 2 );
						updateButtons();
					} else {
						if( mState.playMove( Move.getMove( (int) Math.round( pX ) , (int) Math.round( pY ) , 19 ) ) ) {
							mBoard.unZoom();
						}
					}
				}
			}
		} );
		// mActionText = Label.wrap( RootPanel.get( "actionText" ).getElement() );
		mActionPass = Button.wrap( RootPanel.get( "actionPass" ).getElement() );
		mActionPass.setText( mMessages.pass() );
		mActionResign = Button.wrap( RootPanel.get( "actionResign" ).getElement() );
		mActionResign.setText( mMessages.resign() );
		mActionZoomOut = Button.wrap( RootPanel.get( "actionZoomOut" ).getElement() );
		mActionZoomOut.setText( mMessages.zoomOut() );
		// mActionAccept = Button.wrap( RootPanel.get( "actionAccept" ).getElement() );
		// mActionAccept.setText( mMessages.yes() );
		// mActionReject = Button.wrap( RootPanel.get( "actionReject" ).getElement() );
		// mActionReject.setText( mMessages.no() );
		mActionClose = Button.wrap( RootPanel.get( "actionOk" ).getElement() );
		mActionClose.setText( mMessages.close() );
		mActionShare = Button.wrap( RootPanel.get( "actionShare" ).getElement() );
		mActionShare.setText( mMessages.share() );
		mShareJGoBoard = Button.wrap( RootPanel.get( "shareJGoBoard" ).getElement() );
		mShareJGoBoard.setText( mMessages.viewOnJGoBoard() );
		mShareEidoGo = Button.wrap( RootPanel.get( "shareEidoGo" ).getElement() );
		mShareEidoGo.setText( mMessages.viewOnEidoGo() );
		mShareDownload = Button.wrap( RootPanel.get( "shareDownload" ).getElement() );
		mShareDownload.setText( mMessages.downloadSgf() );
		mShareClose = Button.wrap( RootPanel.get( "shareClose" ).getElement() );
		mLeaderBoardList = new Grid();
		RootPanel.get( "leaderBoardList" ).add( mLeaderBoardList );
		mGamesList = new Grid();
		RootPanel.get( "gameListList" ).add( mGamesList );
		mLeaderBoardClose = Button.wrap( RootPanel.get( "leaderBoardClose" ).getElement() );
		mGreetingRandomize = Button.wrap( RootPanel.get( "greetingRandomize" ).getElement() );
		mGreetingRandomize.setText( mMessages.rerollname() );
		mGreetingReconnect = Button.wrap( RootPanel.get( "greetingReconnect" ).getElement() );
		mGreetingReconnect.setText( mMessages.reconnect() );
		mGreetingPlayBlitz = Button.wrap( RootPanel.get( "greetingPlayBlitz" ).getElement() );
		mGreetingPlayFast = Button.wrap( RootPanel.get( "greetingPlayFast" ).getElement() );
		mGreetingWatch = Button.wrap( RootPanel.get( "greetingWatch" ).getElement() );
		mGreetingWatch.setText( mMessages.watch() );
		mGreetingLeaderBoard = Button.wrap( RootPanel.get( "greetingLeaderBoard" ).getElement() );
		mGreetingLeaderBoard.setText( mMessages.showLeaderBoard() );
		mGamesList.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				Cell cell = mGamesList.getCellForEvent( pEvent );
				if( cell != null ) {
					int rowIndex = (cell.getRowIndex() - 1) / 2;
					if( rowIndex >= 0 && rowIndex < mState.getNumberOfActiveGames() ) {
						mState.loadGame( rowIndex );
						RootPanel.get( "gameListDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
					}
				}
			}
		} );
		mGreetingFaq = Button.wrap( RootPanel.get( "greetingFAQ" ).getElement() );
		mGreetingFaq.setText( mMessages.FAQ() );
		mGreetingText2 = HTML.wrap( RootPanel.get( "greetingText2" ).getElement() );
		mFaqClose = Button.wrap( RootPanel.get( "faqClose" ).getElement() );
		mGameListClose = Button.wrap( RootPanel.get( "gameListClose" ).getElement() );
		mVolumeUp = Button.wrap( RootPanel.get( "volumeUp" ).getElement() );
		mVolumeDown = Button.wrap( RootPanel.get( "volumeDown" ).getElement() );
		RootPanel.get( "faqHeading" ).getElement().setInnerText( mMessages.FAQ() );
		RootPanel.get( "gameListHeading" ).getElement().setInnerText( mMessages.gamesList() );
		RootPanel.get( "leaderBoardHeading" ).getElement().setInnerText( mMessages.leaderBoard() );
		// mActionAccept.addClickHandler( new ClickHandler() {
		//
		// @Override
		// public void onClick( ClickEvent pEvent ) {
		// mState.playMove( Move.Accept );
		// }
		// } );
		// mActionReject.addClickHandler( new ClickHandler() {
		//
		// @Override
		// public void onClick( ClickEvent pEvent ) {
		// mState.playMove( Move.Reject );
		// }
		// } );
		mActionPass.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mState.playMove( Move.Pass );
			}
		} );
		mActionResign.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mState.playMove( Move.Resign );
			}
		} );
		mActionClose.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				showInfo( "" );
				mState.reset();
			}
		} );
		mActionShare.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				RootPanel.get( "shareDialogWrapper" ).getElement().getStyle().setDisplay( Display.BLOCK );
			}
		} );
		mShareJGoBoard.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				Window.open( "http://static.jgoboard.com/jgoboard/demoSGF.html?hidemenu=1&url=" + getSgfUrl() , "_blank" , "" );
			}
		} );
		mShareEidoGo.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				Window.open( "http://eidogo.com/#url:" + getSgfUrl() , "_blank" , "" );
			}
		} );
		mShareDownload.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				Window.open( getSgfUrl() , "_blank" , "" );
			}
		} );
		mShareClose.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				RootPanel.get( "shareDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
			}
		} );
		mLeaderBoardClose.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				RootPanel.get( "leaderBoardDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
			}
		} );
		mActionZoomOut.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mBoard.unZoom();
			}
		} );
		mGreetingRandomize.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mState.randomizeName();
			}
		} );
		mGreetingReconnect.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				// Util.log( Level.SEVERE , "reconnect clicked." );
				mState.initialize();
			}
		} );
		mGreetingPlayBlitz.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mAudio.warmup();
				mState.setSearching( !mState.isSearchingBlitz() , mState.isSearchingFast() );
				updateButtons();
			}
		} );
		mGreetingPlayFast.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mAudio.warmup();
				mState.setSearching( mState.isSearchingBlitz() , !mState.isSearchingFast() );
				updateButtons();
			}
		} );
		mGreetingWatch.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mGamesList.resize( 1 , 1 );
				mGamesList.setText( 0 , 0 , mMessages.loading() );
				mGamesList.getCellFormatter().setStyleName( 0 , 0 , "loader" );
				RootPanel.get( "gameListDialogWrapper" ).getElement().getStyle().setDisplay( Display.BLOCK );
				mState.fetchActiveGames();
				mAudio.warmup();
			}
		} );
		mGameListClose.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				RootPanel.get( "gameListDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
			}
		} );
		mGreetingLeaderBoard.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mLeaderBoardList.resize( 1 , 1 );
				mLeaderBoardList.setText( 0 , 0 , mMessages.loading() );
				mLeaderBoardList.getCellFormatter().setStyleName( 0 , 0 , "loader" );
				RootPanel.get( "leaderBoardDialogWrapper" ).getElement().getStyle().setDisplay( Display.BLOCK );
				mState.fetchLeaderboard();
			}
		} );
		mGreetingFaq.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				RootPanel.get( "faqDialogWrapper" ).getElement().getStyle().setDisplay( Display.BLOCK );
			}
		} );
		mFaqClose.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				RootPanel.get( "faqDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
			}
		} );
		mVolumeUp.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mAudio.warmup();
				mAudio.changeVolume( true );
				showInfoFading( (int) (mAudio.getVolume() * 100) + "%" );
			}
		} );
		mVolumeDown.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				mAudio.warmup();
				mAudio.changeVolume( false );
				showInfoFading( (int) (mAudio.getVolume() * 100) + "%" );
			}
		} );
		mAudio = new AudioHandler( mMessages );
		mState.initialize();
		resize( Window.getClientWidth() , Window.getClientHeight() );
		updateBoard();
		mBoard.draw();
		Window.addResizeHandler( new ResizeHandler() {

			@Override
			public void onResize( ResizeEvent ev ) {
				resize( ev.getWidth() , ev.getHeight() );
			}
		} );
		Window.setTitle( mMessages.servername() + " " + Version.sVersionHuman );
		// RootPanel.get( "faqDialogWrapper" ).addDomHandler( new ClickHandler() {
		//
		// @Override
		// public void onClick( ClickEvent pEvent ) {
		// RootPanel.get( "faqDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
		// }
		//
		// } , ClickEvent.getType() );
		RootPanel.get( "gameListDialogWrapper" ).addDomHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				RootPanel.get( "gameListDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
			}
		} , ClickEvent.getType() );
		RootPanel.get( "leaderBoardDialogWrapper" ).addDomHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent pEvent ) {
				RootPanel.get( "leaderBoardDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
			}
		} , ClickEvent.getType() );
		RootPanel.get().addHandler( new KeyDownHandler() {

			@Override
			public void onKeyDown( KeyDownEvent pEvent ) {
				if( pEvent.getNativeKeyCode() == KeyCodes.KEY_ESCAPE ) {
					RootPanel.get( "faqDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
					RootPanel.get( "gameListDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
					RootPanel.get( "leaderBoardDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
				}
			}
		} , KeyDownEvent.getType() );
		if( Navigator.getUserAgent().toLowerCase().contains( "ipad" ) ) {
			RootPanel.getBodyElement().getStyle().setFontSize( 1.5 , Unit.EM );
		}
	}

	public String initializePayPal( long pUserId ) {
		return "<form action=\"https://www.paypal.com/cgi-bin/webscr\" method=\"post\">" + "<input type=\"hidden\" name=\"business\" value=\"typo.hh@gmail.com\">" + "<input type=\"hidden\" name=\"cmd\" value=\"_xclick\">" + "<input type=\"hidden\" name=\"item_name\" value=\"Custom name\">" + "<input type=\"hidden\" name=\"amount\" value=\"50.00\">" + "<input type=\"hidden\" name=\"currency_code\" value=\"USD\">" + "<input type=\"hidden\" name=\"custom\" value=\"" + pUserId + "\">" + "<input type=\"hidden\" name=\"no_shipping\" value=\"1\">" + "<input type=\"hidden\" name=\"no_note\" value=\"1\">" + "<input type=\"hidden\" name=\"on0\" value=\"name\">" + mMessages.newName() + "<input type=\"text\" name=\"os0\" maxlength=\"60\"> <br />" + "<input type=\"image\" name=\"submit\" border=\"0\" style=\"padding-top:0.5em;\" src=\"https://www.paypalobjects.com/webstatic/en_US/btn/btn_buynow_cc_171x47.png\" alt=\"PayPal - The safer, easier way to pay online\">" + "<img alt=\"\" border=\"0\" width=\"1\" height=\"1\" src=\"https://www.paypalobjects.com/en_US/i/scr/pixel.gif\" >" + "</form>";
	}

	public void initializeFaq( long pUserId ) {
		StringBuffer faq = new StringBuffer();
		String[] questions = new String[] {
				mMessages.faqQuestionWhatRuleSet(), mMessages.faqQuestionHowDoesTheScoringWork(), mMessages.faqQuestionHowDoesThePairingWork(), mMessages.faqQuestionCanIWatchGames(), mMessages.faqQuestionWhatRatingSystem(), mMessages.faqQuestionCanMyBotPlayHere(), mMessages.faqQuestionCanIChooseMyNickName(), mMessages.faqQuestionAcknowledgments(),
		};
		String[] answers = new String[] {
				mMessages.faqAnswerWhatRuleSet(), mMessages.faqAnswerHowDoesTheScoringWork(), mMessages.faqAnswerHowDoesThePairingWork(), mMessages.faqAnswerCanIWatchGames(), mMessages.faqAnswerWhatRatingSystem(), mMessages.faqAnswerCanMyBotPlayHere(), mMessages.faqAnswerCanIChooseMyNickName() + initializePayPal( pUserId ), mMessages.faqAnswerAcknowledgments(),
		};
		for( int i = 0 ; i < questions.length ; ++i ) {
			faq.append( "<div class=\"question\">" + questions[i] + "</div>" );
			faq.append( "<div class=\"answer\">" + answers[i] + "</div>" );
		}
		RootPanel.get( "faqList" ).getElement().setInnerHTML( faq.toString() );
	}

	public void showInfoBlinking( String pText ) {
		RootPanel panel = RootPanel.get( "dialogInfo" );
		panel.getElement().getStyle().setOpacity( 1 );
		panel.setStyleName( "temporary" , false );
		panel.setStyleName( "loader" , true );
		panel.getElement().setInnerText( pText );
	}

	Timer mFadingTimer=new Timer() {

		@Override
		public void run() {
			RootPanel panel = RootPanel.get( "dialogInfo" );
			panel.setStyleName( "temporary" , true );
		}
		
	};
	
	public void showInfoFading( String pText ) {
		RootPanel panel = RootPanel.get( "dialogInfo" );
		panel.getElement().getStyle().setOpacity( 0 );
		panel.setStyleName( "temporary" , false );
		panel.setStyleName( "loader" , false );
		panel.getElement().setInnerText( pText );
		mFadingTimer.schedule( 100 );
	}

	public void showInfo( String pText ) {
		RootPanel panel = RootPanel.get( "dialogInfo" );
		panel.setStyleName( "temporary" , false );
		panel.setStyleName( "loader" , false );
		if( pText.isEmpty() ) {
			panel.getElement().getStyle().setOpacity( 0 );
		} else {
			panel.getElement().getStyle().setOpacity( 1 );
		}
		panel.getElement().setInnerText( pText );
	}

	private String getSgfUrl() {
		return GWT.getHostPageBaseURL() + "sgf/" + mState.getGameId() + ".sgf";
	}

	public void resize( int pWidth , int pHeight ) {
		mBoard.setViewSize( Math.min( pWidth , pHeight ) );
		mBoard.draw();
		Style style = RootPanel.get( "infoGame" ).getElement().getStyle();
		double width = pWidth;
		if( pWidth > pHeight ) {
			style.setLeft( pHeight , Unit.PX );
			style.setTop( 0 , Unit.PX );
			style.setWidth( pWidth - pHeight , Unit.PX );
			style.setHeight( pHeight , Unit.PX );
			// style.setProperty( "minHeight" , pHeight , Unit.PX );
			width -= pHeight;
		} else {
			style.setLeft( 0 , Unit.PX );
			style.setWidth( pWidth , Unit.PX );
			style.setTop( pWidth , Unit.PX );
			style.setHeight( pHeight - pWidth , Unit.PX );
			// style.setProperty( "minHeight" , pHeight - pWidth , Unit.PX );
		}
		double fontSize = Math.max( 10 , width / 25 );
		if( pWidth > pHeight && fontSize > 23 ) {
			fontSize = 23;
		}
		mInfoBlack.getStyle().setFontSize( fontSize , Unit.PX );
		mInfoWhite.getStyle().setFontSize( fontSize , Unit.PX );
		Window.scrollTo( 0 , 0 );
	}

	private void updateBoard() {
		if( mState.getLastMove() != null && mState.getLastMove().isMove() ) {
			mBoard.setLastMove( mState.getLastMove().getX() , mState.getLastMove().getY() );
		} else {
			mBoard.setLastMove( -1 , -1 );
		}
		for( Move move : Move.values() ) { // update visuals..
			if( move.isMove() ) {
				mBoard.setStone( move.getX() , move.getY() , mState.getStone( move ) );
				mBoard.setTerritory( move.getX() , move.getY() , mState.getTerritory( move ) );
			}
		}
		if( mState.isGameInProgress() && mState.isPlaying() && mState.isPlayersTurn() && !mIsTouch && !mState.isOver() ) {
			mBoard.setCursor( true );
			mBoard.setTurn( mState.isBlacksTurn() == mState.isPlayersTurn() );
		} else {
			mBoard.setCursor( false );
		}
		mBoard.draw();
	}

	private void updateButtons() {
		if( !mState.isConnected() ) {
			mInfoBlack.getStyle().setDisplay( Display.NONE );
			mInfoWhite.getStyle().setDisplay( Display.NONE );
			mGreetingText.getElement().getStyle().setDisplay( Display.INLINE_BLOCK );
			mGreetingText.setText( mMessages.youHaveLoggedInElsewhere() );
			mGreetingReconnect.getElement().getStyle().setDisplay( Display.INLINE );
			mActionZoomOut.getElement().getStyle().setDisplay( Display.NONE );
			mActionPass.getElement().getStyle().setDisplay( Display.NONE );
			mActionResign.getElement().getStyle().setDisplay( Display.NONE );
			mActionClose.getElement().getStyle().setDisplay( Display.NONE );
			mActionShare.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingRandomize.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingPlayBlitz.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingPlayFast.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingWatch.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingLeaderBoard.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingFaq.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingText2.getElement().getStyle().setDisplay( Display.NONE );
			return;
		} else {
			mGreetingReconnect.getElement().getStyle().setDisplay( Display.NONE );
		}
		mGreetingText.setStyleName( "loader" , false );
		if( mState.isGameInProgress() ) {
			mInfoBlack.getStyle().setDisplay( Display.INLINE_BLOCK );
			mInfoWhite.getStyle().setDisplay( Display.INLINE_BLOCK );
		} else {
			mInfoBlack.getStyle().setDisplay( Display.NONE );
			mInfoWhite.getStyle().setDisplay( Display.NONE );
		}
		if( !mState.isGameInProgress() || !mState.isPlaying() ) {
			mGreetingText.getElement().getStyle().setDisplay( Display.INLINE_BLOCK );
			if( mState.getNumberOfGames() != 0 ) {
				mGreetingText.setText( mMessages.hello( mState.getUserName() , mState.getUserRating().getMean() , mState.getUserRating().getSD() , mState.getUserNumberOfGames() ) );
				mGreetingRandomize.getElement().getStyle().setDisplay( Display.NONE );
			} else {
				mGreetingText.setText( mMessages.welcome( mState.getUserName() ) );
				mGreetingRandomize.getElement().getStyle().setDisplay( Display.INLINE );
			}
			if( mState.isSearchingBlitz() ) {
				mGreetingPlayBlitz.setText( mMessages.cancel( 10 , 3 ) );
			} else {
				mGreetingPlayBlitz.setText( mMessages.play( 10 , 3 ) );
			}
			mGreetingPlayBlitz.getElement().getStyle().setDisplay( Display.INLINE );
			if( mState.isSearchingFast() ) {
				mGreetingPlayFast.setText( mMessages.cancel( 30 , 20 ) );
			} else {
				mGreetingPlayFast.setText( mMessages.play( 30 , 20 ) );
			}
			mGreetingPlayFast.getElement().getStyle().setDisplay( Display.INLINE );
			mGreetingWatch.getElement().getStyle().setDisplay( Display.INLINE );
			mGreetingLeaderBoard.getElement().getStyle().setDisplay( Display.INLINE );
			mGreetingFaq.getElement().getStyle().setDisplay( Display.INLINE );
			if( mState.isSearchingBlitz() || mState.isSearchingFast() ) {
				mGreetingText2.getElement().getStyle().setDisplay( Display.INLINE_BLOCK );
				mGreetingText2.setText( mMessages.searchingforopponent() );
				mGreetingText2.setStyleName( "loader" , true );
			} else if( mState.getNumberOfGames() != 0 ) {
				String link = Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/?p=" + mState.getUserId();
				mGreetingText2.getElement().getStyle().setDisplay( Display.INLINE_BLOCK );
				mGreetingText2.setHTML( mMessages.thissecretlinkPre() + "<A href=\"" + link + "\">" + mMessages.thissecretlink() + "</A>" + mMessages.thissecretlinkPost() );
				mGreetingText2.setStyleName( "loader" , false );
			} else {
				mGreetingText2.getElement().getStyle().setDisplay( Display.NONE );
				mGreetingText2.setStyleName( "loader" , false );
			}
		} else {
			mGreetingText.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingRandomize.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingPlayBlitz.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingPlayFast.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingWatch.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingLeaderBoard.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingFaq.getElement().getStyle().setDisplay( Display.NONE );
			mGreetingText2.getElement().getStyle().setDisplay( Display.NONE );
		}
		if( !mState.isGameInProgress() ) {
			// mActionText.getElement().getStyle().setDisplay( Display.NONE );
			// mActionAccept.getElement().getStyle().setDisplay( Display.NONE );
			// mActionReject.getElement().getStyle().setDisplay( Display.NONE );
			mActionZoomOut.getElement().getStyle().setDisplay( Display.NONE );
			mActionPass.getElement().getStyle().setDisplay( Display.NONE );
			mActionResign.getElement().getStyle().setDisplay( Display.NONE );
			mActionClose.getElement().getStyle().setDisplay( Display.NONE );
			mActionShare.getElement().getStyle().setDisplay( Display.NONE );
			return;
		}
		if( !mState.isPlaying() ) {
			 if( mState.isOver() ) {
				 showInfo( getResult() );
			 }
			mActionZoomOut.getElement().getStyle().setDisplay( Display.NONE );
			mActionPass.getElement().getStyle().setDisplay( Display.NONE );
			mActionResign.getElement().getStyle().setDisplay( Display.NONE );
			mActionClose.getElement().getStyle().setDisplay( Display.INLINE_BLOCK );
			mActionShare.getElement().getStyle().setDisplay( Display.INLINE_BLOCK );
			return;
		}
		RootPanel.get( "leaderBoardDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
		RootPanel.get( "gameListDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
		RootPanel.get( "faqDialogWrapper" ).getElement().getStyle().setDisplay( Display.NONE );
		if( mState.isOver() ) {
			showInfo( getResult() );
			mActionZoomOut.getElement().getStyle().setDisplay( Display.NONE );
			mActionPass.getElement().getStyle().setDisplay( Display.NONE );
			mActionResign.getElement().getStyle().setDisplay( Display.NONE );
			mActionClose.getElement().getStyle().setDisplay( Display.INLINE );
			mActionShare.getElement().getStyle().setDisplay( Display.INLINE );
			return;
		}
		if( mIsTouch && mBoard.isZoomed() ) {
			mActionZoomOut.getElement().getStyle().setDisplay( Display.INLINE );
		} else {
			mActionZoomOut.getElement().getStyle().setDisplay( Display.NONE );
		}
		if( mState.isPlayersTurn() ) {
			mActionPass.getElement().getStyle().setDisplay( Display.INLINE );
			mActionPass.getElement().getStyle().setVisibility( Visibility.VISIBLE );
			mActionResign.getElement().getStyle().setDisplay( Display.INLINE );
		} else {
			mActionPass.getElement().getStyle().setDisplay( Display.INLINE );
			mActionPass.getElement().getStyle().setVisibility( Visibility.HIDDEN ); // to ensure it doesnt jump around during the game..
			mActionResign.getElement().getStyle().setDisplay( Display.NONE );
		}
		mActionClose.getElement().getStyle().setDisplay( Display.NONE );
		mActionShare.getElement().getStyle().setDisplay( Display.NONE );
	}

	private boolean mMultipleByoyomis = true;
	private int mNextCountdown = 3;
	private Timer mMessageTimer = new Timer() {

		@Override
		public void run() {
			if( mState.isGameInProgress() ) {
				long remaining;
				long remainingByomi;
				int numberOfByomis = 0;
				mInfoBlack.updateTime();
				mInfoWhite.updateTime();
				if( mState.isBlacksTurn() ) {
					remaining = mInfoBlack.getTimeRemaining();
					remainingByomi = mInfoBlack.getPeriodTimeRemaining();
					numberOfByomis = mInfoBlack.getNumberOfPerdiods();
				} else {
					remaining = mInfoWhite.getTimeRemaining();
					remainingByomi = mInfoWhite.getPeriodTimeRemaining();
					numberOfByomis = mInfoWhite.getNumberOfPerdiods();
				}
				if( !mState.isOver() ) { // stop update if game is over..
					if( mState.isPlayersTurn() ) {
						if( mMultipleByoyomis && numberOfByomis == 1 ) {
							mAudio.playLastOvertime();
							mMultipleByoyomis = false;
						}
						if( remaining <= 0 ) {
							if( mState.isPlaying() ) {
								mState.playMove( Move.TimeLoss );
							}
						} else {
							if( remainingByomi < 2200 && mNextCountdown > 0 ) {
								mAudio.playOne();
								mNextCountdown = 0;
							}
							if( remainingByomi < 3200 && mNextCountdown > 1 ) {
								mAudio.playTwo();
								mNextCountdown = 1;
							}
							if( remainingByomi < 4200 && mNextCountdown > 2 ) {
								mAudio.playThree();
								mNextCountdown = 2;
							}
							if( remainingByomi > 4500 ) {
								mNextCountdown = 3;
							}
						}
					}
					if( remaining >= 0 ) {
						mMessageTimer.schedule( 25 + (int) ((remaining) % 1000) ); // schedule for 0.025 seconds after next second starts..
					}
				}
			}
		}
	};

	public String getResult() {
		if( mState.getLastMove() == Move.TimeLoss ) {
			if( mState.isPlaying() ) {
				if( !mState.isPlayersTurn() ) {
					return mMessages.youLostByTime();
				} else {
					return mMessages.youWonByTime();
				}
			} else {
				if( !mState.isBlacksTurn() ) {
					return mMessages.blackLostByTime();
				} else {
					return mMessages.whiteLostByTime();
				}
			}
		} else if( mState.getLastMove() == Move.Resign ) {
			if( mState.isPlaying() ) {
				if( !mState.isPlayersTurn() ) {
					return mMessages.youResigned();
				} else {
					return mMessages.youWonByResignation();
				}
			} else {
				if( !mState.isBlacksTurn() ) {
					return mMessages.blackResigned();
				} else {
					return mMessages.whiteResigned();
				}
			}
		} else {
			if( mState.isDeadListAvailable() ) {
				double score = mState.getScore();
				if( mState.isPlaying() ) {
					if( (score > 0) != (mState.isPlayersTurn() == mState.isBlacksTurn()) ) {
						return mMessages.youLostByPoints( (int) Math.abs( score ) );
					} else {
						return mMessages.youWonByPoints( (int) Math.abs( score ) );
					}
				} else {
					if( score > 0 ) {
						return mMessages.blackWonByPoints( (int) Math.abs( score ) );
					} else {
						return mMessages.whiteWonByPoints( (int) Math.abs( score ) );
					}
				}
			} else {
				return mMessages.determiningDeadStones();
			}
		}
	}

	public boolean isTouch() {
		String useragent = Navigator.getUserAgent().toLowerCase();
		if( useragent.contains( "android" ) ) {
			return true;
		}
		// if( useragent.contains( "ipad" ) ) {
		// return true;
		// }
		if( useragent.contains( "iphone" ) ) {
			return true;
		}
		if( useragent.contains( "ipod" ) ) {
			return true;
		}
		if( useragent.contains( "windows phone" ) ) {
			return true;
		}
		if( useragent.contains( "blackberry" ) ) {
			return true;
		}
		if( useragent.contains( "iemobile" ) ) {
			return true;
		}
		if( useragent.contains( "opera mini" ) ) {
			return true;
		}
		if( useragent.contains( "nokia" ) ) {
			return true;
		}
		return false;
	}
}
