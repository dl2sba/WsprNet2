package de.dl2sba.filderpower.wsprnet.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WsprNetBandMapper {

	protected static final Logger logger = LogManager.getLogger(WsprNetProperties.class.getName());

	private WsprNetBandMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static String mapFrequencyToBand(int frequency) {
		String rc;

		if (frequency > 144000000) {
			rc = "2m";
		} else if (frequency > 50000000) {
			rc = "6m";
		} else if (frequency > 28000000) {
			rc = "10m";
		} else if (frequency > 24000000) {
			rc = "12m";
		} else if (frequency > 21000000) {
			rc = "15m";
		} else if (frequency > 18000000) {
			rc = "17m";
		} else if (frequency > 14000000) {
			rc = "20m";
		} else if (frequency > 10100000) {
			rc = "30m";
		} else if (frequency > 7000000) {
			rc = "40m";
		} else if (frequency > 5000000) {
			rc = "60m";
		} else if (frequency > 3500000) {
			rc = "80m";
		} else if (frequency > 1000000) {
			rc = "160m";
		} else if (frequency > 130000) {
			rc = "LW";
		} else {
			rc = "MW";
		}
		return rc;
	}
}
