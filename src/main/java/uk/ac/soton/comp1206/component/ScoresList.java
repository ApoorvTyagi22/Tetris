package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Pair;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * The scoreList class is used to display the scores in the scoresScene after a game ends.
 */
public class ScoresList extends ListView<Pair<String,Integer>> {

    /**
     * The list of Scores to display after the game ends.
     * Each score is represented as a Pair containing a player name and their corresponding HighScore.
     */
    ListProperty<Pair<String, Integer>> score;

    /**
     * the BufferReader instance is used to read from the fileReader.
     */
    private BufferedReader reader;

    /**
     * The FileReader instance is used to read from file.
     */
    private FileReader fileReader;

    /**
     * The file to read.
     */
    private File file;

    /**
     * This constructor is used to create an instance of the ScoreList object.
     * @param scores scores An ObservableList containing pairs of player names and their corresponding scores.
     */
    public ScoresList(ObservableList<Pair<String, Integer>> scores){
      this.score = new SimpleListProperty<>(scores);
      this.setItems(score);
      setStyle(".viewport");
    }


    /**
     * A getter that returns the ObservableList of scores
     * @return ObservableList of scores
     */
    public ObservableList<Pair<String, Integer>> getScore() {
      return score.get();
    }

    /**
     * Returns the ListProperty of score
     * @return ListProperty of score
     */
    public ListProperty<Pair<String, Integer>> scoreProperty() {
      return score;
    }


  /**
   * This method loads the score of the file and adds those scores to the observableValueList
   * @param game game instance from which we fetch the current score
   */
    public void loadScores(Game game)  {
      //Read Scores From A file
     if(!(game instanceof MultiplayerGame)) {
        try {
          file = new File("src" + File.separator + "localScores.txt");
          // Write default scores to the file
          if (!file.exists()) {
            // Write default scores to the file
            writeDefaultData(file);
          }
          arrangeTheList();
          fileReader = new FileReader(file);
          reader = new BufferedReader(fileReader);
          List<Pair<String, Integer>> localScores = new ArrayList<>();
          while (fileIsReady()) {
            var line = reader.readLine().split(":");
            Pair<String, Integer> pair = new Pair<>(line[0], Integer.parseInt(line[1]));
            localScores.add(pair);
          }
          score.addAll(localScores);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else {
       score.addAll(game.leaderBoardForScoresScene());
     }
  }

  /**
   * This method verifies if the file is ready to be read and
   * the reader is not null
   * @return returns true if read and false if not
   */
  private boolean fileIsReady() {
    if (reader != null) {
      try {
        return reader.ready();
      } catch (IOException exception) {
        System.out.println(exception);
        return false;
      }
    }
    return false;

  }
//TestingTwo

  /**
   * This method writes the scores to the file
   * @param scoresList score-list from which we require the scores
   */
  public void writeScores(ScoresList scoresList) {
    try {
      file  = new File("src" + File.separator + "localScores.txt");
      if (!file.exists()) {
        // Create a file and default scores to the file
        writeDefaultData(file);
      }
      Writer writer = new FileWriter(file);
      for (int i = 0; i < score.size() ; i++) {
        String string = (score.get(i).getKey() + ":" +  score.get(i).getValue());
//        FileManager.save(string, file);
        writer.write(string);
      if(i < score.size())
        writer.write("\n");
      }
      writer.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }


  /**
   * gets the reader ready to write default values to the file if empty
   * @param file file to write to
   */
  private void writeDefaultData(File file) { // Method Working Fine
    try {
      FileWriter fileWriter = new FileWriter(file);
      BufferedWriter writer = new BufferedWriter(fileWriter);
      for(int i = 1; i <= 10; i++) {
        writer.write("Default" + i + ":" + i *100);
        if(i < 10)
          writer.write("\n");
      }

      writer.close();
    }catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Arranges the list from highest to lowest based on scores
   */
  public void arrangeTheList() {
    Comparator<Pair<String, Integer>> comparator = Comparator.comparing
            (stringIntegerPair -> stringIntegerPair.getValue());
//    score.sort(comparator);
    score.sort(comparator.reversed());

  }

  /**
   * returns the value at an index
   * @param i index
   * @return the value at i index
   */
  public int get(int i) {
    return score.get(i).getValue();
  }

  /**
   * returns the size of the scoreList
   * @return size of the list
   */
  public int size() {
    return score.size();
  }


  /**
   * provides a string representation of the list
   * @return string representation of the list
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Remote Score List:\n");
    if (score != null) {
      for (Pair<String, Integer> pair : score) {
        sb.append(pair.getKey()).append(":").append(pair.getValue()).append("\n");
      }
    } else {
      sb.append("No scores available\n");
    }
    return sb.toString();
  }

}

