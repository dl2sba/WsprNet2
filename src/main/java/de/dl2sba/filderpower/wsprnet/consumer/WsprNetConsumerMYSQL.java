package de.dl2sba.filderpower.wsprnet.consumer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dl2sba.common.ProcessingException;
import de.dl2sba.filderpower.wsprnet.WsprNetQueryResult;
import de.dl2sba.filderpower.wsprnet.data.WsprNetSpot;
import de.dl2sba.filderpower.wsprnet.helpers.WsprNetProperties;

public class WsprNetConsumerMYSQL extends WsprNetSpotConsumer {

	protected static final Logger logger = LogManager.getLogger(WsprNetConsumerMYSQL.class.getName());
	protected static final WsprNetProperties props = WsprNetProperties.getSingleton();

	private Connection sqlConnection = null;

	@Override
	public void beginWork() throws ProcessingException {
		logger.traceEntry();
		try {
			if (sqlConnection != null) {
				endWork();
			}
			logger.info("Connecting to [{}]", props.getProperty("SQL.database.user"));
			String connString = String.format("%s?user=%s&password=%s", props.getProperty("SQL.database.uri"), props.getProperty("SQL.database.user"), props.getProperty("SQL.database.password"));
			Properties connProps = new Properties();
			connProps.put("serverTimezone", TimeZone.getDefault().getID());
			sqlConnection = DriverManager.getConnection(connString, connProps);
			sqlConnection.setAutoCommit(true);
			logger.info("Connected to database server {}", props.getProperty("SQL.database.uri"));
		} catch (SQLException e) {
			throw new ProcessingException(e);
		}
		logger.traceExit();

	}

	@Override
	public void endWork() throws ProcessingException {
		logger.traceEntry();
		if (sqlConnection != null) {
			try {
				sqlConnection.close();
				sqlConnection = null;
				logger.info("Disconnected from database");
			} catch (SQLException e) {
				throw new ProcessingException(e);
			} finally {
				sqlConnection = null;
			}
		} else {
			logger.info("No work done so far");
		}
		logger.traceExit();
	}

	public WsprNetConsumerMYSQL() {
		logger.traceEntry();
		logger.traceExit();
	}

	private int inserOneSpot(WsprNetSpot aSpot) throws ProcessingException {
		logger.traceEntry();
		int rc = 0;

		PreparedStatement prepStmt = null;

		try {
			String statement = "INSERT INTO spots(time, transmitter, transmitter_loc, transmitter_W, transmitter_dBm, reporter, reporter_loc, distance, frequency, band, snr, drift, bearing) ";
			statement += "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			prepStmt = sqlConnection.prepareStatement(statement);
			prepStmt.setTimestamp(1, new java.sql.Timestamp(aSpot.date.getTime()));
			prepStmt.setString(2, aSpot.callsign);
			prepStmt.setString(3, aSpot.grid);
			prepStmt.setDouble(4, Math.pow(10, aSpot.power / 10) * 0.001); // PWR
			prepStmt.setDouble(5, aSpot.power); // dBm
			prepStmt.setString(6, aSpot.reporter);
			prepStmt.setString(7, aSpot.reporterGrid);
			prepStmt.setInt(8, aSpot.distance);
			prepStmt.setInt(9, aSpot.frequency);
			prepStmt.setString(10, aSpot.band);
			prepStmt.setInt(11, aSpot.dB); // SNR
			prepStmt.setInt(12, aSpot.drift);
			prepStmt.setInt(13, aSpot.azimuth);
			rc = prepStmt.executeUpdate();

		} catch (SQLException ex) {
			if (1062 == ex.getErrorCode()) {
				logger.info("Duplicate entry for {}, {}, {}, {}", aSpot.date, aSpot.callsign, aSpot.reporter, aSpot.frequency);
			} else {
				// handle any errors
				logger.error("SQLException: {}", ex.getMessage());
				logger.error("SQLState:     {}", ex.getSQLState());
				logger.error("VendorError:  {}", ex.getErrorCode());
				throw new ProcessingException(ex);
			}
		} finally {
			try {
				if (prepStmt != null) {
					prepStmt.close();
				}
			} catch (SQLException sqlEx) {
				logger.catching(sqlEx);
			} // ignore
			prepStmt = null;
		}
		logger.traceExit(rc);
		return rc;
	}

	@Override
	public void consume(WsprNetQueryResult data) throws ProcessingException {
		logger.traceEntry();
		int numInserted = 0;
		if (!data.spots.isEmpty()) {
			for (WsprNetSpot aSpot : data.spots) {
				numInserted += inserOneSpot(aSpot);
			}
			logger.info("{} out of {} spots inserted for {}", numInserted, data.spots.size(), data.spots.get(0).callsign);
		}
		logger.traceExit();
	}

}
