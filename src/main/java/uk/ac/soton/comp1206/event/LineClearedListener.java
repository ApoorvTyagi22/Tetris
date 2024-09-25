package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

/**
 * The LineClearedListener is used to listen when lines are cleared in the game grid.
 */
public interface LineClearedListener {

  /**
   * Used when multiple gameBlocks are cleared
   * @param coordinateHashSet contains coordinates of the blocks to clear
   */
  public void linesCleared(HashSet<GameBlockCoordinate> coordinateHashSet);

  }
