package de.dl2sba.filderpower.wsprnet.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WsprNetCallsign {
	protected static final Logger logger = LogManager.getLogger(WsprNetCallsign.class.getName());

	public String callSign; // NOSONAR
	public String band; // NOSONAR
	public long lastSpotNumber; // NOSONAR
	public int numSpots; // NOSONAR
	public int nodeId; // NOSONAR
	public int sensorId; // NOSONAR

	public static WsprNetCallsign buildFromString(String inp) {
		logger.traceEntry();
		WsprNetCallsign wncs = null;

		String[] vals = inp.split(",");
		if (vals.length == 4) {
			wncs = new WsprNetCallsign();
			wncs.callSign = vals[0].trim().toUpperCase();
			wncs.band = vals[1].trim();
			wncs.nodeId = Integer.parseInt(vals[2]);
			wncs.sensorId = Integer.parseInt(vals[3]);
			wncs.lastSpotNumber = -60;
			wncs.numSpots = 0;
		}
		logger.traceEntry();
		return wncs;
	}

	@Override
	public String toString() {
		return "WsprNetCallsign [callSign=" + callSign + ", band=" + band + ", lastSpotNumber=" + lastSpotNumber + ", numSpots=" + numSpots + ", nodeId=" + nodeId + ", sensorId=" + sensorId + "]";
	}
}
