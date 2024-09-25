package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;


/**
 * The NextPieceListener is used to notify when the next piece is available to display
 */
public interface NextPieceListener {


  /**
   * used when next pieces are ready to be displayed
   * @param piece the current piece to display
   * @param secondPiece the following piece to display
   */
  public void nextPiece(GamePiece piece, GamePiece secondPiece);
}
