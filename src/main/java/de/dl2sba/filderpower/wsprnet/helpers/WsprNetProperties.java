package de.dl2sba.filderpower.wsprnet.helpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dl2sba.common.TypedProperties;

public class WsprNetProperties extends TypedProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6546546551068798406L;
	private static WsprNetProperties singleton = null;

	protected static final Logger logger = LogManager.getLogger(WsprNetProperties.class.getName());

	private void load(String propertiesFileName, Properties defaultProperties) {
		logger.traceEntry("propertiesFileName={}", propertiesFileName);

		InputStream is;
		try {

			is = new FileInputStream(propertiesFileName);
			load(is);
			is.close();
			logger.info("Loaded properties from [{}].", propertiesFileName);
		} catch (FileNotFoundException e) {
			putAll(defaultProperties);
			logger.info("File [{}] not found, using default.", propertiesFileName);
		} catch (IOException e) {
			logger.info("File [{}] not readable, using default.", propertiesFileName);
		}

		logger.traceExit();
	}

	/**
	 * Return the one and only instance of the config object
	 * 
	 * @return the only instance of this class
	 */
	public static synchronized WsprNetProperties getSingleton() {
		if (singleton == null) {
			singleton = new WsprNetProperties();
			// check if environment var is set
			String propertyFile = System.getProperty("WsprNet.properties");

			/// set?
			if (propertyFile == null) {
				// no use default in code-tree
				propertyFile = "./WsprNet.properties";
			}
			logger.info("Loading properties from [{}]", propertyFile);

			// load properties
			singleton.load(propertyFile, new Properties());
		}
		return singleton;
	}
}
