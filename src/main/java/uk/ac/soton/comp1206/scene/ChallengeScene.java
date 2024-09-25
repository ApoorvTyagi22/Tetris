package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;

import static javafx.scene.input.KeyCode.*;


/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {


  /**
   * used to log statements in the terminal
   */
  protected static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * The Game instance for this scene
   */
  Game game;

  /**
   * The pieceBoard instance to display the current piece
   */
  protected PieceBoard pieceBaord;

  /**
   * the pieceBoard instance to display the following piece
   */
  protected PieceBoard secondPieceBoard;

  /**
   * The VBox to hold the pieceBoards
   */
  protected VBox pieceBoardVBox;

  /**
   * boolean to verify id the keyboard has been initialized
   */
  private boolean keyBoardInitialized = false;

  /**
   * an int to hold  x coordinate for keyBoard controls
   */
  private int trackerYCoordinate;

  /**
   * an int to hold y coordinate for keyBoard controls
   */
  private int trackerXCoordinate;

  /**
   * Text to hold the title of the scene
   */
  Text challengeModeText;

  /**
   * VBox to hold HighScores
   */
  protected VBox highScoreBox;

  /**
   * FileReader to read a file
   */
  private FileReader fileReader;

  /**
   * BufferReader to read a FileReader
   */
  private BufferedReader reader;

  /**
   * File to read
   */
  private File file;

  /**
   * HBox for the bottom of the centerPane
   */
  protected HBox bottomHBox;
  private boolean methodCalled = false;
  protected VBox vBox1;

  /**
   * getter to return the board instance
   * @return board instance
   */
  public GameBoard getBoard() {
    return board;
  }

  /**
   * The board instance of this scne
   */
  protected GameBoard board;

  /**
   * TimeLine instance to add animations
   */
  protected Timeline timeline;


  /**
   * Returns the BorderPane
   * @return the mainPane
   */
  public BorderPane getMainPane() {
    return mainPane;
  }

  /**
   * The mainPane instance
   */
  protected BorderPane mainPane;

  /**
   * Returns the rectangle of the timeBar animation
   * @return the rectangle timeBar
   */
  public Rectangle getTimerBar() {
    return timerBar;
  }

  /**
   * The timeBar rectangle
   */
  protected Rectangle timerBar;
  /**
   * The stackPane instance of this class
   */
  protected StackPane challengePane;

  /**
   * boolean to see if the game Has not shut down
   */
  public boolean hasNotShutDownYet = true;

  /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        challengeModeText = new Text("Challenge Mode");
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

         mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2,true);
        mainPane.setCenter(board);

        logger.debug("Before creating any pieceBoard");
        //logger.debug("Width for the smaller window {}",gameWindow.getWidth()/5);
        pieceBaord = new PieceBoard(3,3,160,160,false);
        logger.debug("Creating the first pieceBoard {}", pieceBaord);

        secondPieceBoard = new PieceBoard(3,3,90,90,false);
        logger.debug("Creating the Second pieceBoard {}", secondPieceBoard);
        secondPieceBoard.setAlignment(Pos.CENTER);
        Text highScoreText = new Text("HighScore");
        highScoreText.getStyleClass().add("hiscore");
        Text highScore = new Text();
        highScore.getStyleClass().add("myscore");

        Text MultiplierText = new Text("Multiplier");
        MultiplierText.getStyleClass().add("hiscore");
        Text multiplier = new Text();
        multiplier.textProperty().bind(Game.multiplierProperty().asString());
        multiplier.getStyleClass().add("myscore");

        VBox multiplerBox = new VBox();
        multiplerBox.getChildren().addAll(MultiplierText,multiplier);


        file  = new File("src" + File.separator + "localScores.txt");
        if (!file.exists()) {
          // Create a file and default scores to the file
          highScore.setText("1000");
        } else {
          try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            if (fileIsReady()) {
              var line = reader.readLine().split(":");
              highScore.setText(line[1]);
            }
          }catch (Exception e) {
            highScore.setText("1000");
          }
        }

        Text levelText = new Text("Lvl");
        levelText.getStyleClass().add("hiscore");
        Text level = new Text();
        level.textProperty().bind(Game.levelProperty().asString());
        level.getStyleClass().add("myscore");

        Text skipsRemaining = new Text("Skips:");
        skipsRemaining.getStyleClass().add("myscore");
        Text skip = new Text();
        skip.getStyleClass().add("myscore");
        skip.textProperty().bind(Game.skipProperty().asString());
        HBox hBox = new HBox();
        hBox.getChildren().addAll(skipsRemaining,skip);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(levelText,level);
        highScoreBox = new VBox();
        highScoreBox.getChildren().addAll(highScoreText,highScore);

        bottomHBox = new HBox();
        bottomHBox.getChildren().addAll(multiplerBox, vBox, highScoreBox);
        bottomHBox.setSpacing(20);
        vBox1 = new VBox();
        vBox1.getChildren().addAll(bottomHBox,hBox);

        pieceBoardVBox = new VBox();
        pieceBoardVBox.getChildren().addAll(pieceBaord,secondPieceBoard,vBox1);
        pieceBoardVBox.setSpacing(30);
        pieceBoardVBox.setAlignment(Pos.CENTER);
        mainPane.setRight(pieceBoardVBox);


        //Creating the text for score
        Text scoreText = new Text("Score");
        scoreText.getStyleClass().add("score");
        Text score = new Text();
        score.getStyleClass().add("myscore");
        score.textProperty().bind(Game.scoreProperty().asString());

        VBox scoreBox = new VBox();
        scoreBox.getChildren().addAll(scoreText,score);
        scoreBox.setAlignment(Pos.TOP_LEFT);

        Text lifeText = new Text("Lives");
        lifeText.getStyleClass().add("score");
        Text lives = new Text();
        lives.getStyleClass().add("myscore");
        lives.textProperty().bind(Game.livesProperty().asString());

        //Creating a Vbox to hold the Lifes property
        VBox livesBox = new VBox();
        livesBox.getChildren().addAll(lifeText,lives);
        livesBox.setAlignment(Pos.TOP_RIGHT);

        challengeModeText.getStyleClass().add("title");

        HBox topHorizontalBox = new HBox();
        topHorizontalBox.getChildren().addAll(scoreBox,challengeModeText,livesBox);
        topHorizontalBox.setSpacing(132);


        mainPane.setTop(topHorizontalBox);
