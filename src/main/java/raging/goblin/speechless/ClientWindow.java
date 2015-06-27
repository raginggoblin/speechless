package raging.goblin.speechless;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import marytts.exceptions.MaryConfigurationException;

public class ClientWindow extends JFrame {

	private JTextField typingField;
	private JTextArea speakingArea;
	private Speeker speeker;

	public ClientWindow() throws MaryConfigurationException {
		initGui();
		// setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/calendar.png")));
		setTitle("Speechless");
		speeker = new Speeker();
	}

	public void setFocusOnTextField() {
		typingField.grabFocus();
	}

	private void initGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 400);
		speakingArea = new JTextArea();
		speakingArea.setEditable(false);
		getContentPane().add(speakingArea, BorderLayout.CENTER);
		initTypingField();
	}

	private void initTypingField() {
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
	}
}
