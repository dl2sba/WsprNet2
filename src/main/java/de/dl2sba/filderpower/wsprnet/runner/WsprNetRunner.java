package de.dl2sba.filderpower.wsprnet.runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dl2sba.filderpower.wsprnet.WrspNetReader;
import de.dl2sba.filderpower.wsprnet.helpers.WsprNetProperties;

public class WsprNetRunner {
	protected static final Logger logger = LogManager.getLogger(WsprNetRunner.class.getName());
	protected static final WsprNetProperties props = WsprNetProperties.getSingleton();
	protected boolean endAll = false;

	/**
	 * 
	 */
	public WsprNetRunner() {
		logger.traceEntry();

		showManifestInfo();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutdown hook ran!");
				endAll = true;
			}
		});

		// time between wakeups in s
		long sleepTime = props.getLong("Runner.sleep", 1);
		long interCallsign = props.getLong("Runner.interCallsign", 10);
		long interval = props.getLong("Runner.intervall", 60);

		logger.info("Sleep between time checks....{}s", sleepTime);
		logger.info("Sleep between call queries...{}s", interCallsign);
		logger.info("Sleep between group  polls...{}s", interval);

		sleepTime *= 1000;
		interval *= 1000;
		interCallsign *= 1000;

		WrspNetReader instance = new WrspNetReader(interCallsign);
		long nextRun = 0;
		boolean run = true;

		while (run) {
			long currentTime = System.currentTimeMillis();
			if (currentTime > nextRun) {
				instance.run();
				nextRun = currentTime + interval;
			}

			// sleep one sec
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.catching(e);
				run = false;
				Thread.currentThread().interrupt();
			}
		}
		logger.info("Main instance ended");
		logger.traceExit();
	}

	private void showManifestInfo() {
		logger.traceEntry();
		logger.info("***************************************************************");
		logger.info("Application information:");
		Attributes attributes = getAttributesFromManifest();
		var entries = attributes.entrySet();
		var it = entries.iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Object> pair = it.next();
			logger.info("   {} : {}", pair.getKey(), pair.getValue());
		}
		logger.info("...............................................................");
		logger.traceExit();
	}

	
	/**
	 * 
	 * @return
	 */
	Attributes getAttributesFromManifest() {
		Attributes rc = null;
		InputStream manifestStream = getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
		if (manifestStream != null) {
			Manifest manifest;
			try {
				manifest = new Manifest(manifestStream);
				rc = manifest.getMainAttributes();
			} catch (IOException e) {
				logger.catching(e);
				rc = new Attributes();
			}
		} else {
			rc = new Attributes();
		}
		return rc;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		logger.traceEntry();
		new WsprNetRunner();
		System.exit(0);
	}
}
