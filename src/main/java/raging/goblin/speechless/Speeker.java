package raging.goblin.speechless;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;

import org.apache.log4j.Logger;

public class Speeker {

	private static final Logger LOG = Logger.getLogger(ClientWindow.class);

	private BlockingQueue<AudioPlayer> speechesQueue = new ArrayBlockingQueue<>(100);
	private volatile boolean running = false;
	private MaryInterface marytts;

	public Speeker() throws MaryConfigurationException {
		initMaryTTS();
		initSpeeking();
	}

	public void stop() {
		running = false;
	}

	public boolean speek(String toSpeek) {
		try {
			AudioInputStream audio = marytts.generateAudio(toSpeek);
			AudioPlayer player = new AudioPlayer(audio);
			return speechesQueue.offer(player);
		} catch (SynthesisException e) {
			LOG.error("Unable to speek", e);
		}
		return false;
	}

	private void initMaryTTS() throws MaryConfigurationException {
		marytts = new LocalMaryInterface();
		Set<String> voices = marytts.getAvailableVoices();
		marytts.setVoice(voices.iterator().next());
	}

	private void initSpeeking() {
		Thread dequeueThread = new Thread("Speeking thread") {

			@Override
			public void run() {
				while (running) {
					try {
						AudioPlayer speech = speechesQueue.take();
						speech.start();
						speech.join();
					} catch (InterruptedException e) {
						LOG.error("Interrupted speeking thread", e);
					}
				}
			}
		};
		running = true;
		dequeueThread.start();
	}
}
