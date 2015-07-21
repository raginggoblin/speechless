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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import raging.goblin.speechless.Messages;
import raging.goblin.speechless.SpeechLessProperties;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SoundEffect {

   public enum Effect {
      TRACT_SCALER, F0_SCALE, F0_ADD, RATE, ROBOT, WHISPER, STADIUM, CHORUS, FIR_FILTER, JET_PILOT
   };

   private static final Preferences PREFERENCES = Preferences.userNodeForPackage(SoundEffect.class);
   private static final Messages MESSAGES = Messages.getInstance();
   private static final List<SoundEffect> EFFECTS = new ArrayList<>();

   @Getter
   private Effect effect;
   @Getter
   private String helpText;
   @Getter
   private String format;
   private Map<String, Double> defaultLevels;

   public static List<SoundEffect> getSoundEffects(SpeechLessProperties properties) {
      if (EFFECTS.isEmpty()) {
         EFFECTS.add(createTractScaler());
         EFFECTS.add(createF0Add());
         EFFECTS.add(createF0Scale());
         EFFECTS.add(createRate());
         EFFECTS.add(createRobot());
         EFFECTS.add(createWhisper());
         EFFECTS.add(createStadium());
         EFFECTS.add(createChorus());
         EFFECTS.add(createFirFilter());
         EFFECTS.add(createJetPilot());
      }
      return EFFECTS;
   }

   public Set<String> getLevelKeys() {
      return new HashSet<>(defaultLevels.keySet());
   }

   public void setLevel(String key, double value) {
      if (defaultLevels.containsKey(key)) {
         PREFERENCES.putDouble(effect + "_" + key, value);
      }
   }

   public double getLevel(String key) {
      return PREFERENCES.getDouble(effect + "_" + key, defaultLevels.get(key));
   }

   public void setDefaultLevels() {
      for (String key : defaultLevels.keySet()) {
         setLevel(key, defaultLevels.get(key));
      }
   }

   public boolean isEnabled() {
      return PREFERENCES.getBoolean(effect.name() + "_enabled", false);
   }

   public void setEnabled(boolean enabled) {
      PREFERENCES.putBoolean(effect.name() + "_enabled", enabled);
   }

   private static SoundEffect createTractScaler() {
      return new SoundEffect(Effect.TRACT_SCALER, MESSAGES.get("tract_scaler_help"), "TractScaler amount:%.2f;",
            createDefaultLevelsMap(Arrays.asList("amount"), Arrays.asList(1.5)));
   }

   private static SoundEffect createJetPilot() {
      return new SoundEffect(Effect.JET_PILOT, MESSAGES.get("jet_pilot_help"), "JetPilot;", new HashMap<>());
   }

   private static SoundEffect createF0Scale() {
      return new SoundEffect(Effect.F0_SCALE, MESSAGES.get("f0_scaling_help"), "F0Scale f0Scale:%.2f;",
            createDefaultLevelsMap(Arrays.asList("f0Scale"), Arrays.asList(2.0)));
   }

   private static SoundEffect createF0Add() {
      return new SoundEffect(Effect.F0_ADD, MESSAGES.get("f0_add_help"), "F0Add f0Add:%.2f;", createDefaultLevelsMap(
            Arrays.asList("f0Add"), Arrays.asList(50.0)));
   }

   private static SoundEffect createRate() {
      return new SoundEffect(Effect.RATE, MESSAGES.get("rate_help"), "Rate durScale:%.2f;", createDefaultLevelsMap(
            Arrays.asList("durScale"), Arrays.asList(1.5)));
   }

   private static SoundEffect createRobot() {
      return new SoundEffect(Effect.ROBOT, MESSAGES.get("robot_help"), "Robot amount:%.2f;", createDefaultLevelsMap(
            Arrays.asList("amount"), Arrays.asList(100.0)));
   }

   private static SoundEffect createWhisper() {
      return new SoundEffect(Effect.WHISPER, MESSAGES.get("whisper_help"), "Whisper amount:%.2f;",
            createDefaultLevelsMap(Arrays.asList("amount"), Arrays.asList(100.0)));
   }

   private static SoundEffect createStadium() {
      return new SoundEffect(Effect.STADIUM, MESSAGES.get("stadium_help"), "Stadium amount:%.2f;",
            createDefaultLevelsMap(Arrays.asList("amount"), Arrays.asList(100.0)));
   }

   private static SoundEffect createChorus() {
      return new SoundEffect(Effect.CHORUS, MESSAGES.get("chorus_help"),
            "Chorus delay1:%.2f;amp1:%.2f;delay2:%.2f;amp2:%.2f;delay3:%.2f;amp3:%.2f;", createDefaultLevelsMap(
                  Arrays.asList("delay1", "amp1", "delay2", "amp2", "delay3", "amp3"),
                  Arrays.asList(466.0, 0.54, 600.0, -0.10, 250.0, 0.30)));
   }

   private static SoundEffect createFirFilter() {
      return new SoundEffect(Effect.FIR_FILTER, MESSAGES.get("fir_filter_help"),
            "FIRFilter type:%.2f;fc1:%.2f;fc2:%.2f;", createDefaultLevelsMap(Arrays.asList("type", "fc1", "fc2"),
                  Arrays.asList(3.0, 500.0, 2000.0)));
   }

   private static Map<String, Double> createDefaultLevelsMap(List<String> keys, List<Double> values) {
      Map<String, Double> defaultLevels = new LinkedHashMap<>();
      for (int i = 0; i < keys.size(); i++) {
         defaultLevels.put(keys.get(i), values.get(i));
      }
      return defaultLevels;
   }
}