//        if(root != null) {logger.debug("root is null");}


//        VBox vbox = new VBox( );
//        Text timerText  = new Text();

        setTimeline();
        mainPane.setBottom(timerBar);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        pieceBaord.setOnBlockClick(this::pieceBoardClicked);
        game.setNextPieceListener(this::displayBlock);
        secondPieceBoard.setOnBlockClick(this::swapPieces);
        board.setOnRightClicked(() -> game.rotateCurrentPiece());
        game.setLinesClearedListener(this::linesCleared);
//        game.setOnGameLoop(this::setTimeline);
       game.setOnGameLoop(() -> {
 //Start timer animation
        resetTimerBar();
         });
    }

  /**
   * Checks if the file is ready to be read
   * @return if the file is ready
   */
  private boolean fileIsReady() {
    if (reader != null) {
      try {
        return reader.ready();
      } catch (IOException exception) {
        System.out.println(exception);
        return false;
      }
    }
    return false;
  }

  /**
   * Sets the timeLine for the timeBar animation and defines all the necessary Key Frames
   */
  public void setTimeline() {
        timerBar = new Rectangle(800, 20, Color.DARKGREEN);

        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timerBar.widthProperty(),0)),


                new KeyFrame(Duration.millis((double) game.getTimerDelay() /5),
                                new KeyValue(timerBar.widthProperty(),160),
                                new KeyValue(timerBar.fillProperty(), Color.LIGHTGREEN)),

                new KeyFrame(Duration.millis((double) (game.getTimerDelay() * 2) /5),
                                new KeyValue(timerBar.widthProperty(),320),
                                new KeyValue(timerBar.fillProperty(), Color.GREEN)),

                new KeyFrame(Duration.millis((double) (game.getTimerDelay() * 3) /5),
                        new KeyValue(timerBar.widthProperty(),480),
                        new KeyValue(timerBar.fillProperty(), Color.ORANGE)),

                new KeyFrame(Duration.millis((double) (game.getTimerDelay() * 4) /5),
                        new KeyValue(timerBar.widthProperty(),640),
                        new KeyValue(timerBar.fillProperty(), Color.RED) ),

                new KeyFrame(Duration.millis(game.getTimerDelay()),
                        new KeyValue(timerBar.widthProperty(),800),
                        new KeyValue(timerBar.fillProperty(), Color.DARKRED)),

                new KeyFrame(Duration.ZERO, new KeyValue(timerBar.widthProperty(), 0)),

                new KeyFrame(Duration.ZERO, actionEvent -> {
                  if (Game.getLives() ==-1 && hasNotShutDownYet) {
                      logger.debug("Inside The end KeyEvent in the timeBar Timeline value of boolean: {}", hasNotShutDownYet);
                      hasNotShutDownYet = false;
                      runOnce();
                      Game.setLives(-2);
                  }
                })

        );
        timeline.setCycleCount(Animation.INDEFINITE);
