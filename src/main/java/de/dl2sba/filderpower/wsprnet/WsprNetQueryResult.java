package de.dl2sba.filderpower.wsprnet;

import java.util.List;

import de.dl2sba.filderpower.wsprnet.data.WsprNetCallsign;
import de.dl2sba.filderpower.wsprnet.data.WsprNetSpot;

public class WsprNetQueryResult {
	@SuppressWarnings("all")
	public WsprNetCallsign call;

	@SuppressWarnings("all")
	public List<WsprNetSpot> spots;

	@SuppressWarnings("all")
	public long lastSpotNumber;
}
