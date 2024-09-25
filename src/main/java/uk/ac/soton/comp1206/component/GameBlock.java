package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    /**
     * Logger class is used to produce log Statements which would help in
     * the development process.
     */
    private static final Logger logger = LogManager.getLogger(GameBlock.class);


    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT, // 0
            Color.DEEPPINK, // 1
            Color.RED, // 2
            Color.ORANGE, // 3
            Color.YELLOW, // 4
            Color.YELLOWGREEN, // 5
            Color.LIME, // 6
            Color.GREEN, // 7
            Color.DARKGREEN, // 8
            Color.DARKTURQUOISE, // 9
            Color.DEEPSKYBLUE, // 10
            Color.AQUA, // 11
            Color.AQUAMARINE, // 12
            Color.BLUE, // 13
            Color.MEDIUMPURPLE, // 14
            Color.PURPLE, // 15
            Color.rgb(128, 128, 128,0.1),// 16
            Color.rgb(128, 128, 128,0.1),// 17

    };

    /**
     * The highlightColor is used to highlight block when using the
     * Keyboard, or when hovering with the mouse.
     */
    private static final Paint HIGHLIGHT_COLOR = Color.DIMGREY;

    /**
     * The board of this block
     */
    private final GameBoard gameBoard;

    /**
     * The width of the block
     */
    private final double width;

    /**
     * The height of the block
     */
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();
        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
        setOnMouseEntered(this::mouseEntered);
        setOnMouseClicked(this::mouseExited);
    }


    /**
     * The fadeOut method is used to create a flash and a fadeout effect,
     * and is called when the blocks are cleared.
     */
    public void fadeOut() {
        long fadeStartTime = System.nanoTime();
       // logger.info("fade Start Time: {} ", fadeStartTime);

        AnimationTimer fadeOutTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsedTime = (now - fadeStartTime) / 1_000_000.0;
                // Convert nanoseconds to milliseconds
                if (elapsedTime >= 1000.0) {
                    stop();
                    paintEmpty();
                } else {
                    // Calculate opacity based on elapsed time
                    double opacity = 1.0 - (elapsedTime / 1000.0);
//                    logger.debug("opacity: {}", opacity);
                    paintColor(Color.rgb(255, 255, 100, opacity));
                }
            }
        };
        fadeOutTimer.start();
    }

    /**
     * The Highlight method is called on the block calling the method,
     * and it highlights the block.
     */
    public void highlight() {
            var gc = getGraphicsContext2D();
            gc.setFill(HIGHLIGHT_COLOR);
            gc.fillRect(0, 0, getWidth(), getHeight());

    }

    /**
     * clearHighlight removes any highlight on the block
     * by calling the paint and painting it based on its value.
     */
    public void clearHighlight() {
            paint();

    }

    /**
     *This mouseExisted method is called when the mouse leaves the block
     * and removes the highlight
     * @param mouseEvent this object contains info about the mouseEvent that occurred.
     */
    private void mouseExited(MouseEvent mouseEvent) {
        gameBoard.removeHighlight();
    }

    /**
     * This mouseEntered Method is called when the mouse enters the block,
     * ie hovers over it.
     * @param mouseEvent this object contains info about the mouseEvent that occurred
     */
    private void mouseEntered(MouseEvent mouseEvent) {
        gameBoard.highlightBlock(this);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * This method is used to add a circle in the center block of the currentPiece
     * display for reference.
     */
    public void addCircle() {
        var gc = getGraphicsContext2D();

        double centerX = width / 2;
        double centerY = height / 2;
        double radius = Math.min(width, height) / 4;

        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);

    }


    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.rgb(0, 0, 0, 0.4)); // Black with 50% opacity
        gc.fillRect(0, 0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        Color darkerColor = ((Color)colour).darker();
        Color lighterColor = ((Color)colour).brighter().brighter().brighter().brighter().brighter();

        LinearGradient gradient = new LinearGradient(0, 0, 0, height, false, CycleMethod.NO_CYCLE,
                new Stop(0, darkerColor),
                new Stop(0.5, (Color) colour),
                new Stop(1, lighterColor));

        gc.setFill(gradient);

        //Colour fill
//        gc.setFill(colour);

        gc.fillRect(0,0, width, height);

        Glow glow = new Glow();
        glow.setLevel(0.5);
        gc.applyEffect(glow);


        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }


    /**
     * returns a string representation of the block with necessary information.
     * @return Provide a string representation of the gameBlock
     */
    @Override
    public String toString() {
        return "GameBlock{" +
                "x=" + x +
                ", y=" + y +
                ", value=" + value.get()+
                '}';
    }
}