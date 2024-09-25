package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Media.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

import static javafx.scene.input.KeyCode.ESCAPE;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    /**
     * Used to log statements in the terminal
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        Image TetresECS = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
        ImageView imageView = new ImageView();
        imageView.setImage(TetresECS);
        imageView.setFitWidth(500);
        imageView.setFitHeight(120);
        VBox tetresEcs = new VBox();
        tetresEcs.getChildren().add(imageView);
        tetresEcs.setAlignment(Pos.CENTER);
        mainPane.setCenter(tetresEcs);

        var singlePlayerButton = new Button("Single player");
        singlePlayerButton.getStyleClass().add("title");
        var multiPlayer = new Button("Multi Player");
        multiPlayer.getStyleClass().add("title");
        var howToPlay = new Button("How to Play");
        howToPlay.getStyleClass().add("title");
        var exitButton = new Button("Exit");
        exitButton.getStyleClass().add("title");

        VBox buttonsVBox = new VBox();
        buttonsVBox.getChildren().addAll(
                singlePlayerButton,multiPlayer,howToPlay,exitButton
        );
        buttonsVBox.setAlignment(Pos.BOTTOM_CENTER);

        mainPane.setBottom(buttonsVBox);
        animateLogo(imageView);
        if(root != null) {logger.debug("root is null");}

        //Bind the button action to the startGame method in the menu
        singlePlayerButton.setOnAction(this::startGame);

        //Bind the button to go to instructions
        howToPlay.setOnAction(this::openInstructions);
        exitButton.setOnAction(this::exit);
        multiPlayer.setOnAction(this::openMultiPlayer);
    }

    private void exit(ActionEvent actionEvent) {
        if(alertMessage() == ButtonType.OK) {
            logger.info("Shutting Down");
            System.exit(0);
        }
    }


    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        scene = gameWindow.getScene();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);

    }

    /**
     * This classes the game when escape key is pressed
     * @param keyEvent the keyEvent that occurred
     */
    private void keyPressed(KeyEvent keyEvent) {
        logger.info("Key pressed: " + keyEvent.getCode());
        if (Objects.requireNonNull(keyEvent.getCode()) == ESCAPE) {
            if(alertMessage() == ButtonType.OK) {
                logger.info("Escape key pressed");
                logger.info("Shutting Down");
                System.exit(0);
            }
        }
    }

    /**
     * Displays the alert message on the screen after pressing to make
     * sure the user want to leave after pressing escape.
     * @return returns the showAndWait alert message
     */
    public ButtonType alertMessage(){
        Alert alertMessage = new Alert(Alert.AlertType.CONFIRMATION);
        alertMessage.setTitle("Exit the Game");
        alertMessage.setContentText("Are you sure u want to exit the application");
        return alertMessage.showAndWait().get();
    }

    /**
     * animates the secretes logo
     * @param imageView the imageView to animate
     */
    public static void animateLogo(ImageView imageView) {
        FadeTransition fade = new FadeTransition();
        fade.setNode(imageView);
        fade.setDuration(Duration.millis(4000));
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        DoubleProperty c1 = imageView.translateYProperty();
        DoubleProperty c1Initial = new SimpleDoubleProperty();
        c1Initial.bind(c1);
        KeyValue keyValueStart = new KeyValue(c1, 0);
        KeyValue keyValueEnd = new KeyValue(c1, 100);
        KeyFrame keyFrameOne = new KeyFrame(Duration.ZERO, keyValueStart);
        KeyFrame keyFrameTwo = new KeyFrame(Duration.seconds(3), keyValueEnd);

        Timeline timeline = new Timeline(keyFrameOne, keyFrameTwo);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();
    }
    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    /**
     * Handel the open instructions button
     * @param event event
     */
    private void openInstructions(ActionEvent event) {
        gameWindow.startInstructions();
    }

    /**
     * handles the open multiplayer button
     * @param event event
     */
    private void openMultiPlayer(ActionEvent event) {
        gameWindow.startMultiPlayer();
    }


}
