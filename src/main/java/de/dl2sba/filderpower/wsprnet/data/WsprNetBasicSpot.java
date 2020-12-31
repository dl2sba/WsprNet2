package de.dl2sba.filderpower.wsprnet.data;

public class WsprNetBasicSpot {
	public long Spotnum;// NOSONAR
	public int Date;// NOSONAR
	public String Reporter;// NOSONAR
	public String ReporterGrid;// NOSONAR
	public int dB;// NOSONAR
	public double MHz;// NOSONAR
	public String CallSign; // NOSONAR
	public String Grid;// NOSONAR
	public double Power;// NOSONAR // power in dBm
	public int Drift;// NOSONAR
	public int distance;// NOSONAR
	public int azimuth; // NOSONAR
	public int Band; // NOSONAR
	public String version;// NOSONAR
	public int code;// NOSONAR

	@Override
	public String toString() {
		return "WsprSpot [Spotnum=" + Spotnum + ", Date=" + Date + ", Reporter=" + Reporter + ", ReporterGrid=" + ReporterGrid + ", dB=" + dB + ", MHz=" + MHz + ", Callsign=" + CallSign + ", Grid=" + Grid + ", Power=" + Power + ", Drift=" + Drift + ", distance=" + distance + ", azimuth=" + azimuth + ", Band=" + Band + "]";
	}

}
