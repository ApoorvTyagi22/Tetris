package uk.ac.soton.comp1206.game;


import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Media.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.*;

/**
 * The MultiplayerGame class extends the main logic of the Game class and handles what should happen
 * in a multiplayer game.
 */
public class MultiplayerGame extends Game {

  /**
   * Used to log statements in the terminal
   */
  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

  /**
   * Used to communicate with the server
   */
  private final Communicator communicator;

  /**
   * Add pieces in the same order they are received from the server
   */
  private final Queue<GamePiece> pieceQueue;

  /**
   * Used to distinguish if its the first Play
   */
  private boolean firstTime = true;

  /**
   *  used to add a list for onlineScores
   */
  private List<Pair<String, Integer>> onlineScorelist = new ArrayList<>();

  /**
   * List called by scores Scene
   */
  private List<Pair<String, Integer>> onlineScoreListToSend = new ArrayList<>();


  /**
   * A hashset to store the name of all current players in the MultiPlayer Game
   */
  private HashSet<Object> playerNames;

  /**
   * THe scrollPane to add
   */
  private ScrollPane scrollPane;

  /**
   * Map of current players with their scores
   */
  private Map<String, Integer> playerScoresMap;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.pieceQueue = new LinkedList<>();
    this.communicator = communicator;
//    communicator.addListener((message) -> Platform.runLater(() -> this.processMessage(message)));
//    requestPiecesFromServer();

  }

  /**
   * Overrides the Game class start method to add communicators and send messages
   */
  @Override
  public void start() {
    communicator.clearListeners();
    communicator.addListener((message) -> Platform.runLater(() -> this.processMessage(message)));
    requestPiecesFromServer();
    requestPiecesFromServer();
    super.start();
    //logger.debug("Piece Queue in start is {}, CP is {}, FP: {}", pieceQueue,currentPiece,followingPiece);
  }

  /**
   * Request a new piece from the server
   */
  private void requestPiecesFromServer() {
    // Send a message to the server to request piece stream
    logger.info("Sending Piece Request");
    communicator.send("PIECE");
  }

  /**
   * Outlines the logic to process the message received from the server
   * @param message the message received from the server
   */
  public void processMessage(String message) {
    if (message.startsWith("PIECE")) {
      // Extract the piece information from the message
      String[] parts = message.split(" ");
      if (parts.length == 2) {
        // Create a new piece based on the received data and add it to the piece queue
        GamePiece newPiece = GamePiece.createPiece(Integer.parseInt(parts[1].trim()));
        pieceQueue.offer(newPiece);
        //System.out.println(pieceQueue);
        if(firstTime) {
          nextPiece();
          firstTime = false;
        }
      }
    }  else if(message.startsWith("SCORES")) {
      String[] parts = message.split(" ");

      var eachPlayer  = parts[1].split("\n");
//      onlineScorelist.clear();
      playerScoresMap = new HashMap<>();

      for (String data : eachPlayer) {
        String[] info = data.split(":");
        String playerName = info[0];
        int playerScore = Integer.parseInt(info[1]);

        if(info[2].equals("DEAD")) {
          if(!(nameAlreadySavedInList(playerName))){
            onlineScoreListToSend.add(new Pair<>(playerName, playerScore));
          }
        }

        playerScoresMap.put(playerName, playerScore);
      }
        onlineScorelist.clear();
      for (Map.Entry<String, Integer> entry : playerScoresMap.entrySet()) {
        String playerName = entry.getKey();
        int playerScore = entry.getValue();

        // Check if the player is eliminated, if so, modify the name
        if (onlineScoreListToSend.contains(new Pair<>(playerName, playerScore))) {
          playerName += " DEAD";
        }
        // Add the player name and score to the onlineScorelist
        onlineScorelist.add(new Pair<>(playerName, playerScore));
      }
    }



    else if(message.startsWith("MSG")) {
      Text text = new Text(message.substring(4) + "\n");
      scrollPane.setContent(text);
    }

  }

  /**
   * Check if name already exists in the onlineScoreList to send
   * @param playerName name to check
   * @return if exists
   */
  private boolean nameAlreadySavedInList(String playerName) {
    boolean playerExists = false;
    for (Pair<String, Integer> pair : onlineScoreListToSend) {
      if (pair.getKey().equals(playerName)) {
        playerExists = true;
        break;
      }
    }
   return playerExists;
  }


  /**
   * Moves the game to the next piece in the sequence.
   * If the piece queue is not empty, the current piece is replaced with the next piece in the queue,
   * This method overrides a superclass method.
   */
  @Override
  public void nextPiece() {
    if (!pieceQueue.isEmpty()) {
      currentPiece = followingPiece;
      followingPiece = followingPiece();
      logger.info("next Piece is : {}", currentPiece);
      callToListener();
    }
  }

  /**
   * This method pulls a piece form the pieceQueue and sets it as the following piece, and requests
   * for a new piece.
   * @return the piece to be set as the following piece
   */
  @Override
  public GamePiece followingPiece() {
    GamePiece piece = pieceQueue.poll();
    requestPiecesFromServer();
    logger.debug("Inside the following Piece method and the current piece Being Retrieved is: {} ", piece);
    return piece;
  }


  /**
   * This method requests for piece from the server, and calls the super method.
   * This method overrides a superclass method.
   * @param x x coordinate of where the block is to be played
   * @param y y coordinate of where the block is to be played
   * @return if the block has been played or not
   */
  @Override
  protected boolean playingPiece(int x, int y) {
    requestPiecesFromServer();
    /**
     *Used to
     */
    return super.playingPiece(x,y);

  }

  /**
   * This method shuts down the multiplayer game
   * This method overrides a superclass method.
   */
  @Override
  public void shutDown() {
    super.shutDown();
    communicator.send("QUIT");
    communicator.clearListeners();
  }


  /**
   * This method calculates the boardValues of the multiplayer Game board and adds it all to a string
   * to send it to the server
   * @return boardValues as a string
   */
  @Override
  public String boardValues() {
    StringBuilder buildingTheMessage = new StringBuilder();
    buildingTheMessage.append("BOARD ");
    for (int row = 0; row < 5; row++) {
      for (int col = 0; col < 5; col++) {
        buildingTheMessage.append(getGrid().get(row, col));

      }
    }
    String toSnend = buildingTheMessage.toString();
    return toSnend;
  }

  /**
   * Updates the pieces after the game timer has ended
   */
  @Override
  public void timerEndGamePieceUpdate() {
    currentPiece = followingPiece;
    followingPiece = followingPiece();

  }

  /**
   * returns the onlineScoreList
   * @return returns the onlineScoreList
   */
  @Override
  public List<Pair<String, Integer>> getLeaderBoardScores() {
    return onlineScorelist;
  }

  /**
   * Display the leaderBaord for scores Scene
   * @return leadboard scores
   */
  @Override
  public List<Pair<String, Integer>> leaderBoardForScoresScene() {
    for (Pair<String, Integer> pair : onlineScorelist) {
     if(!(onlineScoreListToSend.contains(pair)) && !(pair.getKey().endsWith("DEAD"))) {
        onlineScoreListToSend.add(pair);
     }
    }
      return onlineScoreListToSend;

  }


  /**
   * Sets the scrollPane
   * @param scrollPane ScrollPane in which the content is to be displayed.
   */
  @Override
  public void displayInScrollPane(ScrollPane scrollPane) {
    this.scrollPane = scrollPane;
  }
}
