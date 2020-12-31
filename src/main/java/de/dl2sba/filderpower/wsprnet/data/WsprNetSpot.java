package de.dl2sba.filderpower.wsprnet.data;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dl2sba.filderpower.wsprnet.helpers.WsprNetBandMapper;
import de.dl2sba.filderpower.wsprnet.helpers.WsprNetQRAHelper;

public class WsprNetSpot {
	protected static final Logger logger = LogManager.getLogger(WsprNetSpot.class.getName());

	public long spotnum; // NOSONAR
	public Date date; // NOSONAR
	public String reporter; // NOSONAR
	public String reporterGrid; // NOSONAR
	public int dB; // NOSONAR
	public int frequency; // NOSONAR
	public String callsign; // NOSONAR
	public String grid; // NOSONAR
	public double power; // NOSONAR // power in dBm
	public int drift; // NOSONAR
	public int distance; // NOSONAR
	public int azimuth; // NOSONAR
	public String band; // NOSONAR
	public int bandId; // NOSONAR

	public WsprNetSpot(WsprNetBasicSpot basicSpot) {
		logger.traceEntry();
		this.frequency = (int) (basicSpot.MHz * 1000000.0);
		this.azimuth = basicSpot.azimuth;
		this.band = WsprNetBandMapper.mapFrequencyToBand(this.frequency);
		this.bandId = basicSpot.Band;
		this.callsign = basicSpot.CallSign;
		this.date = new Date(basicSpot.Date * 1000l);
		this.dB = basicSpot.dB;
		this.distance = basicSpot.distance;
		this.drift = basicSpot.Drift;
		this.grid = WsprNetQRAHelper.correctLocator(basicSpot.Grid);
		this.power = basicSpot.Power;
		this.reporter = basicSpot.Reporter;
		this.reporterGrid = WsprNetQRAHelper.correctLocator(basicSpot.ReporterGrid);
		this.spotnum = basicSpot.Spotnum;
		logger.traceExit();
	}

	@Override
	public String toString() {
		return "WsprNetSpot [spotnum=" + spotnum + ", date=" + date + ", reporter=" + reporter + ", reporterGrid=" + reporterGrid + ", dB=" + dB + ", frequency=" + frequency + ", callsign=" + callsign + ", grid=" + grid + ", power=" + power + ", drift=" + drift + ", distance=" + distance + ", azimuth=" + azimuth + ", band=" + band + ", bandId=" + bandId + "]";
	}
}
