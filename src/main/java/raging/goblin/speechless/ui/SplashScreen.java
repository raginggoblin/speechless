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

package raging.goblin.speechless.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import raging.goblin.speechless.Messages;

public class SplashScreen extends JFrame {

   private static final Messages MESSAGES = Messages.getInstance();

   private JLabel messageLabel = new JLabel(MESSAGES.get("loading") + "...");

   public SplashScreen() {
      setResizable(false);
      setAlwaysOnTop(true);
      setUndecorated(true);
      setSize(400, 250);

      messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
      getContentPane().add(messageLabel, BorderLayout.CENTER);
   }

   public void setMessage(String message) {
      messageLabel.setText(message);
   }
}
