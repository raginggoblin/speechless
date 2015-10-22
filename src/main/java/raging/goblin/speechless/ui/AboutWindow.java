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
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import raging.goblin.speechless.Messages;

public class AboutWindow extends JDialog {

   private static final Messages MESSAGES = Messages.getInstance();

   public AboutWindow(Frame parent) {
      super(parent);
      setModal(true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setTitle(MESSAGES.get("client_window_title"));

      setSize(550, 250);
      ScreenPositioner.centerOnScreen(this);

      JPanel topPanel = new JPanel();
      getContentPane().add(topPanel, BorderLayout.NORTH);
      JLabel iconLabel = new JLabel(new ImageIcon(AboutWindow.class.getResource("/icons/sound.png")));
      topPanel.add(iconLabel);
      topPanel.add(new JLabel(MESSAGES.get("about_title")));

      JPanel centerPanel = new JPanel();
      getContentPane().add(centerPanel, BorderLayout.CENTER);
      centerPanel.add(new JLabel(MESSAGES.get("about_text")));
   }
}
