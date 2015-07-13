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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

import marytts.exceptions.MaryConfigurationException;
import raging.goblin.speechless.Messages;
import raging.goblin.speechless.speech.Speeker;

public class ClientWindow extends JFrame {

   private static final Messages MESSAGES = Messages.getInstance();

   private Speeker speeker;
   private boolean parsing = false;

   private JTextField typingField;
   private JTextArea speakingArea;
   private JButton saveButton;
   private JButton playButton;
   private JButton stopButton;

   private JPanel parseTextButtonPanel;

   public ClientWindow() throws MaryConfigurationException {
      initGui();
      setTitle(MESSAGES.get("client_window_title"));
      speeker = new Speeker();
   }

   public void setFocusOnTextField() {
      typingField.grabFocus();
   }

   private void initGui() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(600, 400);
      BorderLayout layout = new BorderLayout(10, 10);
      getContentPane().setLayout(layout);
      Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      ((JComponent) getContentPane()).setBorder(padding);

      speakingArea = new JTextArea();
      speakingArea.setWrapStyleWord(true);
      speakingArea.setLineWrap(true);
      JScrollPane speakingPane = new JScrollPane(speakingArea);
      speakingPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      getContentPane().add(speakingPane, BorderLayout.CENTER);

      typingField = new JTextField();
      getContentPane().add(typingField, BorderLayout.SOUTH);
      typingField.addKeyListener(new KeyAdapter() {

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               speeker.speek(typingField.getText());
               speakingArea.setText(speakingArea.getText() + "\n" + typingField.getText());
               speakingArea.setCaretPosition(speakingArea.getText().length());
               typingField.setText("");
               typingField.requestFocusInWindow();
            }
         }
      });

      GridLayout buttonLayout = new GridLayout(2, 0);
      buttonLayout.setVgap(10);
      parseTextButtonPanel = new JPanel(buttonLayout);
      getContentPane().add(parseTextButtonPanel, BorderLayout.EAST);

      saveButton = new JButton(getIcon("/icons/save.png"));
      saveButton.setToolTipText(MESSAGES.get("save_tooltip"));
      parseTextButtonPanel.add(saveButton, BorderLayout.EAST);

      playButton = new JButton(getIcon("/icons/control_play.png"));
      playButton.setToolTipText(MESSAGES.get("play_tooltip"));
      playButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            toggleParsing();
            for (String speech : speakingArea.getText().split("\n")) {
               if (!speech.isEmpty()) {
                  speeker.speek(speakingArea.getText());
               }
            }
         }
      });
      parseTextButtonPanel.add(playButton, BorderLayout.EAST);

      stopButton = new JButton(getIcon("/icons/control_stop.png"));
      stopButton.setToolTipText(MESSAGES.get("stop_tooltip"));

   }

   private void toggleParsing() {
      parsing = !parsing;
      typingField.setEnabled(!parsing);
      speakingArea.setEnabled(!parsing);
      saveButton.setEnabled(!parsing);
      playButton.setVisible(!parsing);
      stopButton.setVisible(parsing);
      if (parsing) {
         parseTextButtonPanel.remove(playButton);
         parseTextButtonPanel.add(stopButton);
      } else {
         parseTextButtonPanel.add(playButton);
         parseTextButtonPanel.remove(stopButton);
      }
   }

   private static ImageIcon getIcon(String resource) {
      return new ImageIcon(ClientWindow.class.getResource(resource));
   }
}
