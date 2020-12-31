package de.dl2sba.filderpower.wsprnet.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WsprNetQRAHelper {
	protected static final Logger logger = LogManager.getLogger(WsprNetQRAHelper.class.getName());

	private WsprNetQRAHelper() {
		throw new IllegalStateException("Utility class");
	}

	public static String correctLocator(String in) {
		logger.traceEntry(in);

		int l = in.length();
		if (l < 2) {
			in = "JJ";
		}
		if (l < 3) {
			in += "45";
		}

		if (l < 5) {
			in += "JJ";
		}

		logger.traceExit(in);
		return in.toUpperCase();
	}
}
