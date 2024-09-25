package uk.ac.soton.comp1206.component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;


/**
 * The PieceBoard class is used to display the Current and the FollowingPiece
 * int eh Game class.
 */
public class PieceBoard extends GameBoard {

  /**
   * The Logger instance is used to make log statements.
   */
  private static final Logger logger = LogManager.getLogger(PieceBoard.class);

  public PieceBoard(int cols, int rows, double width, double height,Boolean canHover) {
    super(cols, rows, width, height, canHover);
  }


  /**
   * The pieceToDisplay method is used to print the piece on the pieceBoard
   * @param piece the piece to display
   */
  public void pieceToDisplay(GamePiece piece) {
    if(piece == null) return;
    logger.info("Piece to Display: {}", piece);
      var blocks = piece.getBlocks();
      //printPattern(piece);
      for (int i = 0; i < blocks.length; i++) {
        for (int j = 0; j < blocks[0].length; j++) {
          if (blocks[i][j] >= 1) {
            this.grid.set(i, j, piece.getValue());
            if (i == 1 && j == 1) {
              this.getBlock(1, 1).addCircle();
            }
          } else {
            this.grid.set(i, j, 0);
          }
        }
      }
  }


//  public  void printPattern(GamePiece piece) {
//        var blocks = piece.getBlocks();
//        for (int i = 0; i < blocks.length; i++) {
//          for (int j = 0; j < blocks.length; j++) {
//            if (blocks[i][j] == 1)
//              System.out.print("1");
//            else
//              System.out.print("0");
//          }
//          System.out.println();
//        }
//      }
}

