package de.dl2sba.filderpower.wsprnet.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dl2sba.common.ProcessingException;
import de.dl2sba.filderpower.wsprnet.WsprNetQueryResult;
import de.dl2sba.filderpower.wsprnet.data.WsprNetSpot;

public class WsprNetConsumerConsole extends WsprNetSpotConsumer {

	protected static final Logger logger = LogManager.getLogger(WsprNetConsumerConsole.class.getName());

	@Override
	@SuppressWarnings("all")
	public void consume(WsprNetQueryResult data) {
		logger.traceEntry();
		for (WsprNetSpot aSpot : data.spots) {
			String msg = String.format("%tD %tT %-10s %-10s %-7s %5f %-3d", aSpot.date, aSpot.date, aSpot.callsign, aSpot.reporter, aSpot.distance + "km", aSpot.power, aSpot.azimuth);
			System.out.println(msg);
		}
		logger.traceExit();
	}

	@Override
	public void beginWork() throws ProcessingException {
		logger.traceEntry();
		logger.traceExit();
	}

	@Override
	public void endWork() throws ProcessingException {
		logger.traceEntry();
		beginWork();
		logger.traceExit();
	}
}
