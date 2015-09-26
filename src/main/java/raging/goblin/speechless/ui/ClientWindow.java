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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import lombok.extern.slf4j.Slf4j;
import marytts.exceptions.MaryConfigurationException;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import raging.goblin.speechless.Messages;
import raging.goblin.speechless.UIProperties;
import raging.goblin.speechless.speech.Speeker;
import raging.goblin.speechless.speech.Speeker.EndOfSpeechListener;

@Slf4j
public class ClientWindow extends JFrame implements EndOfSpeechListener {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final UIProperties PROPERTIES = UIProperties.getInstance();

   private Speeker speeker;

   private JTextField typingField;
   private JTextArea speakingArea;
   private JButton saveButton;
   private JButton playButton;
   private JButton stopButton;
   private JPanel parseTextButtonPanel;

   private boolean trayMenuVisible = false;
   private boolean shiftPressed = false;
   private boolean ctrlPressed = false;
   private Timer clickTimer;
   private JPopupMenu systrayMenu;

   private ActionListener playListener = a -> {
      setParsing(true);
      List<String> speeches = Arrays.asList(speakingArea.getText().trim().split("\n"));
      if (speeches.size() < 1) {
         setParsing(false);
         typingField.grabFocus();
      } else {
         speeker.speek(speeches);
      }
   };

