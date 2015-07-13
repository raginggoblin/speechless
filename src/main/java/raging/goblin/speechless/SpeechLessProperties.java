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

package raging.goblin.speechless;

import java.util.Arrays;
import java.util.Locale;
import java.util.prefs.Preferences;

public class SpeechLessProperties {

   private static final String DEFAULT_NATIVE_HOOK_KEY_CODES = "29,42,49";

   public static final boolean DEFAULT_SPLASH_SCREEN_ENABLED = true;
   public static final String DEFAULT_VOICE = "dfki-obadiah-hsmm";

   private static final String KEY_LANGUAGE = "language";
   private static final String KEY_COUNTRY = "country";
   private static final String KEY_NATIVE_HOOK_KEY_CODES = "nativehookkeycodes";
   private static final String KEY_SPLASH_SCREEN_ENABLED = "nativehookkeycodes";
   private static final String KEY_VOICE = "voice";

   private static SpeechLessProperties instance;
   private boolean nativeHookEnabled = true;
   private int[] nativeHookKeyCodes;
   private static Preferences userPreferences = Preferences.userNodeForPackage(SpeechLessProperties.class);

   private SpeechLessProperties() {
      // Singleton
   }

   public static SpeechLessProperties getInstance() {
      if (instance == null) {
         instance = new SpeechLessProperties();
      }
      return instance;
   }

   public String getLocaleLanguage() {
      Locale currentLocale = Locale.getDefault();
      return userPreferences.get(KEY_LANGUAGE, currentLocale.getLanguage());
   }

   public void setLocaleLanguage(String language) {
      userPreferences.put(KEY_LANGUAGE, language);
   }

   public String getLocaleCountry() {
      Locale currentLocale = Locale.getDefault();
      return userPreferences.get(KEY_COUNTRY, currentLocale.getCountry());
   }

   public void setLocaleCountry(String country) {
      userPreferences.put(KEY_COUNTRY, country);
   }

   public void setSplashScreenEnabled(boolean splashScreenEnabled) {
      userPreferences.putBoolean(KEY_SPLASH_SCREEN_ENABLED, splashScreenEnabled);
   }

   public boolean isSplashScreenEnabled() {
      return userPreferences.getBoolean(KEY_SPLASH_SCREEN_ENABLED, DEFAULT_SPLASH_SCREEN_ENABLED);
   }

   public boolean isNativeHookEnabled() {
      return nativeHookEnabled;
   }

   public void setNativeHookEnabled(boolean nativeHookEnabled) {
      this.nativeHookEnabled = nativeHookEnabled;
   }

   public int[] getNativeHookKeyCodes() {
      if (nativeHookKeyCodes == null) {
         String[] keyCodes = userPreferences.get(KEY_NATIVE_HOOK_KEY_CODES, DEFAULT_NATIVE_HOOK_KEY_CODES).split(",");
         nativeHookKeyCodes = Arrays.stream(keyCodes).mapToInt(v -> Integer.parseInt(v)).toArray();
      }
      return nativeHookKeyCodes;
   }

   public void setNativeHookKeyCodes(int[] keyCodes) {
      nativeHookKeyCodes = keyCodes;
      userPreferences.put(KEY_NATIVE_HOOK_KEY_CODES,
            Arrays.toString(keyCodes).replaceAll("\\[", "").replaceAll("\\]", ""));
   }

   public int[] getDefaultNativeHookKeyCodes() {
      return Arrays.stream(DEFAULT_NATIVE_HOOK_KEY_CODES.split(",")).mapToInt(v -> Integer.parseInt(v)).toArray();
   }

   public String getVoice() {
      return userPreferences.get(KEY_VOICE, DEFAULT_VOICE);
   }

   public void setVoice(String voice) {
      userPreferences.put(KEY_VOICE, voice);
   }
}
