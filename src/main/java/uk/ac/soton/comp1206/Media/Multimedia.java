package uk.ac.soton.comp1206.Media;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.MenuScene;

import java.io.File;
import java.net.URI;

/**
 * The Multimedia class is used to create objects that can be used to play audio or background music through the game.
 */
public class Multimedia {

  /**
   * used to log information in the terminal
   */
  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * The MediaPlayer that is used to play audio
   */
  private static MediaPlayer audioPlayer;
  /**
   * MediaPlayer instance used to play music.
   */
  private static MediaPlayer musicPlayer;

  /**
   * Boolean to verify is the sound is enabled or not
   */
  private static boolean soundEnabled = true;


  /**
   * Plays audio wherever necessary
   * @param file the name of the audio to play
   */
  public static void playAudio(String file) {
    if(!soundEnabled) return;
    try {
      File audioFile = new File("/Users/apoorv/Desktop/Programming2CourseWork/coursework/src/main/resources/sounds/" + file);
      URI audioFileURI = audioFile.toURI();
      logger.debug(audioFile);
      if (audioFileURI != null) {
        var toPlay = audioFileURI.toString();
        var media  = new Media(toPlay);
        audioPlayer = new MediaPlayer(media);
        audioPlayer.play();
      } else {
        logger.debug("The URI is null");
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.debug("Could not play the audio");
       soundEnabled = false;
    }
  }

  /**
   * plays the background music
   * @param file the name of the background music to play
   */
  public static void backgroundMusicPlayer (String file) {

    File audioFile = new File(
    "/Users/apoorv/Desktop/Programming2CourseWork/coursework/src/main/resources/music/" + file);
    URI audioFileURI = audioFile.toURI();
    logger.debug(audioFile);
    if (audioFileURI != null) {
      var toPlay = audioFileURI.toString();
      var media  = new Media(toPlay);
      musicPlayer = new MediaPlayer(media);
      musicPlayer.setOnEndOfMedia(() -> {
        musicPlayer.seek(Duration.ZERO);
      });
      musicPlayer.play();

    } else {
      logger.debug("The URI is null");
    }
  }

  /**
   * Stops all the background music
   */
  public  static void stopBackgroundMusic(){
    if(musicPlayer != null) {
      musicPlayer.stop();
    }
    if(audioPlayer != null) {
      audioPlayer.stop();
    }
  }

}
