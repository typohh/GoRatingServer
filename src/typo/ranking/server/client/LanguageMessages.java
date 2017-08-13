package typo.ranking.server.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface LanguageMessages extends Messages {
	
	@DefaultMessage("Ok")
	String ok();
	
	@DefaultMessage("Pass")
	String pass();
	
	@DefaultMessage("Resign")
	String resign();
	
	@DefaultMessage("Re-roll name")
	String rerollname();
	
	@DefaultMessage("Play {1}x{0}sec")
	String play( int pSeconds , int pByomis );

	@DefaultMessage("Cancel {1}x{0}sec")
	String cancel( int pSeconds , int pByomis );
	
	@DefaultMessage("No")
	String no();
	
	@DefaultMessage("Yes")
	String yes();
	
	@DefaultMessage("Ranking Go Server")
	String servername();
	
	@DefaultMessage("With " )
	String thissecretlinkPre();
	
	@DefaultMessage("this secret link" )
	String thissecretlink();

	@DefaultMessage(" you can login to this account from anywhere.")
	String thissecretlinkPost();
	
	@DefaultMessage("Welcome {0}!")
	String welcome( String pName );
	
	@DefaultMessage("Hello {0}, your rating is {1}±{2} after {3,number} games.")
	@AlternateMessage({"one", "Hello {0}, your rating is {1}±{2} after {3} game."})
	String hello( String pName , int pMean , int pSd ,@PluralCount int pGames );
	
	@DefaultMessage("Searching for opponent.")
	String searchingforopponent();
	
	@DefaultMessage("Opponent passed.")
	String opponentPassed();

	@DefaultMessage("Black passed.")
	String blackPassed();

	@DefaultMessage("White passed.")
	String whitePassed();

	@DefaultMessage("Opponent agreed.")
	String opponentAgreed();

	@DefaultMessage("When game is scored, all stones on the board will be considered alive.")
	String captureAllDeadStones();

	@DefaultMessage("Is this scored correctly?" )
	String isThisScoredCorrectly();
	
	@DefaultMessage("You lost by time.")
	String youLostByTime();

	@DefaultMessage("You won by time.")
	String youWonByTime();

	@DefaultMessage("Black lost by time.")
	String blackLostByTime();

	@DefaultMessage("White lost by time.")
	String whiteLostByTime();

	@DefaultMessage("You resigned.")
	String youResigned();

	@DefaultMessage("You won by resignation.")
	String youWonByResignation();

	@DefaultMessage("Black resigned.")
	String blackResigned();
	
	@DefaultMessage("White resigned.")
	String whiteResigned();
	
	@DefaultMessage( "You lost by {0}.5 points.")
	String youLostByPoints( int pPoints );
	
	@DefaultMessage( "You won by {0}.5 points.")
	String youWonByPoints( int pPoints );

	@DefaultMessage( "Black won by {0}.5 points.")
	String blackWonByPoints( int pPoints );

	@DefaultMessage( "White won by {0}.5 points.")
	String whiteWonByPoints( int pPoints );

	@DefaultMessage( "Waiting for opponent." )
	String waitingForOpponet();

	@DefaultMessage( "Export" )
	String share();

	@DefaultMessage( "Close" )
	String close();

	@DefaultMessage( "Download SGF" )
	String downloadSgf();

	@DefaultMessage( "View in JGoBoard" )
	String viewOnJGoBoard();

	@DefaultMessage( "View in EidoGo" )
	String viewOnEidoGo();

	@DefaultMessage( "Leaderboard" )
	String showLeaderBoard();

	@DefaultMessage( "Games per day" )
	String gamesPerDay();
	
	@DefaultMessage( "loading..." )
	String loading();

	@DefaultMessage( "finding dead stones..." )
	String removingDeadStones();

	@DefaultMessage( "Zoom out" )
	String zoomOut();

	@DefaultMessage( "FAQ" )
	String FAQ();
	
	@DefaultMessage( "Reconnecting..." )
	String reconnecting();

	@DefaultMessage( "audio.mp3" )
	String audioFile();

	@DefaultMessage( "320" )
	String audioStone();
		
	@DefaultMessage( "744" )
	String audioOne();
		
	@DefaultMessage( "816" )
	String audioTwo();
		
	@DefaultMessage( "792" )
	String audioThree();
		
	@DefaultMessage( "864" )
	String audioReady();
		
	@DefaultMessage( "936" )
	String audioPass();

	@DefaultMessage( "1008" )
	String audioResign();

	@DefaultMessage( "1560" )
	String audioLastOvertime();
	
	@DefaultMessage( "What rule-set and komi is used for the games?" )
	String faqQuestionWhatRuleSet();

	@DefaultMessage( "Chinese rules and komi of 7.5." )
	String faqAnswerWhatRuleSet();

	@DefaultMessage( "What rating system is used?" )
	String faqQuestionWhatRatingSystem();

	@DefaultMessage( "As of yet unpublished Heh rating system by Henry Hemming, which provides better accuracy than Glicko and TrueSkill(™) on numerous large data sets." )
	String faqAnswerWhatRatingSystem();

	@DefaultMessage( "Can I choose my own nickname instead of selecting a random one?" )
	String faqQuestionCanIChooseMyNickName();

	@DefaultMessage( "For the meager fee of $50 you can 'freely' choose your own nickname and it will also help support the server." )
	String faqAnswerCanIChooseMyNickName();

	@DefaultMessage( "How does the pairing work?" )
	String faqQuestionHowDoesThePairingWork();

	@DefaultMessage( "Good pairings are made faster than bad pairings (giving time to find better pairings)." )
	String faqAnswerHowDoesThePairingWork();

	@DefaultMessage( "Acknowledgments" )
	String faqQuestionAcknowledgments();

	@DefaultMessage( "Layout (CSS) was designed by Malviina Hallamaa. Board graphics have been ripped from JGoBoard." )
	String faqAnswerAcknowledgments();

	@DefaultMessage( "How does the scoring work?" )
	String faqQuestionHowDoesTheScoringWork();

	@DefaultMessage( "After both players pass, an estimate on which groups are dead is shown, if both players agree the game is scored accordingly. If either player disagrees the game continues, and both players are supposed to remove from the board all dead stones. Since its chinese rules no points are lost in the process. Next time both players pass the game is scored as if all stones on the board are alive." )
	String faqAnswerHowDoesTheScoringWork();
	
	@DefaultMessage( "Can my bot play here?" )
	String faqQuestionCanMyBotPlayHere();

	@DefaultMessage( "Sure, there is a jar file that runs your bot and talks to the server. Documentation, source code and some examples can be found at <a href=\"https://github.com/typohh/GTPRest\">https://github.com/typohh/GTPRest</a>. There are already several bots playing here." )
	String faqAnswerCanMyBotPlayHere();

	@DefaultMessage( "Can I find my old games?" )
	String faqQuestionCanIWatchGames();

	@DefaultMessage( "You can find the most recent games played on the server from <a href=\"/games.rss\" target=\"_blank\">this RSS feed</a>." )
	String faqAnswerCanIWatchGames();

	@DefaultMessage( "Error occured. Please reload page." )
	String errorOccured();

	@DefaultMessage( "There is a new version available, please reload page." )
	String newVersionAvailable();

	@DefaultMessage( "Watch game" )
	String watch();

	@DefaultMessage( "Active games" )
	String gamesList();

	@DefaultMessage( "Leaderboard" )
	String leaderBoard();

	@DefaultMessage( "White" )
	String white();

	@DefaultMessage( "Black" )
	String black();

	@DefaultMessage( "No games." )
	String noGames();

	@DefaultMessage( "Players" )
	String players();

	@DefaultMessage( "Rating" )
	String rating();

	@DefaultMessage( "#" )
	String moveNumber();

	@DefaultMessage( "sec" )
	String byoyomi();

	@DefaultMessage( "You have logged in elsewhere." )
	String youHaveLoggedInElsewhere();

	@DefaultMessage( "Reconnect" )
	String reconnect();

	@DefaultMessage( "New name : " )
	String newName();

	@DefaultMessage( "Determining dead stones." )
	String determiningDeadStones();

}