//        timeline.setOnFinished((e) -> {
//          game.shutDown();
//        });
        timeline.play();
    }

  protected void runOnce() {
    if (!methodCalled) {
      logger.debug("Inside RunOnce");
      callScoresScene();
      game.shutDown();
      timeline.stop();
      methodCalled = true;
    }
  }

  /**
   * Starts the scoreScene
   */
  public void callScoresScene() {
    gameWindow.startScoresScene(game);
  }

  /**
   * Handle when a block is clicked
   * @param gameBlock the Game Block that was clocked
   */
  public void blockClicked(GameBlock gameBlock) {
    Boolean isPlaced= game.blockClicked(gameBlock);
    blockPlayedTimerUpdated(isPlaced);
  }

  /**
   * if the block isPlayed the timer is reset
   * @param isPlaced if the block is played
   */
  public void blockPlayedTimerUpdated(Boolean isPlaced) {
    if(isPlaced) {
      resetTimerBar();
    }
  }

  /**
   *  Starts the timeLine
   */
  public void start() {
        timeline.playFromStart();
    }

  /**
   * Restarts the TimeLine
   */
  public void resetTimerBar() {
        timeline.stop();
            start();
    }

  /**
   * Creates the LinesClear animation on the blocks when a line is cleared
   * @param gameBlockCoordinates the coordinates of the blocks that are cleared
   */
  public void linesCleared(HashSet<GameBlockCoordinate> gameBlockCoordinates) {
        board.fadeOut(gameBlockCoordinates);
    }

  /**
   * Swaps the currentPiece and the following piece
   * @param gameBlock
   */
  public void swapPieces(GameBlock gameBlock) {
        game.swapCurrentPiece();
    }

  /**
   * Rotates the current piece
   * @param gameBlock the block to rotate
   */
  public void pieceBoardClicked(GameBlock gameBlock)  {
        game.rotateCurrentPiece();
    }


  /**
   * Displays the provided piece on the pieceBoards
   * @param piece the first piece to display
   * @param secondPiece the following piece to display
   */
  public void displayBlock(GamePiece piece,GamePiece secondPiece) {
      pieceBaord.pieceToDisplay(piece);
      secondPieceBoard.pieceToDisplay(secondPiece);
      logger.debug(" 1. Piece {} 2. Piece {}", piece.toString(), secondPiece.toString());

  }
    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");
        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        scene = gameWindow.getScene();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
    }

  /**
   * Handles the key pressed
   * @param keyEvent The keyEvent that has occurred
   */
  public void keyPressed(KeyEvent keyEvent) {
      var keyCode = Objects.requireNonNull(keyEvent.getCode());
      logger.info("Key Pressed: {}", keyCode);
      if (Objects.requireNonNull(keyEvent.getCode()) == ESCAPE) {
        escapeKeyPressed(keyEvent);
      }
      if ((keyCode) == W || keyCode == KeyCode.UP) {
        logger.info("Moving the KeyBoard tracker up");
        extracted();
        //Board Y position ++
      }
      else if ((keyCode) == A || keyCode == KeyCode.LEFT) {
        logger.info("Moving the KeyBoard tracker left");
        extracted1();
      }
      else if ((keyCode) == S || keyCode == KeyCode.DOWN) {
        logger.info("Moving the KeyBoard tracker down");
        extracted2();
      }
      else if ((keyCode) == D || keyCode == KeyCode.RIGHT) {
        logger.info("Moving the KeyBoard tracker right");
        extracted3();
      }
      else if (keyCode == ENTER || keyCode == X) {
       var played =  game.blockClicked(getBoard().getBlock(trackerXCoordinate,trackerYCoordinate));
       if(played) {
         resetTimerBar();
       }
      } else if (keyCode == E) {
        game.rotateCurrentPiece();
      } else if(keyCode == SPACE) {
        game.swapCurrentPiece();
      } else if (keyCode == K) {
        if(!(game instanceof MultiplayerGame)) {
          game.useSkip();
        }  else {
          logger.info("Cant Skip pieces In multiplayer");
        }
      }
  }

  /**
   * Method moves the keyBoard tracker to the right
   */
  private void extracted3() {
    if (keyBoardInitialized) {
      if ((trackerXCoordinate + 1 >= 0) && (trackerXCoordinate < 5)) {
        trackerXCoordinate++;
        board.highlightBlock(getBoard().getBlock(trackerXCoordinate, trackerYCoordinate));
      } else {
        inalizeKeyboard();
      }//Board X position ++;
    }
  }


  /**
   * Method moves the keyBoard tracker to the down
   */
  private void extracted2() {
    if (keyBoardInitialized) {
      if (((trackerYCoordinate + 1) >= 0) && ((trackerYCoordinate + 1) < 5)) {
        trackerYCoordinate++;
        board.highlightBlock(getBoard().getBlock(trackerXCoordinate, trackerYCoordinate));
      }
    } else {
      inalizeKeyboard();
    }
    //Board Y position --;
  }

  /**
   * Method moves the keyBoard tracker to the left
   */
  private void extracted1() {
    if (keyBoardInitialized) {
      if ((trackerXCoordinate - 1 >= 0) && (trackerXCoordinate < 5)) {
        trackerXCoordinate--;
        board.highlightBlock(getBoard().getBlock(trackerXCoordinate, trackerYCoordinate));
      }
    } else {
      inalizeKeyboard();
    }
    //Board X position --
  }

  /**
   * Method moves the keyBoard tracker to the up
   */
  private void extracted() {
    if (keyBoardInitialized) {
      if ((trackerYCoordinate - 1 >= 0) && (trackerYCoordinate - 1 < 5)) {
        trackerYCoordinate--;
        board.highlightBlock(getBoard().getBlock(trackerXCoordinate, trackerYCoordinate));
      } else {
        logger.info("Not Valid");
      }
    } else {
      inalizeKeyboard();
    }
  }


  /**
   * this method does what it should do when an escape key is pressed
   * @param keyEvent
   */
  public void escapeKeyPressed(KeyEvent keyEvent) {
    logger.info("Key pressed: " + keyEvent.getCode());
        logger.info("Escape key pressed");
        game.shutDown();
        timeline.stop();
        logger.info("Stopping animation TimeLine");
        gameWindow.startMenu();
  }


  /**
   * Internalises the keyboard tracker with 0,0 coordinates
   */
  private void inalizeKeyboard() {
    keyBoardInitialized = true;
    trackerYCoordinate = 0;
    trackerXCoordinate = 0;

  }
 }
