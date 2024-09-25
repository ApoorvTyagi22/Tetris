package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {


    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        if(x < cols && y < rows) {
            grid[x][y].set(value);
        }else {
            logger.info("cant Place the piece there");
        }
    }


    /**
     * This verifies if a piece can be placed in the grid.
     * @param piece  This is the piece to be played
     * @param x the x placement on the grid
     * @param y the y placement on the grid
     * @return if it can be placed
     */
    public boolean canPlayPiece(GamePiece piece, int x, int y) {
        logger.info("Checking if we can play the piece {} at {}. {}", piece,x,y);

        int topX = x - 1;
        int topY = y - 1;

        int [][] blocks = piece.getBlocks();
            for (var blockX = 0; blockX < blocks.length; blockX++) {
                for (var blockY = 0; blockY < blocks[0].length; blockY++) {
                    var blockvalue = blocks[blockX][blockY];
                    if(blockvalue > 0) {
                        //Check if we can place this block on the grid
                        var gridValue =  get(topX + blockX, topY + blockY);
//                        logger.debug("the Rows {}, the coloums{} {}, {}", blockX,blockY,blockvalue,gridValue);
                        if(gridValue != 0) {
                            logger.info("Unable to place piece confilict at {},{}"
                                                    , topX + blockX, topY + blockY);
                            return false;
                        }
                    }
                }
            }
            return true;
    }

    /**
     * This places a piece by updating the piece with the piece blocks
     * @param piece the piece to be placed
     * @param x the x placement
     * @param y the y placement
     */
    public void playPiece(GamePiece piece, int x, int y) {

        logger.info("playing the piece {} at {}. {}", piece,x,y);
        int topX = x - 1;
        int topY = y - 1;

        int value = piece.getValue();
         int [][] blocks = piece.getBlocks();
         if (canPlayPiece(piece,x,y)) {
            for (var blockX = 0; blockX < blocks.length; blockX++) {
                for (var blockY = 0; blockY < blocks[0].length; blockY++) {
                    var blockValue = blocks[blockX][blockY];
                    if(blockValue > 0) {
                        //BlockX and BlockY corrdinate
                        set(topX + blockX, topY + blockY, value);
                    }
                }
            }
        }
        //return if we cant play the piece (Same as  if(!canPlayPiece(piece,x,y)))
        else return;

    }
    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

}
