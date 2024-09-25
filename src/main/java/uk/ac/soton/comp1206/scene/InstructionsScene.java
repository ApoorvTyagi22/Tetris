package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

import static javafx.scene.input.KeyCode.ESCAPE;

/**
 * THis class is responsible for displaying the instructions and also displaying all the playable
 * pieces dynamically.
 */
public class InstructionsScene extends BaseScene{


  /**
   * Used to print log statements in the terminal
   */
  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  /**
   * initialise the method
   */
  @Override
  public void initialise() {
    scene = gameWindow.getScene();
    scene.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
  }

  /**
   * Builds the scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
     root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());


    var pane = new StackPane();
    pane.setMaxWidth(gameWindow.getWidth());
    pane.setMaxHeight(gameWindow.getHeight());
    pane.getStyleClass().add("instructions-background");

    root.getChildren().add(pane);
    Image instructionsImage = new Image(
            getClass().getResource("/images/Instructions.png").toExternalForm()
    );
    ImageView imageView = new ImageView();
    imageView.setImage(instructionsImage);
    imageView.setFitWidth(500);
    imageView.setFitHeight(300);
    VBox vBox = new VBox();
    vBox.getChildren().add(imageView);
    vBox.setAlignment(Pos.TOP_CENTER);

    var mainPane = new BorderPane();
    pane.getChildren().add(mainPane);
    mainPane.setCenter(vBox);

    GridPane gridPane = new GridPane();
    gridPane.addRow(3);
    gridPane.addColumn(5);
    gridPane.setAlignment(Pos.BOTTOM_CENTER);
    gridPane.setHgap(20);
    gridPane.setVgap(5);

    int numToplay = 0;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 5; j++) {
        PieceBoard pieceBoard = new PieceBoard(3, 3, 70, 70,false);
        gridPane.add(pieceBoard, i, j);
        pieceBoard.pieceToDisplay(GamePiece.createPiece(numToplay));
        numToplay++;
      }

    }
      mainPane.setBottom(gridPane);

  }

  /**
   * handles the key pressed
   * @param keyEvent the keyEvent that has occurred.
   */
    private void keyPressed(KeyEvent keyEvent) {
      logger.info("Key pressed: " + keyEvent.getCode());
      if (Objects.requireNonNull(keyEvent.getCode()) == ESCAPE) {
        logger.info("Escape key pressed");
        gameWindow.startMenu();
      }
    }

}
