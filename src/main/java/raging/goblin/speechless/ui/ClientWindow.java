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
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

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

   private JTextField typingField;
   private JTextArea speakingArea;
   private JButton saveButton;
   private JButton playButton;
   private JButton stopButton;
   private JPanel parseTextButtonPanel;

   private boolean trayMenuVisible = false;
   private Timer clickTimer;
   private JPopupMenu systrayMenu;

   public ClientWindow(Speeker speeker) throws MaryConfigurationException {
      super(MESSAGES.get("client_window_title"));
      this.speeker = speeker;
      speeker.addEndOfSpeechListener(this);
      initGui();
      if (java.awt.SystemTray.isSupported() && PROPERTIES.isSystrayEnabled()) {
         loadSystray();
         if (PROPERTIES.isNativeHookEnabled()) {
             initNativeHook();
         }
      }
      setDefaultCloseOperation(PROPERTIES.isSystrayEnabled() ? DISPOSE_ON_CLOSE : EXIT_ON_CLOSE);
      setVisible(!PROPERTIES.isSystrayEnabled());
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
      new EditAdapter(speakingArea);

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
      new EditAdapter(typingField);

      GridLayout buttonLayout = new GridLayout(2, 0);
      buttonLayout.setVgap(20);
      parseTextButtonPanel = new JPanel(buttonLayout);
      parseTextButtonPanel.setPreferredSize(new Dimension(50, 50));
      getContentPane().add(parseTextButtonPanel, BorderLayout.EAST);

      saveButton = new JButton(Icon.getIcon("/icons/save.png"));
      saveButton.setToolTipText(MESSAGES.get("save_tooltip"));
      saveButton.addActionListener(a -> {
         JFileChooser chooser = new JFileChooser();
         int returnValue = chooser.showOpenDialog(ClientWindow.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            speeker.save(speakingArea.getText(), file);
         }
      });
      parseTextButtonPanel.add(saveButton, BorderLayout.EAST);

      playButton = new JButton(Icon.getIcon("/icons/control_play.png"));
      playButton.setToolTipText(MESSAGES.get("play_tooltip"));
      playButton.addActionListener(a -> {
         setParsing(true);
         lastOfferedToSpeek = -1;
         List<String> speeches = Arrays.asList(speakingArea.getText().split("\n"));
         for (int i = 0; i < speeches.size(); i++) {
            if (!speeches.get(i).trim().isEmpty()) {
               lastOfferedToSpeek++;
            }
         }
         speeker.speek(speeches);
      });
      parseTextButtonPanel.add(playButton, BorderLayout.EAST);

      stopButton = new JButton(Icon.getIcon("/icons/control_stop.png"));
      stopButton.setToolTipText(MESSAGES.get("stop_tooltip"));
      stopButton.addActionListener(a -> {
         speeker.stopSpeeking();
         setParsing(false);
      });

      setJMenuBar(createMenu());
   }

   private JMenuBar createMenu() {
      JMenuBar menuBar = new JMenuBar();

      JMenu fileMenu = new JMenu(MESSAGES.get("file"));

      menuBar.add(fileMenu);

      JMenu editMenu = new JMenu(MESSAGES.get("edit"));

      JMenuItem configureVoiceItem = new JMenuItem(MESSAGES.get("configure_voice"));
      configureVoiceItem.addActionListener(a -> {
         VoiceConfigurationDialog dialog = new VoiceConfigurationDialog(ClientWindow.this);
         dialog.setVisible(true);
         if (dialog.isOkPressed()) {
            try {
               speeker.initMaryTTS();
            } catch (Exception e) {
               JOptionPane.showMessageDialog(ClientWindow.this, MESSAGES.get("init_marytts_error"),
                     MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE);
               LOG.error("Unable to reinitialize marytts", e);
            }
         }
      });
      editMenu.add(configureVoiceItem);

      menuBar.add(editMenu);

      JMenu helpMenu = new JMenu(MESSAGES.get("help"));
      menuBar.add(helpMenu);

      return menuBar;
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

   private void loadSystray() {
      try {
         SystemTray tray = SystemTray.getSystemTray();
         TrayIcon trayIcon = new TrayIcon(new ImageIcon(getClass().getResource("/icons/sound.png")).getImage());
         trayIcon.setImageAutoSize(true);
         trayIcon.addMouseListener(new TrayIconMouseListener());
         tray.add(trayIcon);
         systrayMenu = createSystrayMenu();
      } catch (Exception e) {
         LOG.warn("TrayIcon could not be added.");
         LOG.debug("TrayIcon could not be added.", e);
         PROPERTIES.setSystrayEnabled(false);
      }
   }

   private JPopupMenu createSystrayMenu() {
      JPopupMenu menu = new JPopupMenu();

      JMenuItem showGuiItem = new JMenuItem(MESSAGES.get("show_gui"),
            Icon.getIcon("/icons/application_form_magnify.png"));
      showGuiItem.addActionListener(e -> {
         trayMenuVisible = false;
         setVisible(true);
      });
      menu.add(showGuiItem);

      JMenuItem quitItem = new JMenuItem(MESSAGES.get("quit"), Icon.getIcon("/icons/cross.png"));
      quitItem.addActionListener(e -> System.exit(0));
      menu.add(quitItem);

      return menu;
   }

   private class EditAdapter extends MouseAdapter implements FocusListener {

      private JTextComponent parent;
      private JPopupMenu menu;

      public EditAdapter(JTextComponent parent) {
         this.parent = parent;
         parent.addMouseListener(this);
         parent.addFocusListener(this);
         menu = createEditMenu();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
         parent.requestFocus();

         // Left button
         if (e.getButton() == MouseEvent.BUTTON3) {
            menu.setLocation(e.getXOnScreen() + 1, e.getYOnScreen() + 1);
            menu.setVisible(true);
         } else {
            menu.setVisible(false);
         }
      }

      private JPopupMenu createEditMenu() {
         final JPopupMenu menu = new JPopupMenu(MESSAGES.get("edit"));

         final JMenuItem cutItem = new JMenuItem(MESSAGES.get("cut"), Icon.getIcon("/icons/cut.png"));
         cutItem.addActionListener(a -> {
            StringSelection selection = new StringSelection(parent.getSelectedText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            parent.replaceSelection("");
            menu.setVisible(false);
         });
         menu.add(cutItem);

         JMenuItem copyItem = new JMenuItem(MESSAGES.get("copy"), Icon.getIcon("/icons/page_copy.png"));
         copyItem.addActionListener(a -> {
            StringSelection selection = new StringSelection(parent.getSelectedText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            menu.setVisible(false);
         });
         menu.add(copyItem);

         JMenuItem pasteItem = new JMenuItem(MESSAGES.get("paste"), Icon.getIcon("/icons/paste_plain.png"));
         pasteItem.addActionListener(a -> {
            Transferable clipboardContents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (clipboardContents != null && clipboardContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
               try {
                  String pasted = (String) clipboardContents.getTransferData(DataFlavor.stringFlavor);
                  parent.replaceSelection(pasted);
               } catch (UnsupportedFlavorException | IOException ex) {
                  LOG.error("Unable to paste content", ex);
               }
            }

            menu.setVisible(false);
         });
         menu.add(pasteItem);

         JMenuItem deleteItem = new JMenuItem(MESSAGES.get("delete"), Icon.getIcon("/icons/page_white_delete.png"));
         deleteItem.addActionListener(a -> {
            parent.replaceSelection("");
            menu.setVisible(false);
         });
         menu.add(deleteItem);

         menu.addSeparator();

         JMenuItem selectAllItem = new JMenuItem(MESSAGES.get("select_all"), Icon.getIcon("/icons/accept.png"));
         selectAllItem.addActionListener(a -> {
            parent.setSelectionStart(0);
            parent.setSelectionEnd(parent.getText().length());
            menu.setVisible(false);
         });
         menu.add(selectAllItem);

         return menu;
      }

      @Override
      public void focusGained(FocusEvent e) {
         // nothing todo
      }

      @Override
      public void focusLost(FocusEvent e) {
         menu.setVisible(false);
      }
   }

   private class TrayIconMouseListener implements MouseListener {

      @Override
      public void mouseClicked(MouseEvent e) {
         // Left button
         if (e.getButton() != MouseEvent.BUTTON3) {
            trayMenuVisible = false;
            if (e.getClickCount() == 1) {
               // Single click - show menu
               clickTimer = new Timer(PROPERTIES.getDoubleClickDelay(), ae -> {
                  trayMenuVisible = !trayMenuVisible;
                  systrayMenu.setLocation(e.getXOnScreen() + 1, e.getYOnScreen() + 1);
                  systrayMenu.setInvoker(systrayMenu);
                  systrayMenu.setVisible(trayMenuVisible);
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
            // Right button - Do nothing
         }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         // Nothing to do
      }
   }
}
