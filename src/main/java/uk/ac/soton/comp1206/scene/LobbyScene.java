package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.util.*;

import static javafx.scene.input.KeyCode.*;

/**
 * This is the multiplayerGame lobbyScene class that displays all the current games that are available to
 * join and helps communicate within that channel.
 */
public class LobbyScene extends BaseScene {

  /**
   * Used to print logging statements in the terminal
   */
  private static final Logger logger = LogManager.getLogger(LobbyScene.class);

  /**
   * List used to display all the channel names in the scene
   */
  private static ListView<String> listView;
  /**
   * ObservableList for that list
   */
  private static ObservableList<String> channelList = FXCollections.observableArrayList();

  /**
   * The chat text message received from the server
   */
  private static TextFlow textRecieved;

  /**
   * The timer that request for the latest channel list at regular time interval
   */
  private Timer timer;

  /**
   * the statckPane that holds the lobbyScene
   */
  private StackPane lobbyScene;

  /**
   * The mainPane BorderScene
   */
  private static BorderPane mainPane;

  /**
   * the VBox that holds the left hand side of the scene: list, textField,etc
   */
  private static VBox vBox;

  /**
   * the userList
   */
  private static ArrayList<String> userList = new ArrayList<>();


  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    listView = new ListView<>(channelList);
    listView.setVisible(false);
    listView.setMaxWidth(300);
    listView.setMaxHeight(300);
    listView.setItems(channelList);
  }

  /**
   * initialises the scene
   */
  @Override
  public void initialise() {
    scene = gameWindow.getScene();
    scene.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
  }

  /**
   * initializes the menuScene when escape is pressed
   * @param keyEvent
   */
  private void keyPressed(KeyEvent keyEvent) {
    logger.info("Key pressed: " + keyEvent.getCode());
    if (Objects.requireNonNull(keyEvent.getCode()) == ESCAPE) {
      logger.info("Escape key pressed");
      timer.cancel();
      gameWindow.getCommunicator().send("QUIT");
      gameWindow.startMenu();
    }
  }

  /**
   * Starts the timer to request the list
   */
  private void initialiseTimer() {
    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        requestList();
      }
    };
    timer  = new Timer();
    timer.scheduleAtFixedRate(timerTask,0,2000);
    gameWindow.getCommunicator().addListener(listener());

  }

  /**
   * Rewuests the list
   */
  private void requestList() {
    logger.info("Requesting List");
    gameWindow.getCommunicator().send("LIST");
  }

  /**
   * Creates a channel
   * @param nameOfChannel the name to create it qith
   */
  private void requestCreation(String nameOfChannel) {
    gameWindow.getCommunicator().send("CREATE " + nameOfChannel);
  }

  /**
   * Requests to join a channel which has been created by someone else
   * @param nameOfChannel name of the channel to join
   */
  private void requestJoining(String nameOfChannel) {
    gameWindow.getCommunicator().send("JOIN " + nameOfChannel);
  }

  /**
   * Request to leave
   */
  private void requestExit(){
    gameWindow.getCommunicator().send("QUIT");
  }

  /**
   * Returns a CommunicationsListener object that processes a message.
   * @return the CommunicationsListener object
   */
  private CommunicationsListener listener() {
    return s -> getMessage(s);
  }

  /**
   * waits for the application thread to be free to execute the processMessage on it
   * @param s the message to process
   */
  private void getMessage(String s) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        processMessage(s);
      }
    });
  }

  /**
   * Process the message based on its starting the command
   * @param message the message to process
   */

  private void processMessage(String message) {
    var splitMessage  = message.split(" ");

    logger.info("Message received from the server {}", message);

    switch (splitMessage[0]) {
      case "CHANNELS" -> {
        logger.info("Channel List Received");
        if(splitMessage.length == 1) {
          logger.info("There are no games Currently active");
          return;
        }
        var differentChannels = splitMessage[1].split("\n");
        for (int i = 0; i < differentChannels.length ; i++) {
          if(doesNotExist(differentChannels[i])) {
            channelList.add(differentChannels[i]);
          }
        }
        ArrayList<String> ch = new ArrayList<>(channelList);
        ArrayList<String> differentChannelsList = new ArrayList<>(Arrays.asList(differentChannels));

        for (int i = ch.size() - 1; i >= 0; i--) {
          if (differentChannelsList.contains(ch.get(i))) {
            ch.remove(i);
          }
        }
        channelList.removeAll(ch);
        updateUIWithChannels();
      }
      case "JOIN" -> {
        logger.info("Joining Game");
        /// Join The game
        joinGame();
      }
      case "ERROR" -> {
        //Error occurred while joining the game.
        logger.debug("Error Occurred {}", message);
      }
      case "NICK" -> {
        logger.info("The name has been successfully changed {}", message);
      }

      case "MSG" -> {
        Text text = new Text(message.substring(4) + "\n");
        textRecieved.getChildren().add(text);
      } case "START" -> {
        logger.info("The game is starting");
        startMultiPlayerGame();

      } case "USERS" -> {
        var currentUserList  = splitMessage[1].split("\n");
        userList.addAll(Arrays.asList(currentUserList));
      }

    }

  }

  /**
   * Makes sure the game doesn't Exist
   * @param differentChannel
   * @return
   */
  private static boolean doesNotExist(String differentChannel) {
    return !channelList.contains(differentChannel);
  }

  /**
   * Updates the UI
   */
  private static void updateUIWithChannels() {
    listView.setCellFactory(updateListView());
    listView.setVisible(true);
  }

  /**
   * Builds the scene
   */
  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    lobbyScene = new StackPane();
    lobbyScene.setMaxWidth(gameWindow.getWidth());
    lobbyScene.setMaxHeight(gameWindow.getHeight());
    lobbyScene.getStyleClass().add("lobbyScene-background");
    root.getChildren().add(lobbyScene);

    mainPane = new BorderPane();
    lobbyScene.getChildren().add(mainPane);



    Text currentGames = new Text("Current Games");
    currentGames.getStyleClass().add("title");
    Text enterName  = new Text("join a Game");
    enterName.getStyleClass().add("title");
    TextField textField = new TextField();
    Button button = new Button("Submit");
    vBox = new VBox();
    vBox.getChildren().addAll(currentGames,listView,enterName,textField,button);
    listView.setCellFactory(updateListView());
    listView.setVisible(true);
    mainPane.setLeft(vBox);
    button.setOnAction(actionEvent -> {
        String nameOfChannel = textField.getText();
        if(inList(nameOfChannel)) {
          requestJoining(nameOfChannel);
        } else {
          requestCreation(nameOfChannel);}
    });

    initialiseTimer();


  }

  private static Callback<ListView<String>, ListCell<String>> updateListView() {
    return stringListView -> new ListCell<>() {
      @Override
      protected void updateItem(String s, boolean b) {
        updateCell(s, b);
      }

      private void updateCell(String s, boolean b) {
        super.updateItem(s, b);
        if (s != null) {
          setText(s);
          getStyleClass().add("channelItem");
          setFont(new Font(16));
        }
      }
    };
  }


  /**
   * Joins the game
   */
  private void joinGame() {
    requestUserList();
    createChatBox();
    logger.debug("Inside the joinGame method.");

  }

  /**
   * Creates the chat Box
   */
  private  void createChatBox() {
    textRecieved  = new TextFlow();
    textRecieved.getStyleClass().add("textbit");
    TextField messageToSend = new TextField();
    messageToSend.setFont(Font.font(16));
    HBox hbox = new HBox();
    Button button = new Button("Start");
    Button buttonTwo = new Button("Leave");
    hbox.getChildren().addAll(messageToSend,button,buttonTwo);
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.getStyleClass().add("scrollpane");
    scrollPane.setContent(textRecieved);
    scrollPane.setPrefSize(300,500);
    VBox secondVbox = new VBox();
    VBox.setMargin(messageToSend, new Insets(10, 0, 0, 0));
    // Set alignment to right
    secondVbox.setAlignment(Pos.CENTER);

    // Set padding to move it to the right side
    secondVbox.setPadding(new Insets(0, 10, 0, 0));

    secondVbox.getChildren().addAll(scrollPane,hbox);
    mainPane.setRight(secondVbox);
    messageToSend.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent) -> {
      if (Objects.requireNonNull(KeyEvent.getCode()) == ENTER) {
        sendToServer(messageToSend.getText());
        messageToSend.clear();
//        reuqestPieceTest();
      }});
    button.setOnAction(this::startMultiPlayerGame);
    buttonTwo.setOnAction((e) -> {
      gameWindow.getCommunicator().send("PART");
      secondVbox.getChildren().removeAll(scrollPane,hbox);
    });
  }

  /**
   * Sends the message to the server
   * @param text message to send
   */
  private void sendToServer(String text) {
//    if (text.startsWith("\n")) {
//      gameWindow.getCommunicator().send("NICK " + text.substring(2));
//
//    }
//    else {
      gameWindow.getCommunicator().send("MSG " + text);
    //}
  }


  /**
   * Sees if the name already exists in the list
   * @param name name of the channel to create
   * @return if the name already exists
   */
  private boolean inList(String name) {
    boolean nameAlreadyExists = false;
    outer : for (String string: channelList) {
             if (string.equals(name)) {
               nameAlreadyExists = true;
               break outer;
             }
    }
    return nameAlreadyExists;
  }


  /**
   * Starts the multiPlayerGame
   * @param event the actionEvent that triggered the start of multiplayer
   */
  private void startMultiPlayerGame(ActionEvent event) {
    gameWindow.startMultiPlayerGame();
    timer.cancel();
  }

  /**
   * Starts the multiplayer Game
   */
  private void startMultiPlayerGame() {
    gameWindow.startMultiPlayerGame();
    timer.cancel();
  }

  /**
   * Requests the userList
   */
  public void requestUserList() {
    gameWindow.getCommunicator().send("USERS");
  }
}

