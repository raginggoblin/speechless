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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lombok.extern.slf4j.Slf4j;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import raging.goblin.speechless.Messages;
import raging.goblin.speechless.ui.ToastWindow;

@Slf4j
public class Speeker {

   private static final Messages MESSAGES = Messages.getInstance();

   private ExecutorService speechesExecutor = Executors.newSingleThreadExecutor();
   private volatile boolean speeking;
   private AudioPlayer currentlySpeeking;
   private int speechId;
   private Set<EndOfSpeechListener> endOfSpeechListeners = new HashSet<>();
   private MaryInterface marytts;

   public Speeker() throws MaryConfigurationException {
      initMaryTTS();
   }

   public void stop() {
      log.info("Stopping Speeker");
      stopSpeeking();
      speechesExecutor.shutdown();
   }

   public void stopSpeeking() {
      speeking = false;
      if (currentlySpeeking != null) {
         currentlySpeeking.cancel();
      }
   }

   public void speek(List<String> speeches) {
      speeking = true;
      speechId = -1;
      for (String speech : speeches) {
         if (speeking && !speech.trim().isEmpty()) {
            speechId++;
            speechesExecutor.execute(() -> speek(speech));
         }
      }
   }

   private void speek(String speech) {
      log.debug("Preparing to speek: " + speech);
      try {
         AudioInputStream audio = marytts.generateAudio(speech.toLowerCase().trim());
         currentlySpeeking = new AudioPlayer(audio);
         log.debug("Speeking: " + speech);
         currentlySpeeking.start();
         currentlySpeeking.join();
      } catch (Exception e) {
         log.error("Unable to speek: " + speech, e);
         List<String> words = Arrays.asList(speech.trim().split(" "));
         if (words.size() > 1) {
            words.stream().forEach(w -> speek(w));
         } else if (words.size() == 1) {
            ToastWindow.showToast(String.format(MESSAGES.get("offending_word"), words.get(0)), true);
         }
      } finally {
         notifyEndOfSpeechListeners(speechId);
      }
   }

   public void save(String speech, File file) {
      new Thread("Saving speeches") {

         @Override
         public void run() {
            if (!speech.trim().isEmpty()) {
               ToastWindow toast = ToastWindow.showToast(MESSAGES.get("saving"), false);
               try {
                  AudioInputStream audio = marytts.generateAudio(speech.toLowerCase());
                  AudioSystem.write(audio, AudioFileFormat.Type.WAVE, file);
                  toast.setVisible(false);
                  toast.dispose();
                  ToastWindow.showToast(MESSAGES.get("ready_saving"), true);
               } catch (SynthesisException | IOException e) {
                  log.error("Unable to save speech", e);
                  toast.setVisible(false);
                  toast.dispose();
                  SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        MESSAGES.get("offending_speech"), MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE));
               }
            }
         }
      }.start();
   }

   public void initMaryTTS() throws MaryConfigurationException {
      log.debug("Initialiazing MaryTTS");
      marytts = new LocalMaryInterface();

      String voice = Voice.getSelectedVoice().getName();
      log.debug("Setting voice to: " + voice);
      marytts.setVoice(voice);

      String effects = SoundEffect.toMaryTTSString();
      log.debug("Setting effects to: " + effects);
      marytts.setAudioEffects(effects);
   }

   public void addEndOfSpeechListener(EndOfSpeechListener listener) {
      endOfSpeechListeners.add(listener);
   }

   private void notifyEndOfSpeechListeners(int speechId) {
      endOfSpeechListeners.stream().forEach(l -> l.endOfSpeech(speechId));
   }

   public interface EndOfSpeechListener {

      void endOfSpeech(int speechId);
   }
}
