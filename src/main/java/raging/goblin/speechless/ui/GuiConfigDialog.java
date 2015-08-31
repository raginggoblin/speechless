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

import lombok.Getter;

import org.jnativehook.keyboard.NativeKeyEvent;

import raging.goblin.speechless.Messages;
import raging.goblin.speechless.UIProperties;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class GuiConfigDialog extends JDialog {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final UIProperties PROPERTIES = UIProperties.getInstance();

   @Getter
   private boolean okPressed;
   private int[] nativeHookKeyCodes = PROPERTIES.getNativeHookKeyCodes();

   private JCheckBox chckbxSplashScreenEnabled;
   private JCheckBox chckbxSystrayEnabled;
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
      configPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"),
            ColumnSpec.decode("default:grow"), ColumnSpec.decode("right:default"), ColumnSpec.decode("right:default"),
            ColumnSpec.decode("right:default") }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("default:grow"), }));

      StringSeparator spashScreenSeparator = new StringSeparator(MESSAGES.get("splash_screen"));
      configPanel.add(spashScreenSeparator, "1, 3, 5, 1");

      JLabel lblSplashScreenEnabled = new JLabel(MESSAGES.get("enabled"));
      configPanel.add(lblSplashScreenEnabled, "3, 5");

      chckbxSplashScreenEnabled = new JCheckBox();
      chckbxSplashScreenEnabled.setSelected(PROPERTIES.isSplashScreenEnabled());
      configPanel.add(chckbxSplashScreenEnabled, "4, 5");

      StringSeparator systraySeparator = new StringSeparator(MESSAGES.get("system_tray_icon"));
      configPanel.add(systraySeparator, "1, 7, 5, 1");

      JLabel lblSystrayEnabled = new JLabel(MESSAGES.get("enabled"));
      configPanel.add(lblSystrayEnabled, "3, 9");

      chckbxSystrayEnabled = new JCheckBox();
      chckbxSystrayEnabled.setSelected(PROPERTIES.isSystrayEnabled());
      configPanel.add(chckbxSystrayEnabled, "4, 9");

      StringSeparator nativeHookSeparator = new StringSeparator(MESSAGES.get("native_hook"));
      configPanel.add(nativeHookSeparator, "1, 11, 5, 1");

      nativeHookLabel = new JLabel(getNativeHookText(nativeHookKeyCodes));
      configPanel.add(nativeHookLabel, "2, 15");

      JButton btnRecordNativeHook = new JButton(Icon.getIcon("/icons/pencil.png"));
      configPanel.add(btnRecordNativeHook, "3, 15, 2, 1, fill, default");
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
      FormLayout layout = new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("max(40dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("max(5dlu;default)"), }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(5dlu;default)"), });
      actionPanel.setLayout(layout);

      JButton btnCancel = new JButton(MESSAGES.get("cancel"));
      btnCancel.addActionListener(e -> setVisible(false));
      actionPanel.add(btnCancel, "7, 1, 2, 1");

      JButton btnOk = new JButton(MESSAGES.get("ok"));
      btnOk.addActionListener(e -> {
         saveConfiguration();
         okPressed = true;
         setVisible(false);
      });
      actionPanel.add(btnOk, "11, 1, 2, 1");
   }

   private void saveConfiguration() {
      PROPERTIES.setSplashScreenEnabled(chckbxSplashScreenEnabled.isSelected());
      PROPERTIES.setSystrayEnabled(chckbxSystrayEnabled.isSelected());
      PROPERTIES.setNativeHookEnabled(nativeHookKeyCodes.length > 0);
      PROPERTIES.setNativeHookKeyCodes(nativeHookKeyCodes);
   }

   private String getNativeHookText(int[] nativeHookKeyCodes) {
      return Arrays.stream(nativeHookKeyCodes).mapToObj(kc -> NativeKeyEvent.getKeyText(kc))
            .collect(Collectors.joining(", "));
   }
}
