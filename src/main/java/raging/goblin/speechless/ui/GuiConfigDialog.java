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
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jnativehook.keyboard.NativeKeyEvent;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import lombok.Getter;
import raging.goblin.speechless.Messages;
import raging.goblin.speechless.UIProperties;

public class GuiConfigDialog extends JDialog {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final UIProperties PROPERTIES = UIProperties.getInstance();

   @Getter
   private boolean okPressed;
   @Getter
   private boolean settingsChanged;
   private int[] nativeHookKeyCodes = PROPERTIES.getNativeHookKeyCodes();

   private JCheckBox chckbxSplashScreenEnabled;
   private JCheckBox chckbxWelcomeScreenEnabled;
   private JCheckBox chckbxStartMinimized;
   private JLabel nativeHookLabel;

   public GuiConfigDialog(JFrame parent) {
      super(parent, MESSAGES.get("gui_config_title"), true);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setSize(450, 350);
      ScreenPositioner.centerOnScreen(this);
      initActionsPanel();
      initConfigPanel(parent);
   }

   private void initConfigPanel(JFrame parent) {
      JPanel configPanel = new JPanel();
      configPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
      getContentPane().add(configPanel, BorderLayout.CENTER);
      configPanel
            .setLayout(
                  new FormLayout(
                        new ColumnSpec[] { ColumnSpec.decode("default:grow"), ColumnSpec.decode("default:grow"),
                              ColumnSpec.decode("right:default"), ColumnSpec.decode("right:default"), ColumnSpec
                                    .decode("right:default") },
            new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  RowSpec.decode("default:grow"), }));

      StringSeparator splashScreenSeparator = new StringSeparator(MESSAGES.get("splash_screen"));
      configPanel.add(splashScreenSeparator, "1, 3, 5, 1");

      JLabel lblSplashScreenEnabled = new JLabel(MESSAGES.get("enabled"));
      configPanel.add(lblSplashScreenEnabled, "3, 5");

      chckbxSplashScreenEnabled = new JCheckBox();
      chckbxSplashScreenEnabled.setSelected(PROPERTIES.isSplashScreenEnabled());
      configPanel.add(chckbxSplashScreenEnabled, "4, 5");

      StringSeparator welcomeScreenSeparator = new StringSeparator(MESSAGES.get("welcome_screen"));
      configPanel.add(welcomeScreenSeparator, "1, 7, 5, 1");

      JLabel lblShowWelcomeScreen = new JLabel(MESSAGES.get("enabled"));
      configPanel.add(lblShowWelcomeScreen, "3, 9");

      chckbxWelcomeScreenEnabled = new JCheckBox();
      chckbxWelcomeScreenEnabled.setSelected(PROPERTIES.isWelcomeScreenEnabled());
      configPanel.add(chckbxWelcomeScreenEnabled, "4, 9");

      StringSeparator startMinimizedSeparator = new StringSeparator(MESSAGES.get("start_minimized"));
      configPanel.add(startMinimizedSeparator, "1, 11, 5, 1");

      JLabel lblStartMinized = new JLabel(MESSAGES.get("enabled"));
      configPanel.add(lblStartMinized, "3, 13");

      chckbxStartMinimized = new JCheckBox();
      chckbxStartMinimized.setSelected(PROPERTIES.isStartMinimized());
      configPanel.add(chckbxStartMinimized, "4, 13");

      StringSeparator nativeHookSeparator = new StringSeparator(MESSAGES.get("native_hook"));
      configPanel.add(nativeHookSeparator, "1, 15, 5, 1");

      nativeHookLabel = new JLabel(getNativeHookText(nativeHookKeyCodes));
      configPanel.add(nativeHookLabel, "2, 19");

      JButton btnRecordNativeHook = new JButton(Icon.getIcon("/icons/pencil.png"));
      configPanel.add(btnRecordNativeHook, "3, 19, 2, 1, fill, default");
      btnRecordNativeHook.addActionListener(e -> {
         RecordNativeHookDialog dialog = new RecordNativeHookDialog(parent);
         dialog.setVisible(true);
         if (dialog.isOkButtonPressed()) {
            nativeHookKeyCodes = dialog.getNativeHookKeyCodes();
            nativeHookLabel.setText(getNativeHookText(nativeHookKeyCodes));
         }
         dialog.dispose();
      });
   }

   private void initActionsPanel() {
      JPanel actionPanel = new JPanel();
      getContentPane().add(actionPanel, BorderLayout.SOUTH);
      FormLayout layout = new FormLayout(
            new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                  FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                  FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
                  FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                  ColumnSpec.decode("max(40dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
                  ColumnSpec.decode("max(5dlu;default)"), },
            new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  RowSpec.decode("max(5dlu;default)"), });
      actionPanel.setLayout(layout);

      JButton btnCancel = new JButton(MESSAGES.get("cancel"));
      btnCancel.addActionListener(e -> setVisible(false));
      actionPanel.add(btnCancel, "7, 1, 2, 1");

      JButton btnOk = new JButton(MESSAGES.get("ok"));
      btnOk.addActionListener(e -> {
         checkSettingsChanged();
         saveConfiguration();
         okPressed = true;
         setVisible(false);
      });
      actionPanel.add(btnOk, "11, 1, 2, 1");
   }

   private void saveConfiguration() {
      PROPERTIES.setSplashScreenEnabled(chckbxSplashScreenEnabled.isSelected());
      PROPERTIES.setWelcomeScreenEnabled(chckbxWelcomeScreenEnabled.isSelected());
      PROPERTIES.setStartMinimized(chckbxStartMinimized.isSelected());
      PROPERTIES.setNativeHookEnabled(nativeHookKeyCodes.length > 0);
      PROPERTIES.setNativeHookKeyCodes(nativeHookKeyCodes);
   }

   private void checkSettingsChanged() {
      settingsChanged = !Arrays.equals(PROPERTIES.getNativeHookKeyCodes(), nativeHookKeyCodes);
   }

   private String getNativeHookText(int[] nativeHookKeyCodes) {
      return Arrays.stream(nativeHookKeyCodes).mapToObj(kc -> NativeKeyEvent.getKeyText(kc))
            .collect(Collectors.joining(", "));
   }
}
