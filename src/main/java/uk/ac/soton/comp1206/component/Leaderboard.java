package uk.ac.soton.comp1206.component;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.util.Pair;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.List;


/**
 * The LeaderBoard class extends the ScoreList class and is used to display A live LeaderBoard
 * in the MultiPlayerScene of all the people in the MultiPlayer Game.
 */
public class Leaderboard extends ScoresList {


  /**
   * This constructor is used to initialise an instance of Leaderboard.
   * @param scores scores An ObservableList containing pairs of player names and their corresponding scores.
   */
  public Leaderboard(ObservableList<Pair<String, Integer>> scores) {
    super(scores);
  }


//  public void getOnlineScores(Communicator communicator)  {
//    communicator.send("SCORES");
//    communicator.addListener((message) -> Platform.runLater(() -> this.loadOnlineScoresFromServer(message)));
//  }

//  private void loadOnlineScoresFromServer(String message) {
//    var messageObtaind = message.split(" ");
//    if(messageObtaind[0].equals("SCORES")) {
//      String[] scoreData = messageObtaind[1].split("\n");
//
//      ObservableList<Pair<String, Integer>> scores = getScore();
//
//      // Clear existing scores
//      scores.clear();
//
//
//      for (String data : scoreData) {
//        String[] parts = data.split(":");
//        String playerName = parts[0];
//        int playerScore = Integer.parseInt(parts[1]);
//        if(parts[2].equals("DEAD")) {
//          playerName = parts[0] + " ELIMINATED";
//        } else {
//          int numLives = Integer.parseInt(parts[2]);
//        }
//        var pair =  new Pair<>(playerName, playerScore);
//        scores.add(pair);
//      }
//    }
//
//  }
//
//  public void AddToLeadBoard (List<Pair<String,Integer>> add){
//    ObservableList<Pair<String, Integer>> scores = getScore();
//
//    // Clear existing scores
//    scores.clear();
//    score.addAll(add);
//  }


  /**
   * Clears the list.
   */
  public void clear() {
    score.clear();
  }

  public void setCellFactory() {
  }

//    public void makeTextBiggerAndBold() {
//      setCellFactory(listView -> new ListCell<Pair<String, Integer>>() {
//        @Override
//        protected void updateItem(Pair<String, Integer> item, boolean empty) {
//          super.updateItem(item, empty);
//          if (empty || item == null) {
//            setText(null);
//          } else {
//            setText(item.getKey() + ": " + item.getValue());
//            setStyle("-fx-font-weight: bold; -fx-font-size: 16px;"); // Adjust font size and style as needed
//          }
//        }
//      });
//    }
  }
