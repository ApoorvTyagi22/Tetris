package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickListener;
import uk.ac.soton.comp1206.game.Grid;

import java.util.HashSet;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);
    private final Boolean canHover;

    private GameBlock highlightedBlock;


    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
     GameBlock[][] blocks;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;
    private RightClickListener rightClickListener;


    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height,Boolean hoverON) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;
        this.canHover = hoverON;

        //Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height, Boolean hoverON) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols,rows);
        this.canHover = hoverON;


        //Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}",cols,rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));
        
        this.setOnMouseClicked(this::boardRightClick);

        this.setOnRightClicked(this::rotateCurrentPiece);

        return block;
    }


    /**
     * This method is called when the user right-clicks on the Board,
     * and this in turn rotates the piece.
     * @param event contains information about the mouseEvent that .
     */
    private void boardRightClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY && rightClickListener != null) {
            rightClickListener.rotatePiece();
        }
    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }


    /**
     * Set the listener to handel an event when a block is rightClicked
     * @param listener listener to add
     */
    public void setOnRightClicked(RightClickListener listener) {
        this.rightClickListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);

        if(event.getButton() == MouseButton.PRIMARY &&blockClickedListener != null) {
            blockClickedListener.blockClicked(block);
        }
    }

    /**
     * Returns the GameBlocks on the current Board
     * @return the GameBlock 2D array is returned.
     */
    public GameBlock[][] getBlocks() {
        return blocks;
    }

    /**
     * Rotates the current piece on display on PieceBoard display.
     */
    public void rotateCurrentPiece() {
        this.setOnRightClicked(null);
        logger.info("GameBoard Right Clicked");
        if(rightClickListener != null) {
            rightClickListener.rotatePiece();
        }
        this.setOnRightClicked(this::rotateCurrentPiece);
    }

    /**
     * Highlights the block on the board by calling the
     * highlight method of the GameBlock class.
     * @param block the block on which the highlight effect is to be called.
     */
    public void highlightBlock(GameBlock block) {
        removeHighlight();
        if (block != null && canHover) {
            highlightedBlock = block;
            block.highlight();
        }

    }

    /**
     * Removes the highlight of the block.
     */
    public void removeHighlight() {
        if (highlightedBlock != null) {
            highlightedBlock.clearHighlight();
            highlightedBlock = null;
        }

    }

    /**
     * Created a fadeOut effect on a Hashset of GameBlocks, when a line is cleared
     * @param coordinateHashSet the coordinates of the block on which the fadeout animation
     *                          should be called
     */
    public void fadeOut(HashSet<GameBlockCoordinate> coordinateHashSet) {
        for(GameBlockCoordinate blockCoordinate : coordinateHashSet) {
            blocks[blockCoordinate.getX()][blockCoordinate.getY()].fadeOut();
        }
    }
}
