package raging.goblin.speechless;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import marytts.exceptions.MaryConfigurationException;

import org.apache.log4j.Logger;

public class Application {

	private static final Logger LOG = Logger.getLogger(Application.class);

	public static void main(String[] args) {
		loadLaf();
		try {
			ClientWindow clientWindow = new ClientWindow();
			centerFrame(clientWindow);
			clientWindow.setVisible(true);
			clientWindow.setFocusOnTextField();
		} catch (MaryConfigurationException e) {
			LOG.error("Unable to start Speeker", e);
			JOptionPane.showMessageDialog(null, "Initialization error, could not start player", "Initialization error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void centerFrame(JFrame frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dimension.width / 2 - frame.getSize().width / 2, dimension.height / 2 - frame.getSize().height
				/ 2);
	}

	private static void loadLaf() {
		try {
			if (UIManager.getSystemLookAndFeelClassName().contains("Metal")) {
				setNimbusLaf();
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				LOG.debug("Setting system look and feel");
			}
		} catch (Exception e) {
			LOG.error("Could not set system look and feel");
			LOG.debug(e.getMessage(), e);
		}
	}

	private static void setNimbusLaf() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				LOG.debug("Setting Nimbus look and feel");
				break;
			}
		}
	}
}
