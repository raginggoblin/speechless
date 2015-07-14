package raging.goblin.speechless.speech;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.Pair;
import marytts.util.data.audio.AudioPlayer;

import org.apache.log4j.Logger;

import raging.goblin.speechless.SpeechLessProperties;

public class Speeker {

   private static final Logger LOG = Logger.getLogger(Speeker.class);
   private static final SpeechLessProperties PROPERTIES = SpeechLessProperties.getInstance();

   private BlockingQueue<Pair<AudioPlayer, Integer>> speechesQueue = new ArrayBlockingQueue<>(100);
   private Pair<AudioPlayer, Integer> currentlySpeeking;
   private Set<EndOfSpeechListener> endOfSpeechListeners = new HashSet<>();
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
      speechesQueue.clear();
      if (currentlySpeeking != null) {
         currentlySpeeking.getFirst().cancel();
      }
   }

   public void speek(List<String> speeches) {
      new Thread("Offering speeches thread") {

         @Override
         public void run() {
            int speechId = -1;
            for (String speech : speeches) {
               if (!speech.trim().isEmpty()) {
                  try {
                     AudioInputStream audio = marytts.generateAudio(speech);
                     AudioPlayer player = new AudioPlayer(audio);
                     speechesQueue.offer(new Pair<>(player, ++speechId));
                     LOG.debug("Preparing to speek: " + speech);
                  } catch (SynthesisException e) {
                     LOG.error("Unable to speek", e);
                  }
               }
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
                  currentlySpeeking = speechesQueue.take();
                  currentlySpeeking.getFirst().start();
                  currentlySpeeking.getFirst().join();
                  notifyEndOfSpeechListeners(currentlySpeeking.getSecond());
               } catch (InterruptedException e) {
                  LOG.error("Interrupted speeking thread", e);
               }
            }
         }
      };
      running = true;
      dequeueThread.start();
   }

   public void notifyEndOfSpeechListeners(int speechId) {
      endOfSpeechListeners.stream().forEach(l -> l.endOfSpeech(speechId));
   }

   public void addEndOfSpeechListener(EndOfSpeechListener listener) {
      endOfSpeechListeners.add(listener);
   }

   public void removeEndOfSpeechListener(EndOfSpeechListener listener) {
      endOfSpeechListeners.remove(listener);
   }

   public interface EndOfSpeechListener {

      void endOfSpeech(int speechId);
   }
}
