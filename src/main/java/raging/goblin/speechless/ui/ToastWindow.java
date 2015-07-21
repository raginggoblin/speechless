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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import raging.goblin.speechless.SpeechLessProperties;

public class ToastWindow extends JFrame {

   private static final int MARGIN = 25;
   private static final SpeechLessProperties PROPERTIES = SpeechLessProperties.getInstance();
   private static final String textFormat = "<html>%s</html>";

   private JLabel messageLabel;

   public static ToastWindow showToast(String message, boolean autoHide) {
      ToastWindow instance = new ToastWindow();

      SwingUtilities.invokeLater(() -> {
         instance.messageLabel.setText(String.format(textFormat, message));
         instance.setVisible(true);
      });

      Timer timer = new Timer(PROPERTIES.getToastTime(), new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            instance.setVisible(false);
            instance.dispose();
         }
      });
      timer.setRepeats(false);
      if (autoHide) {
         timer.start();
      }

      return instance;
   }

   private ToastWindow() {
      setUndecorated(true);
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      if (gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
         setOpacity(0.55f);
      }
      setSize(250, 100);
      setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
      addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
         }
      });
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Dimension resolution = toolkit.getScreenSize();
      int x = (int) resolution.getWidth() - getWidth() - MARGIN;
      int y = MARGIN;
      setLocation(x, y);
      setBackground(new Color(0f, 0f, 0f, 1f / 2f));
      messageLabel = new JLabel();
      messageLabel.setForeground(Color.WHITE);
      messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
      add(messageLabel);
   }
}
