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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;

import marytts.exceptions.MaryConfigurationException;

import org.apache.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import raging.goblin.speechless.Messages;
import raging.goblin.speechless.SpeechLessProperties;
import raging.goblin.speechless.speech.Speeker;
import raging.goblin.speechless.speech.Speeker.EndOfSpeechListener;

public class ClientWindow extends JFrame implements EndOfSpeechListener {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final SpeechLessProperties PROPERTIES = SpeechLessProperties.getInstance();
   private static final Logger LOG = Logger.getLogger(ClientWindow.class);

   private Speeker speeker;
   private int lastOfferedToSpeek;
   private Timer clickTimer;

   private JTextField typingField;
   private JTextArea speakingArea;
   private JButton saveButton;
   private JButton playButton;
   private JButton stopButton;
   private JPanel parseTextButtonPanel;

   public ClientWindow(Speeker speeker) throws MaryConfigurationException {
      this.speeker = speeker;
      speeker.addEndOfSpeechListener(this);
      initGui();
      if (SystemTray.isSupported() && PROPERTIES.isSystrayEnabled()) {
         loadSystray();
         initNativeHook();
      }
      setTitle(MESSAGES.get("client_window_title"));
      setVisible(PROPERTIES.isSystrayEnabled());
   }

   @Override
   public void endOfSpeech(int speechIndex) {
      if (lastOfferedToSpeek == speechIndex) {
         setParsing(false);
      }
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      typingField.grabFocus();
   }

   private void initGui() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(600, 400);
      ScreenPositioner.centerOnScreen(this);
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
               speeker.speek(Arrays.asList(typingField.getText()));
               lastOfferedToSpeek = -1;
               speakingArea.setText(speakingArea.getText() + "\n" + typingField.getText());
               speakingArea.setCaretPosition(speakingArea.getText().length());
               typingField.setText("");
               typingField.requestFocusInWindow();
            }
         }
      });

      GridLayout buttonLayout = new GridLayout(2, 0);
      buttonLayout.setVgap(20);
      parseTextButtonPanel = new JPanel(buttonLayout);
      parseTextButtonPanel.setPreferredSize(new Dimension(50, 50));
      getContentPane().add(parseTextButtonPanel, BorderLayout.EAST);

      saveButton = new JButton(getIcon("/icons/save.png"));
      saveButton.setToolTipText(MESSAGES.get("save_tooltip"));
      saveButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            int returnValue = chooser.showOpenDialog(ClientWindow.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               File file = chooser.getSelectedFile();
               speeker.save(speakingArea.getText(), file);
            }
         }
      });
      parseTextButtonPanel.add(saveButton, BorderLayout.EAST);

      playButton = new JButton(getIcon("/icons/control_play.png"));
      playButton.setToolTipText(MESSAGES.get("play_tooltip"));
      playButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            setParsing(true);
            lastOfferedToSpeek = -1;
            List<String> speeches = Arrays.asList(speakingArea.getText().split("\n"));
            for (int i = 0; i < speeches.size(); i++) {
               if (!speeches.get(i).trim().isEmpty()) {
                  lastOfferedToSpeek++;
               }
            }
            speeker.speek(speeches);
         }
      });
      parseTextButtonPanel.add(playButton, BorderLayout.EAST);

      stopButton = new JButton(getIcon("/icons/control_stop.png"));
      stopButton.setToolTipText(MESSAGES.get("stop_tooltip"));
      stopButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            speeker.stopSpeeking();
            setParsing(false);
         }
      });
   }

   private void initNativeHook() {
      try {
         java.util.logging.Logger logger = java.util.logging.Logger
               .getLogger(GlobalScreen.class.getPackage().getName());
         logger.setLevel(Level.OFF);
         GlobalScreen.registerNativeHook();
         GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

            private Set<Integer> pressedKeyCodes = new HashSet<>();

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
               // Nothing todo
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
               pressedKeyCodes.clear();
            }

            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
               if (Arrays.stream(PROPERTIES.getNativeHookKeyCodes()).anyMatch(new Integer(e.getKeyCode())::equals)) {
                  pressedKeyCodes.add(e.getKeyCode());
               }
               if (pressedKeyCodes.size() == 3) {
                  pressedKeyCodes.clear();
                  SwingUtilities.invokeLater(new Runnable() {

                     @Override
                     public void run() {
                        setVisible(!isVisible());
                     }
                  });
               }
            }
         });
      } catch (NativeHookException ex) {
         LOG.warn("Unable to use native hook, disabling it");
         PROPERTIES.setNativeHookEnabled(false);
      }
   }

   private void setParsing(boolean parsing) {
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

   private void loadSystray() {
      try {
         SystemTray tray = SystemTray.getSystemTray();
         TrayIcon trayIcon = new TrayIcon(new ImageIcon(getClass().getResource("/icons/sound.png")).getImage());
         trayIcon.setImageAutoSize(true);
         trayIcon.addMouseListener(new TrayIconMouseListener());
         tray.add(trayIcon);
      } catch (Exception e) {
         LOG.warn("TrayIcon could not be added.");
         LOG.debug("TrayIcon could not be added.", e);
         PROPERTIES.setSystrayEnabled(false);
      }
   }

   private class TrayIconMouseListener implements MouseListener {

      @Override
      public void mouseClicked(MouseEvent e) {
         // Left button
         if (e.getButton() != MouseEvent.BUTTON3) {
            if (e.getClickCount() == 1) {
               // Single click - toggle visibility of sticky notes
               clickTimer = new Timer(PROPERTIES.getDoubleClickDelay(), new ActionListener() {

                  @Override
                  public void actionPerformed(ActionEvent e) {
                     setVisible(!isVisible());
                  }
               });
               clickTimer.setRepeats(false);
               clickTimer.start();
            } else if (e.getClickCount() >= 2) {
               // Double click - show/hide gui
               clickTimer.stop();
               setVisible(!isVisible());
            }
         }
      }

      @Override
      public void mouseEntered(MouseEvent e) {
         // Nothing to do
      }

      @Override
      public void mouseExited(MouseEvent e) {
         // Nothing to do
      }

      @Override
      public void mousePressed(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON3) {
            // Right button
         }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         // Nothing to do
      }
   }
}
