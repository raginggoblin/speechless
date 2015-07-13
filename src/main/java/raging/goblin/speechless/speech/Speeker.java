package raging.goblin.speechless.speech;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioInputStream;

import org.apache.log4j.Logger;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import raging.goblin.speechless.SpeechLessProperties;

public class Speeker {

   private static final Logger LOG = Logger.getLogger(Speeker.class);
   private static final SpeechLessProperties PROPERTIES = SpeechLessProperties.getInstance();

   private BlockingQueue<AudioPlayer> speechesQueue = new ArrayBlockingQueue<>(100);
   private volatile boolean running = false;
   private MaryInterface marytts;

   public Speeker() throws MaryConfigurationException {
      initMaryTTS();
      initSpeeking();
   }

   public void stop() {
      running = false;
   }

   public void stopSpeeking() {
      for (AudioPlayer audioPlayer : speechesQueue) {
         audioPlayer.cancel();
      }
      speechesQueue.clear();
   }

   public void speek(String toSpeek) {
      new Thread("Offering speeches thread") {

         @Override
         public void run() {
            try {
               AudioInputStream audio = marytts.generateAudio(toSpeek);
               AudioPlayer player = new AudioPlayer(audio);
               speechesQueue.offer(player);
            } catch (SynthesisException e) {
               LOG.error("Unable to speek", e);
            }
         };
      }.start();
   }

   private void initMaryTTS() throws MaryConfigurationException {
      marytts = new LocalMaryInterface();
      marytts.setVoice(PROPERTIES.getVoice());
   }

   private void initSpeeking() {
      Thread dequeueThread = new Thread("Speeking thread") {

         @Override
         public void run() {
            while (running) {
               try {
                  AudioPlayer speech = speechesQueue.take();
                  speech.start();
                  speech.join();
               } catch (InterruptedException e) {
                  LOG.error("Interrupted speeking thread", e);
               }
            }
         }
      };
      running = true;
      dequeueThread.start();
   }
}
