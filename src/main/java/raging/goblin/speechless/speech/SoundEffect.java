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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import raging.goblin.speechless.Messages;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SoundEffect {

   TRACT_SCALER(Messages.getInstance().get("tract_scaler"), Messages.getInstance().get("tract_scaler_help"),
         "TractScaler amount:%.2f;", createDefaultLevelsMap(Arrays.asList("amount"), Arrays.asList(1.5))),

   F0_SCALE(Messages.getInstance().get("f0_scaling"), Messages.getInstance().get("f0_scaling_help"),
         "F0Scale f0Scale:%.2f;", createDefaultLevelsMap(Arrays.asList("f0Scale"), Arrays.asList(2.0))),

   F0_ADD(Messages.getInstance().get("f0_add"), Messages.getInstance().get("f0_add_help"), "F0Add f0Add:%.2f;",
         createDefaultLevelsMap(Arrays.asList("f0Add"), Arrays.asList(50.0))),

   RATE(Messages.getInstance().get("rate"), Messages.getInstance().get("rate_help"), "Rate durScale:%.2f;",
         createDefaultLevelsMap(Arrays.asList("durScale"), Arrays.asList(1.5))),

   ROBOT(Messages.getInstance().get("robot"), Messages.getInstance().get("robot_help"), "Robot amount:%.2f;",
         createDefaultLevelsMap(Arrays.asList("amount"), Arrays.asList(100.0))),

   WHISPER(Messages.getInstance().get("whisper"), Messages.getInstance().get("whisper_help"), "Whisper amount:%.2f;",
         createDefaultLevelsMap(Arrays.asList("amount"), Arrays.asList(100.0))),

   STADIUM(Messages.getInstance().get("stadium"), Messages.getInstance().get("stadium_help"), "Stadium amount:%.2f;",
         createDefaultLevelsMap(Arrays.asList("amount"), Arrays.asList(100.0))),

   CHORUS(Messages.getInstance().get("chorus"), Messages.getInstance().get("chorus_help"),
         "Chorus delay1:%.2f;amp1:%.2f;delay2:%.2f;amp2:%.2f;delay3:%.2f;amp3:%.2f;", createDefaultLevelsMap(
               Arrays.asList("delay1", "amp1", "delay2", "amp2", "delay3", "amp3"),
               Arrays.asList(466.0, 0.54, 600.0, -0.10, 250.0, 0.30))),

   FIR_FILTER(Messages.getInstance().get("fir_filter"), Messages.getInstance().get("fir_filter_help"),
         "FIRFilter type:%.2f;fc1:%.2f;fc2:%.2f;", createDefaultLevelsMap(Arrays.asList("type", "fc1", "fc2"),
               Arrays.asList(3.0, 500.0, 2000.0))),

   JET_PILOT(Messages.getInstance().get("jet_pilot"), Messages.getInstance().get("jet_pilot_help"), "JetPilot;",
         new HashMap<>());

   private static final Preferences PREFERENCES = Preferences.userNodeForPackage(SoundEffect.class);

   private String visualName;
   @Getter
   private String helpText;
   private String format;
   private Map<String, Double> defaultLevels;

   public Set<String> getLevelKeys() {
      return new HashSet<>(defaultLevels.keySet());
   }

   public void setLevel(String key, double value) {
      if (defaultLevels.containsKey(key)) {
         PREFERENCES.putDouble(name() + "_" + key, value);
      }
   }

   public double getLevel(String key) {
      return PREFERENCES.getDouble(name() + "_" + key, defaultLevels.get(key));
   }

   public void setDefaultLevels() {
      for (String key : defaultLevels.keySet()) {
         setLevel(key, defaultLevels.get(key));
      }
   }

   public boolean isEnabled() {
      return PREFERENCES.getBoolean(name() + "_enabled", false);
   }

   public void setEnabled(boolean enabled) {
      PREFERENCES.putBoolean(name() + "_enabled", enabled);
   }

   @Override
   public String toString() {
      return visualName;
   }

   public String toMaryTTSString() {
      List<Double> values = defaultLevels.keySet().stream().map(k -> getLevel(k)).collect(Collectors.toList());
      return String.format(format, values.toArray());
   }

   private static Map<String, Double> createDefaultLevelsMap(List<String> keys, List<Double> values) {
      Map<String, Double> defaultLevels = new LinkedHashMap<>();
      for (int i = 0; i < keys.size(); i++) {
         defaultLevels.put(keys.get(i), values.get(i));
      }
      return defaultLevels;
   }
}
