package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Media.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.*;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCode.D;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game  {

    /**
     * used to log progress on terminal
     */
    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Used to generate random numbers
     */
    private final Random random = new Random();

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /***
     * CurrentPiece to be played.
     */
    protected static GamePiece currentPiece;


    /**
     * The followingPiece to be displayed
     */
    protected static GamePiece followingPiece;

    /**
     *the instance of NextPieceListener listens for when the next Piece is ready.
     */
    protected NextPieceListener nextPieceListener;

    /**
     * Listener to notify when lines have been cleared.
     */
    private LineClearedListener lineClearedListener;


    /***
     * Users current score
     */
    private static SimpleIntegerProperty score;


    /***
     * level user is on
     */
    private static SimpleIntegerProperty level;


    /***
     * Current lives of the user
     */
    private static SimpleIntegerProperty lives;

    /***
     * Current multiplier
     */
    private static SimpleIntegerProperty multiplier;
    /**
     * boolean to check the end
     */
    protected boolean end = false;

    /**
     * GameLoop listener to listen for gameLoop
     */
    private GameLoopListener gameLoopListner;

    /**
     * The timer instance is used to initialize a timerTask
     */
    protected Timer timer;

    /**
     * Boolean to verify if the next piece has been played
     */
    protected boolean blockPlayed = false;


    /**
     * Skip the current piece
     */
    private static SimpleIntegerProperty skips;


    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        score = new SimpleIntegerProperty(0);
        level = new SimpleIntegerProperty(0);
        lives = new SimpleIntegerProperty(3);
        multiplier = new SimpleIntegerProperty(1);
        skips = new SimpleIntegerProperty(3);


        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }


    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        startTimer();
    }

    /**
     * Method to swap the current and the following piece
     */
    public void swapCurrentPiece() {
        var tempGamePiece = followingPiece;
        followingPiece = currentPiece;
        currentPiece = (tempGamePiece);
        Multimedia.playAudio("pling.wav");
        callToListener();
    }


    /**
     * Call to the listener to update the piece to display in the scene
     */
    public void callToListener() {
        if(nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
    }

    /**
     * method to rotate the current Piece
     */
    public void rotateCurrentPiece()  {
         currentPiece.rotate(1);
        Multimedia.playAudio("rotate.wav");
        callToListener();
    }


//    public  void printPattern(GamePiece piece) {
//            for (int[] row :piece.getBlocks()) {
//                for (int element : row) {
//                    System.out.print(element + " ");
//                }
//                System.out.println(); // Move to the next line after printing each row
//            }
//
//    }

    /**
     *  Sets the listener to notify for when the next piece is ready
     * @param listener The nextPieceListener to be set as a listener
     */
    public void setNextPieceListener (NextPieceListener listener) {
        logger.debug("2: Set as the listener {}", listener);
        this.nextPieceListener = listener;
    }
    /***
     * This method calls the next piece
     */
    public void nextPiece() {
        currentPiece = (followingPiece); // Update currentPiece with nextPiece
        followingPiece = spawmPiece(); // Generate a new nextPiece
        logger.info("next Piece is : {}", currentPiece);
        logger.info("Next to Next Piece is: {}", followingPiece);
        if(nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
    }
    /***
     * This method implements the action for what should happen after a piece is played
     */
    public int[] afterPiece() {
        HashSet<GameBlockCoordinate> coordinateHashSet = new HashSet<>();
        int[] data = new int[2];
        int linesToClear = 0;
        linesToClear += extracted1(coordinateHashSet);

        linesToClear += extracted(coordinateHashSet);

        data[0] = linesToClear;
        data[1] = coordinateHashSet.size();
        clearRows(coordinateHashSet, data[0]);

        return data;
    }

    /***
     * This method checks for any horizontal lines to clear
     * @param coordinateHashSet contains all the blocks to clear
     */
    private int extracted1(HashSet<GameBlockCoordinate> coordinateHashSet) {
        int numLinesToClear = 0;
        outer: for(int rows = 0; rows < grid.getRows(); rows++) {
            ArrayList<IntegerProperty> lineProperties = new ArrayList<>();
             for(int cols = 0; cols < grid.getCols(); cols++) {
                int val = grid.get(cols,rows);
                if(val == 0 || val == -1) {
                    continue outer;
                } else {
                    lineProperties.add(grid.getGridProperty(cols,rows));
                }
            }
            if(lineProperties.size() == 5) {
                numLinesToClear++;
                for (int col = 0; col < grid.getCols(); col++)
                    coordinateHashSet.add(new GameBlockCoordinate(col,rows));

            }
        }
        return numLinesToClear;
    }

    /***
     * The method checks vertical lines to clear
     * @param coordinateHashSet contains all the blocks to clear
     */
    private int extracted(HashSet<GameBlockCoordinate> coordinateHashSet) {
        int numLinesToClear = 0;
        outer: for (int cols = 0; cols< grid.getCols(); cols++) {
            ArrayList<IntegerProperty> lineProperties = new ArrayList<>();
             for (int row = 0; row < grid.getRows(); row++){
                int val = grid.get(cols,row);
                if(val == 0 || val == -1) {
                    continue outer;
                  } else {
                    lineProperties.add(grid.getGridProperty(cols,row));
                 }
              }
            if(lineProperties.size() == 5) {
                numLinesToClear++;
                for (int row = 0; row < grid.getRows(); row++)
                    coordinateHashSet.add(new GameBlockCoordinate(cols,row));
             }
            }
            return numLinesToClear;
    }


    /**
     * Sets the listener to notify for when Lines are cleared from the game grid
     * @param lineClearedListener
     */
    public void setLinesClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * This method clears all the blocks
     * @param coordinateHashSet contains all the blocks to clear
     */
    private void clearRows(HashSet<GameBlockCoordinate> coordinateHashSet, int numLinesToClear) {
        if(lineClearedListener != null) {
            lineClearedListener.linesCleared(coordinateHashSet);
        }
        if(!(numLinesToClear > 0)) return;
        Iterator<GameBlockCoordinate> iterator = coordinateHashSet.iterator();
        while (iterator.hasNext()){
            GameBlockCoordinate gameBlockCoordinate = iterator.next();
            grid.set(gameBlockCoordinate.getX(), gameBlockCoordinate.getY(),0);
            iterator.remove();
        }
            Multimedia.playAudio("clear.wav");

    }


    /**
     * Picks a random piece from the GamePiece class to play
     * @return the piece that is to be played
     */
    public GamePiece spawmPiece() {

        var maxPieces = GamePiece.PIECES;
        var randomNumForPiece = random.nextInt(maxPieces);
        logger.info("Picking a random piece: {}", randomNumForPiece);
        var randomPiece = GamePiece.createPiece(randomNumForPiece);
        return randomPiece;
    }
    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        if(!end) {
            logger.info("Initialising game");
            Multimedia.backgroundMusicPlayer("game.wav");
            followingPiece = followingPiece();
            nextPiece();
        }
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public Boolean blockClicked(GameBlock gameBlock) {
        if(end) return false;
        logger.info("next Play");
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        int[] data;
        if(grid.canPlayPiece(currentPiece, x ,y)) return playingPiece(x, y);
        else {
            //Cant place the piece
            Multimedia.playAudio("fail.wav");
            return false;
        }
    }

    /**
     * This method executes the steps for what should happen if the block can be played
     * @param x x coordinate of where the block is to be played
     * @param y y coordinate of where the block is to be played
     * @return returns if the block has been played
     */
    protected boolean playingPiece(int x, int y) {
        int[] data;
        blockPlayed = true;
        timer.cancel();
        startTimer();
        //can place a piece
        grid.playPiece(currentPiece, x, y);
        Multimedia.playAudio("place.wav");
        nextPiece();
        data = afterPiece();
        score(data[0],data[1]);
        updateMultiplier(data[0]);
        updateLevel();
//            newTimer();
        return true;
    }

    /***
     * This method updates the score.
     */
    protected void updateLevel() {
        int points = getScore();
        int levelToSet  = points / 1000;
        Game.level.set(levelToSet);
    }

    /***
     *This method checks the conditions to change the multiplier and does it if necessary.
     * @param numLinesToClear number of lines to clear as of the current play.
     */
    protected void updateMultiplier(int numLinesToClear) {
        if(numLinesToClear > 0) {
            incrementMultiplier();
        }
        if(numLinesToClear == 0){
            resetMultiplier();
        }

    }

    /***
     * This calculates the score of the game after everyplay.
     * @param numberOfLines number of lines to clear
     * @param numberOfBlocks number of blocks to clear
     */
    public void score (int numberOfLines, int numberOfBlocks) {
        if(numberOfLines != 0) {
            int previousScore = getScore();
            int scoreToSet = numberOfLines * numberOfBlocks * 10 * multiplierProperty().get();
            setScore(scoreToSet + previousScore);
        }
     }

//    public void addToConcurrentLeadBoard() {
//    }


    /**
     * Generates a new piece which is later set as the following piece
     * @return the piece to set as the following piece
     */
    public GamePiece followingPiece() {
        GamePiece nextSquarePiece = spawmPiece();
        return nextSquarePiece;
    }
    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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

    /***
     *This method returns the level as an int
     * @return score
     */
    public static int getScore() {
        return score.get();
    }

    /**
     * This method sets the score as an int
     * @param score score to set
     */
    public static void setScore(int score) {
        Game.score.set(score);
    }

    /***
     * This method returns the score as a SimpleIntegerProperty.
     * @return score
     */
    public static SimpleIntegerProperty scoreProperty() {
        return score;
    }

    /***
     * This method returns the level as an int.
     * @return level.
     */
    public static int getLevel() {
        return level.get();
    }

    /***
     * This method returns the level as a SimpleIntegerProperty.
     * @return level
     */
    public static SimpleIntegerProperty levelProperty() {
        return level;
    }

    /***
     *This method returns the lives as an int.
     * @return lives
     */
    public static int getLives() {
        return lives.get();
    }

    /**
     * Set the lives
     * @param lives life to set
     */
    public static void setLives(int lives) {
        Game.lives.set(lives);
    }

    /**
     * This method decrements the current lives by 1 life
     */
    public static void decrementLife() {
            int currentLife = getLives();
            logger.debug("Current Life before decrease: {}", currentLife);
            int lifeToSet = currentLife - 1;
//logger.debug("Life to set: {}", lifeToSet);
            Game.setLives(lifeToSet);
            logger.debug("Current Life is: {}", getLives());
            Multimedia.playAudio("lifelose.wav");


    }
    /**
     * This method returns the lives as a SimpleIntegerProperty.
     * @return lives
     */
    public static SimpleIntegerProperty livesProperty() {
        return lives;
    }

    /**
     * This method returns the multiplier as int.
     * @return multiplier as an int.
     */
    public static int getMultiplier() {
        return multiplier.get();
    }

    /***
     * This method returns the multiplier as a SimpleIntegerProperty.
     * @return multiplier
     */
    public static SimpleIntegerProperty multiplierProperty() {
        return multiplier;
    }

    /**
     * This method is used to update the multiplier property.
     * @param multiplier the value to set.
     */
    public static void setMultiplier(int multiplier) {
        Game.multiplier.set(multiplier);
    }

    /**
     * increments the multiplier
     */
    public static void incrementMultiplier() {
        Game.multiplier.set(getMultiplier() + 1);
    }

    /**
     * sets the Multiplier to zero.
     */
    public static void resetMultiplier(){
        Game.multiplier.set(1);
    }

    /**
     * increments the level by one unit.
     */
    public static void incrementLevel(){
        Game.level.set(getLevel() + 1);
    }

    /**
     * This method shuts down the game
     */
    public void shutDown() {
        end = true;
        Multimedia.stopBackgroundMusic();
        if(timer != null) {
            timer.cancel();
        }
        logger.info("Shutting down the Game");
    }

    /**
     * Sets the listener for handling the gameLoop
     * @param listener The GameLoopListener to set as the listener
     */
    public void setOnGameLoop(GameLoopListener listener) {
        this.gameLoopListner = listener;
    }


    /**
     * Starts a new timer for the time Task
     */
    public void startTimer() {
        // Cancel the previous timer if it exists
        if (timer != null) {
            timer.cancel();
        }

        blockPlayed = false;
        // Create a new timer instance
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(!blockPlayed) {
                    gameLoop();
                }
            }
        };
        logger.info("New Timer started: {}", timer.toString());
        timer.scheduleAtFixedRate(task, getTimerDelay(),getTimerDelay());

    }

    /**
     * this method outlines what should happen if the block is not played in time
     */
    public void gameLoop() {
        blockPlayed = false;
        decrementLife();
        if(gameLoopListner != null) {
            gameLoopListner.gameLoop();
        }
        timerEndGamePieceUpdate();
        callToListener();
        resetMultiplier();
    }

    /**
     * updating the currentPiece and following piece as the block was not played in time
     */
    public void timerEndGamePieceUpdate() {
        currentPiece = (followingPiece);
        followingPiece = spawmPiece();
    }

    /**
     * calculates the time delay based on the provided formula
     * @return the timeDelay calculated
     */
    public long getTimerDelay() {
      long time =  Math.max(2500,12000 - 500 * getLevel());
        logger.debug("Current Play Time Delay: {}", time);
        return time;
    }

    /**
     * Calculates the boardValues and returns them, required for the multiplayer Game
     * @return each block value on the board
     */
    public String boardValues() {
        return null;
    }


    /**
     * Returns the leadBoard Scores, required for multiplayerGame mode
     * @return List with current scores from leaderBoard game
     */
    public List<Pair<String, Integer>> getLeaderBoardScores() {
        return null;
    }


    /**
     * Displays content in the specified ScrollPane, is overridden by multiplayerGame class
     * @param scrollPane ScrollPane in which the content is to be displayed.
     */
    public void displayInScrollPane(ScrollPane scrollPane) {
    return;
    }

    /**
     * used to return leaderBoard scores for scores scene
     * leaderBoard scores for multiplayer
     */
    public List<Pair<String, Integer>> leaderBoardForScoresScene() {
        return null;
    }

    /**
     * Skips the current Piece
     */
    public void useSkip() {
        if(skips.get() > 0) {
            Random random1 = new Random();
            currentPiece = followingPiece;
            followingPiece = GamePiece.createPiece(random1.nextInt(14));
            skips.set(skips.get()-1);
            if(nextPieceListener != null) {
                nextPieceListener.nextPiece(currentPiece, followingPiece);
            }
        } else {
            logger.info("No More Skips available");
        }
    }

    /**
     * returns the skip property
     * @return skip property
     */
    public static SimpleIntegerProperty skipProperty() {
        return skips;
    }
}
