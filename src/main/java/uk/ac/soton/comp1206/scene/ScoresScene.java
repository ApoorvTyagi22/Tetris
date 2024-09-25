package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Random;

/**
 * Responsible for displaying the scores after the Game ends.
 */
public class ScoresScene extends BaseScene {

  /**
   * The game instance after which the scene is called
   */
  Game game;

  /**
   * used to log progress in the terminal
   */
  private static final Logger logger = LogManager.getLogger(ScoresScene.class);

  /**
   * the ObservableList scores to display with name
   */
  private ObservableList<Pair<String, Integer>> scores;

  /**
   * the scoresList to display
   */
  private ScoresList scoresList;
  /**
   * the mainPane of the scene
   */
  private BorderPane mainPane;

  /**
   * the stackPane of the scene
   */
  private StackPane scoresScene;

  /**
   * the online highScore list
   */
  private static ListProperty<Pair<String, Integer>> remoteScores;

  /**
   * the remote Score List to display
   */
  private static ScoresList remoteScoreList;

  /**
   * the userName entered by the user
   */
  private String userNameEntered;

  /**
   * Main Multiplayer Title
   */
  private Text localHighScore;

  /**
   * The timeLine that exits from the game scene after 25 seconds
   */
  private Timeline timeline;


  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    this.scores = FXCollections.observableArrayList();
    this.scoresList = new ScoresList(scores);
    this.remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList());
    this.remoteScoreList = new ScoresList(remoteScores);
    localHighScore = new Text("Local High Scores");

  }

  /**
   * Creates a scene to display after the multiplayer game
   * @param gameWindow the game window
   * @param game the game
   * @param isMp if its multiplayer game
   */
  public ScoresScene(GameWindow gameWindow, Game game,Boolean isMp) {
    super(gameWindow);
    this.game = game;
    this.scores = FXCollections.observableArrayList();
    this.scoresList = new ScoresList(scores);
    this.remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList());
    this.remoteScoreList = new ScoresList(remoteScores);
    localHighScore = new Text("Multi-Game Scores");
  }

  /**
   * Designs the list cell that display the scores List
   * @param cell the cell to updated
   * @return returns the updated listCell
   */
  private static ListCell<Pair<String, Integer>> call(ListView<Pair<String, Integer>> cell) {
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
   * Requests highScore from the server
   */
  private void loadOnlineScores() {
    gameWindow.getCommunicator().send("HISCORES");
  }

  /**
   * Builds a Scores scene
   */
  @Override
  public void build() {

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    scoresScene = new StackPane();
    scoresScene.setMaxWidth(gameWindow.getWidth());
    scoresScene.setMaxHeight(gameWindow.getHeight());
    scoresScene.getStyleClass().add("scoreScene-background");
    root.getChildren().add(scoresScene);

    mainPane = new BorderPane();
    scoresScene.getChildren().add(mainPane);

    Image TetresECS = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
    ImageView imageView = new ImageView();
    imageView.setImage(TetresECS);
    imageView.setFitWidth(500);
    imageView.setFitHeight(130);
    VBox tetresEcs = new VBox();
    tetresEcs.getChildren().addAll(imageView);
    tetresEcs.setAlignment(Pos.TOP_CENTER);
    mainPane.setTop(tetresEcs);

    Text highScoreText = new Text("High Scores");
    highScoreText.getStyleClass().add("title");

    scoresList.loadScores(game);
    scoresList.setMaxWidth(300);
    scoresList.setMaxHeight(400);
    scoresList.getStyleClass().add("list-cell");


    scoresList.setVisible(false);

    if(!(game instanceof MultiplayerGame)) {
      if (Game.getScore() > lowestScore() && Game.getScore() < highestScore()) {
//        if (!(game instanceof MultiplayerGame)) {
          updateScorlist();
//        }
      } else if (Game.getScore() > highestScore()) {
//        if (!(game instanceof MultiplayerGame)) {
          updateBoth();
//        }
      } else {
        executionAfterHighScore();
        reveal(scoresList);
        reveal(remoteScoreList);
      }
    } else {
      executionAfterHighScore();
      scoresList.arrangeTheList();
      reveal(scoresList);
      reveal(remoteScoreList);
    }
  }

//  private void KeyEvents(KeyEvent keyEvent) {
//    if(keyEvent.getCode() == KeyCode.ESCAPE) {
//      gameWindow.startMenu();
//    }
//  }

  /**
   * Update the scoreList with the new entry
   */
  private void updateScorlist() {
    try {
      Text gameOverText = new Text("Game Over");
      gameOverText.getStyleClass().add("bigtitle");
      Text enterYourScore = new Text("You got a high score");
      enterYourScore.getStyleClass().add("title");
      TextField userName = new TextField();
      userName.setAlignment(Pos.CENTER);
      userName.getStyleClass().add("TextField");
      Button submitButton = new Button("Submit");
      Text highScores  = new Text("Enter Score");
      highScores.getStyleClass().add("title");
      submitButton.setAlignment(Pos.CENTER);
      VBox vBox1 = new VBox();
      vBox1.setAlignment(Pos.CENTER);
      vBox1.getChildren().addAll(gameOverText, enterYourScore, userName, submitButton, highScores);
      mainPane.setCenter(vBox1);


      submitButton.setOnAction((e) -> {
        String name = userName.getText();
        Pair<String,Integer> stringIntegerPair  = new Pair<>(name, Game.getScore());
        scores.add(stringIntegerPair);
        scoresList.arrangeTheList();
        scoresList.writeScores(scoresList);
        vBox1.setVisible(false);
        userNameEntered = userName.getText();
        executionAfterHighScore();
        reveal(scoresList);
        reveal(remoteScoreList);
      });

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Updates the local and the server score if its even high then the highest local score.
   */
  private void updateBoth() {
    try {
      Text gameOverText = new Text("Game Over");
      gameOverText.getStyleClass().add("bigtitle");
      Text enterYourScore = new Text("You got a high score");
      enterYourScore.getStyleClass().add("title");
      TextField userName = new TextField();
      userName.setAlignment(Pos.CENTER);
      userName.getStyleClass().add("TextField");
      Button submitButton = new Button("Submit");
      Text highScores  = new Text("Enter Score");
      highScores.getStyleClass().add("title");
      submitButton.setAlignment(Pos.CENTER);
      VBox vBox1 = new VBox();
      vBox1.setAlignment(Pos.CENTER);
      vBox1.getChildren().addAll(gameOverText, enterYourScore, userName, submitButton, highScores);
      mainPane.setCenter(vBox1);


      submitButton.setOnAction((e) -> {
        String name = userName.getText();
        Pair<String,Integer> stringIntegerPair  = new Pair<>(name, Game.getScore());
        scores.add(stringIntegerPair);
        scoresList.arrangeTheList();
        scoresList.writeScores(scoresList);
        vBox1.setVisible(false);
        userNameEntered = userName.getText();
        sendHighScore();
        executionAfterHighScore();
        reveal(scoresList);
        reveal(remoteScoreList);
      });

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * returns the highest local score
   * @return local highScore
   */
    private int highestScore() {
    scoresList.arrangeTheList();
    logger.debug("The Current Highest Score is: {}", scoresList.get(scoresList.size() -1));
    return scoresList.get(scoresList.size() -1);
  }

  /**
   * method to execute after the highScore has benn entered
   */
  private void executionAfterHighScore() {
      scoresList.setCellFactory(ScoresScene::call);
      localHighScore.getStyleClass().add("title");
      VBox secondVBox = new VBox();
      secondVBox.setAlignment(Pos.BOTTOM_CENTER);
      secondVBox.getChildren().addAll(localHighScore, scoresList);

//    mainPane.setBottom(secondVBox);

      remoteScoreList.setMaxWidth(300);
      remoteScoreList.setMaxHeight(400);
      remoteScoreList.getStyleClass().add("list-cell");
      loadOnlineScores();
      gameWindow.getCommunicator().addListener(getCommunicationsListener());
      System.out.println(remoteScoreList.toString());
      remoteScoreList.setCellFactory(ScoresScene::call);

      Text onlineHighScore = new Text("Online High Scores");
      onlineHighScore.getStyleClass().add("title");
      VBox thirdVBox = new VBox();
      thirdVBox.setAlignment(Pos.BOTTOM_CENTER);
      thirdVBox.getChildren().addAll(onlineHighScore, remoteScoreList);

      HBox hBox = new HBox();
      hBox.getChildren().addAll(secondVBox, thirdVBox);
      hBox.setSpacing(90);
      mainPane.setCenter(hBox);
  }

  /**
   * Returns the CommunicationsListener
   * @return CommunicationsListener
   */
  private  CommunicationsListener getCommunicationsListener() {
    return ScoresScene::receiveCommunication;
  }

  /**
   * waits for the java application thread to free up to call processMessage
   * @param message message received from the server
   */
  private static void receiveCommunication(String message) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        var messageRecieved  =message.split(" ");
        if(messageRecieved[0].equals("HISCORES")){
          processMessage(message);
        } else if (messageRecieved[0].equals("NEWSCORE")) {
          logger.info("You High Score of {} has been sent to the server", Game.getScore());
          logger.info("Info Returned from the Server {}", message);
        }
        displayRemoteList();
      }
    });
  }

  /**
   * Configures the cell Factory for the remote score list.
   */
  private static void displayRemoteList() {
    remoteScoreList.setCellFactory(ScoresScene::call);
  }

  /**
   * Process the message received from the server
   * @param message message received from the server
   */
  private static void processMessage(String message) {
      var messageWithOutHighScore = message.split(" ");
      var split  = messageWithOutHighScore[1].split("\n");
    for (int i = 0; i < split.length ; i++) {
      var array = split[i].split(":");
      remoteScores.add(
              new Pair<>(array[0], Integer.parseInt(array[1]))
      );
    }
  }

  /**
   * sends highScore to the server
   */
  public void sendHighScore() {
    gameWindow.getCommunicator().send("HISCORE " + userNameEntered + ":" + Game.getScore());
  }

  /**
   * returns the lowest score in the list
   * @return lowest score
   */
  private int lowestScore() {
    scoresList.arrangeTheList();
    logger.debug("The Current Lowest Score is: {}", scoresList.get(0));
    return scoresList.get(0);
  }


  /**
   * reveals the scoreList
   * @param scoresList scorelist
   */
  public void reveal(ScoresList scoresList){
    scoresList.setVisible(true);
    FadeTransition fade = new FadeTransition();
        fade.setNode(scoresList);
        fade.setDuration(Duration.millis(3000));
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
  }


  /**
   * Initialises the scene
   */
  @Override
  public void initialise() {
    timeline = new Timeline(new KeyFrame(Duration.seconds(25), event -> endGame()));
    timeline.setCycleCount(1); // Run only once
    timeline.play();
    getScene().setOnKeyPressed(this::keyPressed);
  }

  /**
   * defines the action when escape key Pressed
   * @param keyEvent the keyEvent that has occurred
   */
  private void keyPressed(KeyEvent keyEvent) {
    if(keyEvent.getCode() == KeyCode.ESCAPE) {
      gameWindow.startMenu();
    }
  }

  /**
   * Defines what should happen when the game ends
   */
  private void endGame() {
//    game.shutDown();
    gameWindow.startMenu();

  }

}
