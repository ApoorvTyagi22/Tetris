package uk.ac.soton.comp1206.scene;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.security.KeyException;
import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class is responsible for the multiplayerScene which displays the main multiplayer Game
 */
public class MultiplayerScene extends ChallengeScene {

  /**
   * ObservableList of the live leaderboard scores
   */
  private static ObservableList<Pair<String, Integer>> leaderboardScores;

  /**
   * The live leaderBoard
   */
  private Leaderboard leaderboard;


  /**
   * Timer responsible for constantly requesting for updated Scores
   */
  private Timer timer2;

  /**
   *
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
    gameWindow.getCommunicator().send("START");
    challengeModeText = new Text("MultiPlayer Mode");
    this.leaderboardScores = FXCollections.observableArrayList();
    this.leaderboard = new Leaderboard(leaderboardScores);
    leaderboard.getStyleClass().add("leaderboard");

  }

  /**
   * Sets the gameUp
   */
  @Override
  public void setupGame() {
    logger.info("Starting a new multiplayer game");
    // Start new multiplayer game
    game = new MultiplayerGame(5, 5, gameWindow.getCommunicator()); // Assuming MultiplayerGame constructor takes grid dimensions and communicator
  }

  /**
   * Quits the game
   * @param keyEvent keyEvent
   */
  @Override
  public void escapeKeyPressed(KeyEvent keyEvent) {
    super.escapeKeyPressed(keyEvent);
    gameWindow.getCommunicator().send("QUIT");
  }


  /**
   * Builds the multiplayerGame scene
   */
    @Override
  public void build() {
    super.build();
    leaderboard.setMaxHeight(80);
    leaderboard.setMaxWidth(200);
    Text leadBoardTest = new Text("Live Scores");
    leadBoardTest.setFont(Font.font(16));
    VBox leadBaordVBox = new VBox();
    leadBaordVBox.getChildren().addAll(leadBoardTest,leaderboard);
//    leadBoardTest.fillProperty().set(Paint.valueOf("green"));
    leaderboard.setBackground(Background.fill(Color.LIGHTCYAN));
    leadBaordVBox.setBackground(Background.fill(Color.LIGHTCYAN));
    //leadBaordVBox.getStyleClass().add("leaderBoard");
    pieceBoardVBox.getChildren().remove(vBox1);
    pieceBoardVBox.getChildren().addAll(leadBaordVBox);
    pieceBoardVBox.setSpacing(20);
    pieceBoardVBox.setAlignment(Pos.CENTER);
    mainPane.setRight(pieceBoardVBox);
    setupLeaderBoardUpdateTimer();
    VBox vBox = new VBox();
    TextField textField = new TextField();

//    textField.setHgrow(Priority.ALWAYS);
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setPrefSize(400,1);
    VBox messagesBox = new VBox();
    messagesBox.getChildren().addAll(scrollPane,textField);
    vBox.getChildren().addAll(messagesBox,timerBar);
    vBox.setFillWidth(true);
    vBox.setSpacing(2);
    messagesBox.getStyleClass().add("leaderboard");
    mainPane.setBottom(vBox);


    textField.setOnKeyPressed(ActionEvent -> {

      if (ActionEvent.getCode() == KeyCode.ENTER) {
        String tosend  = textField.getText();
        gameWindow.getCommunicator().send("MSG " + tosend);
        textField.clear();
        game.displayInScrollPane(scrollPane);
      }

    });

  }

  /**
   * sends the server the necessary information about after the block has been played
   * @param isPlaced if the block is played
   */
  public void blockPlayedTimerUpdated(Boolean isPlaced) {
      super.blockPlayedTimerUpdated(isPlaced);
      sendUpdatedScores();
      gameWindow.getCommunicator().send(game.boardValues());
      gameWindow.getCommunicator().send("LIVES " + Game.getLives());
  }

  /**
   * sends the server the updated score
   */
  private void sendUpdatedScores() {
    logger.info("Sending Score");
    gameWindow.getCommunicator().send("SCORE " + Game.getScore());
  }

  /**
   *timer used to  requests leaderInformation
   */
  private void setupLeaderBoardUpdateTimer() {

    TimerTask leadBoardUpdate = new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            if(hasNotShutDownYet) {
              gameWindow.getCommunicator().send("SCORES");
              leaderboardScores.clear();
              leaderboard.clear();
              leaderboardScores.addAll(game.getLeaderBoardScores());
              leaderboard.setCellFactory(null);
              leaderboard.setCellFactory(MultiplayerScene::call);
            } else{
              timer2.cancel();
            }
          }
        });}
    };
    timer2  = new Timer();
    timer2.scheduleAtFixedRate(leadBoardUpdate, 0,3000);
  }

  @Override
  protected void runOnce() {
    hasNotShutDownYet = false;
    logger.info("Timer Before: {}", timer2);
    timer2 = new Timer();
    logger.info("Timer After: {}", timer2);
    super.runOnce();
  }


  private static ListCell<Pair<String, Integer>> call(ListView<Pair<String, Integer>> pairListView) {
    return new ListCell<>() {
      @Override
      protected void updateItem(Pair<String, Integer> item, boolean empty) {
        updateCell(item, empty);
      }

      private void updateCell(Pair<String, Integer> item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
          setText(item.getKey() + ": " + item.getValue());
//          setFont(Font.font(16));
          getStyleClass().add("channelItem");
          Color[] colors = {Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.LIGHTGREEN};
          Random random = new Random();
          setTextFill(colors[random.nextInt(5)]);
        }
      }
    };
  }

  /**
   * Starts the scores scene
   */
  @Override
  public void callScoresScene() {
    gameWindow.startScoresScene(this.game, true);
  }
}
