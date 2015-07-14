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

import org.apache.log4j.Logger;

import marytts.exceptions.MaryConfigurationException;
import raging.goblin.speechless.ui.ClientWindow;
import raging.goblin.speechless.ui.ScreenPositioner;
import raging.goblin.speechless.ui.SplashScreen;

public class Application {

   private static final Logger LOG = Logger.getLogger(Application.class);
   private static final SpeechLessProperties PROPERTIES = SpeechLessProperties.getInstance();
   private static final Messages MESSAGES = Messages.getInstance();

   public static void main(String[] args) {
      loadLaf();
      setLocale();

      SplashScreen splashScreen = new SplashScreen();
      ScreenPositioner.centerOnScreen(splashScreen);
      splashScreen.setVisible(PROPERTIES.isSplashScreenEnabled());

      try {
         ClientWindow clientWindow = new ClientWindow();
         ScreenPositioner.centerOnScreen(clientWindow);
         splashScreen.setVisible(false);
         splashScreen.dispose();
         clientWindow.setVisible(true);
         clientWindow.setFocusOnTextField();
      } catch (MaryConfigurationException e) {
         LOG.error("Unable to start Speeker", e);
         splashScreen.setMessage(MESSAGES.get("initialization_error"));
         try {
            Thread.sleep(5000);
         } catch (InterruptedException e1) {
            LOG.error(e.getMessage(), e);
         }
         System.exit(0);
      }
   }

   private static void setLocale() {
      Locale.setDefault(new Locale(PROPERTIES.getLocaleLanguage(), PROPERTIES.getLocaleCountry()));
   }

   private static void loadLaf() {
      try {
         if (UIManager.getSystemLookAndFeelClassName().contains("Metal")) {
            setNimbusLaf();
         } else {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LOG.debug("Setting system look and feel");
         }
      } catch (Exception e) {
         LOG.error("Could not set system look and feel");
         LOG.debug(e.getMessage(), e);
      }
   }

   private static void setNimbusLaf() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
         UnsupportedLookAndFeelException {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            LOG.debug("Setting Nimbus look and feel");
            break;
         }
      }
   }
}
