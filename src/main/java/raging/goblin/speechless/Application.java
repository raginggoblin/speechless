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

import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import marytts.exceptions.MaryConfigurationException;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import raging.goblin.speechless.speech.Speeker;
import raging.goblin.speechless.ui.ClientWindow;
import raging.goblin.speechless.ui.ScreenPositioner;
import raging.goblin.speechless.ui.SplashScreen;
import raging.goblin.speechless.ui.WelcomeScreen;

@Slf4j
public class Application {

   private static final UIProperties PROPERTIES = UIProperties.getInstance();
   private static final Messages MESSAGES = Messages.getInstance();

   public static void main(String[] args) {
      loadLaf(args);
      setLocale();

      SplashScreen splashScreen = new SplashScreen();
      ScreenPositioner.centerOnScreen(splashScreen);
      splashScreen.setVisible(PROPERTIES.isSplashScreenEnabled());

      try {
         Speeker speeker = new Speeker();
         Runtime.getRuntime().addShutdownHook(new ShutdownHook(speeker));
         new ClientWindow(speeker);
         splashScreen.setVisible(false);
         splashScreen.dispose();
         if (PROPERTIES.isWelcomeScreenEnabled()) {
            WelcomeScreen welcomeScreen = new WelcomeScreen();
            ScreenPositioner.centerOnScreen(welcomeScreen);
            welcomeScreen.setVisible(true);
         }

      } catch (MaryConfigurationException e) {
         log.error("Unable to start Speeker", e);
         splashScreen.setMessage(MESSAGES.get("initialization_error"));
         try {
            Thread.sleep(5000);
         } catch (InterruptedException e1) {
            log.error(e.getMessage(), e);
         }
         System.exit(0);
      }
   }

   private static void setLocale() {
      String language = PROPERTIES.getLocaleLanguage();
      String country = PROPERTIES.getLocaleCountry();
      log.debug(String.format("Setting locale to: %s/%s", language, country));
      Locale.setDefault(new Locale(language, country));
   }

   private static void loadLaf(String[] args) {
      try {
         boolean isGtk = tryLinuxLaf();
         if (!isGtk) {
            if (UIManager.getSystemLookAndFeelClassName().contains("Metal")) {
               setNimbusLaf();
            } else {
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
               log.debug("Setting system look and feel");
            }
         }
      } catch (Exception e) {
         log.error("Could not set look and feel");
         log.debug(e.getMessage(), e);
      }
   }

   private static boolean tryLinuxLaf() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
         UnsupportedLookAndFeelException {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
         if ("GTK+".equals(info.getName())) {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            log.debug("Setting GTK+ look and feel");
            return true;
         }
      }
      log.debug("Not able to set GTK+ look and feel");
      return false;
   }

   private static void setNimbusLaf() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
         UnsupportedLookAndFeelException {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            log.debug("Setting Nimbus look and feel");
            break;
         }
      }
      log.debug("Not able to set  look and feel");
   }

   @AllArgsConstructor
   private static class ShutdownHook extends Thread {

      private Speeker speeker;

      @Override
      public void run() {
         try {
            GlobalScreen.unregisterNativeHook();
            speeker.stop();
            log.info("Speechless shutdown complete");
         } catch (NativeHookException ex) {
            log.warn("Unable to unregister native hook");
         }
      }
   }
}
