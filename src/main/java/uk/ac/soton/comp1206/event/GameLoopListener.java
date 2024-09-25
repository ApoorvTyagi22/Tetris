package uk.ac.soton.comp1206.event;

import java.util.Timer;

/**
 * The GameLoopListener is used for listening to notify when the game loop progresses.
 */
public interface GameLoopListener {

  /**
   * Called when a game loop event occurs.
   */
  public void gameLoop();
}