   private ActionListener saveListener = a -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle(MESSAGES.get("save_to_wav"));
      chooser.setFileFilter(new WaveFilter());
      int returnValue = chooser.showOpenDialog(ClientWindow.this);
      if (returnValue == JFileChooser.APPROVE_OPTION) {
         File file = chooser.getSelectedFile();
         if (!file.getName().endsWith("wav") || !file.getName().endsWith("WAV")) {
            file = new File(file.getAbsolutePath() + ".wav");
         }
         speeker.save(speakingArea.getText(), file);
      }
   };

   public ClientWindow(Speeker speeker) throws MaryConfigurationException {
      super(MESSAGES.get("client_window_title"));
      this.speeker = speeker;
      speeker.addEndOfSpeechListener(this);
      initGui();
      if (SystemTray.isSupported() && PROPERTIES.isSystrayEnabled()) {
         loadSystray();
      }
      if (PROPERTIES.isNativeHookEnabled()) {
         initNativeHook();
      }
      setDefaultCloseOperation(PROPERTIES.isSystrayEnabled() ? DISPOSE_ON_CLOSE : EXIT_ON_CLOSE);
      setVisible(!PROPERTIES.isSystrayEnabled());
   }

   @Override
   public void endOfSpeech() {
      SwingUtilities.invokeLater(() -> {
         setParsing(false);
         typingField.grabFocus();
      });
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
      speakingArea.addKeyListener(new KeyAdapter() {

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
               if (shiftPressed) {
                  typingField.grabFocus();
               } else {
                  saveButton.grabFocus();
               }
               e.consume();
            }
         }
      });
      getContentPane().add(speakingPane, BorderLayout.CENTER);
      new EditAdapter(speakingArea);

      typingField = new JTextField();
      getContentPane().add(typingField, BorderLayout.SOUTH);
      typingField.addKeyListener(new KeyAdapter() {

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               speeker.speek(Arrays.asList(typingField.getText()));
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
      saveButton.addActionListener(saveListener);
      parseTextButtonPanel.add(saveButton, BorderLayout.EAST);

      playButton = new JButton(Icon.getIcon("/icons/control_play.png"));
      playButton.setToolTipText(MESSAGES.get("play_tooltip"));
      playButton.addActionListener(playListener);
      parseTextButtonPanel.add(playButton, BorderLayout.EAST);

      stopButton = new JButton(Icon.getIcon("/icons/control_stop.png"));
      stopButton.setToolTipText(MESSAGES.get("stop_tooltip"));
      stopButton.addActionListener(a -> {
         speeker.stopSpeeking();
         setParsing(false);
      });

      addGlobalKeyAdapters(typingField, speakingArea, saveButton, playButton);

      setJMenuBar(createMenu());
   }

   private JMenuBar createMenu() {
      JMenuBar menuBar = new JMenuBar();

      JMenu fileMenu = new JMenu(MESSAGES.get("file"));
      fileMenu.setMnemonic(KeyEvent.VK_F);
      menuBar.add(fileMenu);

      JMenuItem quitItem = new JMenuItem(MESSAGES.get("quit"));
      quitItem.setMnemonic(KeyEvent.VK_Q);
      quitItem.addActionListener(a -> {
         System.exit(0);
      });
      fileMenu.add(quitItem);

      JMenu editMenu = new JMenu(MESSAGES.get("edit"));
      editMenu.setMnemonic(KeyEvent.VK_E);
      menuBar.add(editMenu);

      JMenuItem configureVoiceItem = new JMenuItem(MESSAGES.get("configure_voice"));
      configureVoiceItem.setMnemonic(KeyEvent.VK_V);
      configureVoiceItem.addActionListener(a -> {
         VoiceConfigurationDialog dialog = new VoiceConfigurationDialog(ClientWindow.this);
         dialog.setVisible(true);
         if (dialog.isOkPressed()) {
            try {
               speeker.initMaryTTS();
            } catch (Exception e) {
               JOptionPane.showMessageDialog(ClientWindow.this, MESSAGES.get("init_marytts_error"),
                     MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE);
               log.error("Unable to reinitialize marytts", e);
            }
         }
      });
      editMenu.add(configureVoiceItem);

      JMenuItem configureGuiItem = new JMenuItem(MESSAGES.get("configure_gui"));
      configureGuiItem.setMnemonic(KeyEvent.VK_G);
      configureGuiItem.addActionListener(a -> {
         GuiConfigDialog dialog = new GuiConfigDialog(ClientWindow.this);
         dialog.setVisible(true);
         if (dialog.isOkPressed()) {
            JOptionPane.showMessageDialog(ClientWindow.this, MESSAGES.get("restart_message"),
                  MESSAGES.get("restart_title"), JOptionPane.INFORMATION_MESSAGE);
         }
      });
      editMenu.add(configureGuiItem);

      JMenu helpMenu = new JMenu(MESSAGES.get("help"));
      helpMenu.setMnemonic(KeyEvent.VK_H);
      menuBar.add(helpMenu);

      JMenuItem helpItem = new JMenuItem(MESSAGES.get("help"));
      helpItem.setMnemonic(KeyEvent.VK_F1);
      helpItem.addActionListener(a -> HelpBrowser.getInstance().setVisible(true));
      helpMenu.add(helpItem);

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
               if (PROPERTIES.isSystrayEnabled()) {
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
            }
         });
      } catch (NativeHookException ex) {
         log.warn("Unable to use native hook, disabling it");
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
         log.warn("TrayIcon could not be added.", e);
         PROPERTIES.setSystrayEnabled(false);
      }
   }

   private void addGlobalKeyAdapters(Component... components) {
      Arrays.stream(components).forEach(c -> {
         c.addKeyListener(new SpecialKeyAdapter());
         c.addKeyListener(new ShortCutsAdapter());
      });
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

   private class SpecialKeyAdapter extends KeyAdapter {

      @Override
      public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
         } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = true;
         }
      }

      @Override
      public void keyReleased(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
         } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = false;
         }
      }
   }

   private class ShortCutsAdapter extends KeyAdapter {

      @Override
      public void keyPressed(KeyEvent e) {
         if (ctrlPressed && e.getKeyCode() == KeyEvent.VK_P) {
            playListener.actionPerformed(null);
         } else if (ctrlPressed && e.getKeyCode() == KeyEvent.VK_S) {
            saveListener.actionPerformed(null);
         }
      }
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
                  log.error("Unable to paste content", ex);
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

   private class WaveFilter extends FileFilter {

      @Override
      public boolean accept(File file) {
         return file.isDirectory() || file.getName().endsWith("wav") || file.getName().endsWith("WAV");
      }

      @Override
      public String getDescription() {
         return MESSAGES.get("wav");
      }
   }
}
