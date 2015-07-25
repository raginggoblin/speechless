/*
 * Copyright 2015, RagingGoblin <http://raginggoblin.wordpress.com>
 * 
 * This file is part of SpeechLess.
 *
 *  SpeechLess is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SpeechLess is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SpeechLess.  If not, see <http://www.gnu.org/licenses/>.
 */

package raging.goblin.speechless.speech;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.Pair;
import marytts.util.data.audio.AudioPlayer;

import org.apache.log4j.Logger;

import raging.goblin.speechless.Messages;
import raging.goblin.speechless.SpeechLessProperties;
import raging.goblin.speechless.ui.ToastWindow;

public class Speeker {

   private static final Logger LOG = Logger.getLogger(Speeker.class);
   private static final SpeechLessProperties PROPERTIES = SpeechLessProperties.getInstance();
   private static final Messages MESSAGES = Messages.getInstance();

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
      new Thread("Offering speeches") {

         @Override
         public void run() {
            int speechId = -1;
            for (String speech : speeches) {
               if (!speech.trim().isEmpty()) {
                  speechId++;
                  try {
                     AudioInputStream audio = marytts.generateAudio(speech.toLowerCase());
                     AudioPlayer player = new AudioPlayer(audio);
                     speechesQueue.offer(new Pair<>(player, speechId));
                     LOG.debug("Preparing to speek: " + speech);
                  } catch (SynthesisException e) {
                     LOG.error("Unable to speek: " + speech, e);
                  }
               }
            }
         };
      }.start();
   }

   public void save(String speech, File file) {
      new Thread("Saving speeches") {

         @Override
         public void run() {
            if (!speech.trim().isEmpty()) {
               try {
                  ToastWindow toast = ToastWindow.showToast(MESSAGES.get("saving"), false);
                  AudioInputStream audio = marytts.generateAudio(speech.toLowerCase());
                  AudioSystem.write(audio, AudioFileFormat.Type.WAVE, file);
                  toast.setVisible(false);
                  toast.dispose();
                  ToastWindow.showToast(MESSAGES.get("ready_saving"), true);
               } catch (SynthesisException | IOException e) {
                  LOG.error("Unable to save speech", e);
               }
            }
         }
      }.start();
   }

   public void initMaryTTS() throws MaryConfigurationException {
      marytts = new LocalMaryInterface();
      marytts.setVoice(Voice.getSelectedVoice().getName());
      marytts.setAudioEffects(SoundEffect.toMaryTTSString());
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
