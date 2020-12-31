package de.dl2sba.filderpower.wsprnet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import de.dl2sba.common.ProcessingException;
import de.dl2sba.filderpower.wsprnet.consumer.WsprNetSpotConsumer;
import de.dl2sba.filderpower.wsprnet.data.WsprNetBasicSpot;
import de.dl2sba.filderpower.wsprnet.data.WsprNetCallsign;
import de.dl2sba.filderpower.wsprnet.data.WsprNetSpot;
import de.dl2sba.filderpower.wsprnet.data.WsprNetUser;
import de.dl2sba.filderpower.wsprnet.helpers.WsprNetProperties;

/**
 * https://github.com/google/gson
 * https://github.com/google/gson/blob/master/UserGuide.md
 * 
 * @author dietmar
 *
 */
public class WrspNetReader {
	protected static final Logger logger = LogManager.getLogger(WrspNetReader.class.getName());
	protected static final WsprNetProperties props = WsprNetProperties.getSingleton();


	private CookieStore httpCookieStore = null;
	private WsprNetSession httpSession = null;
	private final List<WsprNetCallsign> callSignsToQuery = new ArrayList<>();
	private final List<WsprNetSpotConsumer> spotConsumers = new ArrayList<>();

	private long sleepBetweenCallsignQueries = 10000;

	private static NumberFormat formatFrequency = null;
	public static NumberFormat getMemoryFormat() {
		if (formatFrequency == null) {
			formatFrequency = NumberFormat.getIntegerInstance();
			formatFrequency.setGroupingUsed(true);
			formatFrequency.setMaximumFractionDigits(0);
			formatFrequency.setMinimumFractionDigits(0);
			formatFrequency.setMaximumIntegerDigits(12);
			formatFrequency.setMinimumIntegerDigits(1);
		}
		return formatFrequency;
	}

	public WrspNetReader(long sbcq) {
		logger.traceEntry();
		this.sleepBetweenCallsignQueries = sbcq;
		prepareCallList();
		prepareConsumers();
		logger.traceExit();
	}

	private void prepareCallList() {
		logger.traceEntry();
		Enumeration<Object> x = props.createProperties("WsprNet.callsigns.", true).elements();

		while (x.hasMoreElements()) {
			String val = (String) x.nextElement();
			WsprNetCallsign wncs = WsprNetCallsign.buildFromString(val);
			if (wncs != null) {
				this.callSignsToQuery.add(wncs);
				logger.info("Querying callsign [{}]", wncs);
			} else {
				logger.error("Wrong value [{}] in callsign list", val);
			}
		}
		logger.traceExit();
	}

	private void prepareConsumers() {
		logger.traceEntry();
		Enumeration<Object> x = props.createProperties("WsprNet.SpotConsumer.", true).elements();

		while (x.hasMoreElements()) {
			String consumerClassname = (String) x.nextElement();

			try {
				Object newConsumerObject = Class.forName(consumerClassname).getConstructor().newInstance();
				if (newConsumerObject instanceof WsprNetSpotConsumer) {
					WsprNetSpotConsumer newConsumer = (WsprNetSpotConsumer) newConsumerObject;
					spotConsumers.add(newConsumer);
					logger.info("Added consumer of class [{}]", consumerClassname);
				} else {
					logger.error("[{}] is not of type [{}]", consumerClassname, WsprNetSpotConsumer.class.getName());
				}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				logger.catching(e);
			}

		}
		logger.traceExit();

	}

	/**
	 * 
	 * @param call
	 * @param band
	 * @param lastSpot
	 * @return
	 */
	public WsprNetQueryResult queryWsprNet(WsprNetCallsign data) {
		logger.traceEntry();
		List<WsprNetSpot> spots = new ArrayList<>();
		WsprNetQueryResult rc = new WsprNetQueryResult();
		rc.spots = spots;
		rc.lastSpotNumber = data.lastSpotNumber;
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			URIBuilder uriB;
			uriB = new URIBuilder(props.getProperty("WsprNet.uri.data")).addParameter("band", data.band).addParameter("callsign", data.callSign);
			if (data.lastSpotNumber > 0) {
				// starting with the next free spot number
				uriB = uriB.addParameter("spotnum_start", "" + (data.lastSpotNumber + 1));
			} else {
				uriB = uriB.addParameter("minutes", "" + (-1 * data.lastSpotNumber));
			}
			URI uri = uriB.build();
			HttpPost httpPost = new HttpPost(uri);
			// Create local HTTP context
			HttpClientContext localContext = HttpClientContext.create();

			// Bind custom cookie store to the local context
			localContext.setCookieStore(httpCookieStore);

			logger.debug("Executing request [{}]", httpPost.getRequestLine());

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				@Override
				public String handleResponse(final HttpResponse response) throws IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};

			String responseBody = httpclient.execute(httpPost, responseHandler, localContext);
			logger.debug("response [{}]", responseBody);
			WsprNetBasicSpot[] basicSpots = new Gson().fromJson(responseBody, WsprNetBasicSpot[].class);

			for (WsprNetBasicSpot spot : basicSpots) {
				WsprNetSpot newSpot = new WsprNetSpot(spot);
				rc.spots.add(newSpot);
				if (newSpot.spotnum > rc.lastSpotNumber) {
					rc.lastSpotNumber = newSpot.spotnum;
				}
				logger.debug(newSpot);
			}
			logger.debug("{} spots read. last spot num {}", rc.spots.size(), rc.lastSpotNumber);

		} catch (URISyntaxException | IOException e) {
			logger.catching(e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.catching(e);
			}
		}
		logger.traceExit();
		return rc;
	}

	/**
	 * @throws ProcessingException
	 * 
	 */
	public void login() throws ProcessingException {
		logger.traceEntry();
		CloseableHttpClient httpclient = HttpClients.createDefault();

		logger.info("Logging in ...");

		WsprNetUser user = new WsprNetUser(props.getProperty("WsprNet.user"), props.getProperty("WsprNet.password"));
		Gson gson = new Gson();
		String logonJSON = gson.toJson(user);
		URIBuilder uriB;
		try {
			httpCookieStore = new BasicCookieStore();

			uriB = new URIBuilder(props.getProperty("WsprNet.uri.login"));
			URI uri = uriB.build();

			HttpPost httpPost = new HttpPost(uri);
			StringEntity se = new StringEntity(logonJSON, ContentType.create("application/json"));
			httpPost.setEntity(se);

			// Create local HTTP context
			HttpClientContext localContext = HttpClientContext.create();

			// Bind custom cookie store to the local context
			localContext.setCookieStore(httpCookieStore);

			logger.debug("Executing request [{}]", httpPost.getRequestLine());

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				@Override
				public String handleResponse(final HttpResponse response) throws IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};

			String responseBody = httpclient.execute(httpPost, responseHandler, localContext);

			httpSession = new Gson().fromJson(responseBody, WsprNetSession.class);

			logger.debug("cookies {}", localContext.getCookieStore().toString());
			logger.debug("response [{}]", responseBody);
			logger.debug(localContext.getCookieStore().toString());
			logger.info("Logged in");
			logger.info("SessionName  {}", httpSession.session_name);
			logger.info("SessionID    {}", httpSession.sessid);
			logger.info("SessionToken {}", httpSession.token);
		} catch (URISyntaxException | IOException e) {
			throw new ProcessingException(e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.catching(e);
			}
		}
		logger.traceExit();
	}

	/**
	 * @throws ProcessingException
	 * 
	 */
	public void logout() throws ProcessingException {
		logger.traceEntry();

		logger.info("Logging out ...");
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			HttpPost httpPost = new HttpPost(props.getProperty("WsprNet.uri.logoff"));
			httpPost.setEntity(new StringEntity("", ContentType.create("application/json")));
			httpPost.addHeader("X-CSRF-Token", httpSession.token);
			httpPost.addHeader("Content-Type", "application/json");

			// Create local HTTP context
			HttpClientContext localContext = HttpClientContext.create();

			// Bind custom cookie store to the local context
			localContext.setCookieStore(httpCookieStore);

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(final HttpResponse response) throws IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};

			logger.debug("Executing request [{}]", httpPost.getRequestLine());

			String responseBody = httpclient.execute(httpPost, responseHandler, localContext);
			logger.debug("response [{}]", responseBody);
			logger.info("Logged out");
			httpCookieStore = null;

		} catch (IOException e) {
			throw new ProcessingException(e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.catching(e);
			}
		}
		logger.traceExit();
	}

	public void run() {
		logger.traceEntry();
		try {
			logger.info("<<<<<<<<<<<<<<<<<<<<<<< BEGIN WORK");
			// login into WSPRNET
			login();

			boolean firstDone = false;

			/// tell all consumers we are now starting with work
			for (WsprNetSpotConsumer consumer : this.spotConsumers) {
				consumer.beginWork();
			}

			// now for each callsign to query
			for (WsprNetCallsign callSign : this.callSignsToQuery) {

				// if not first call
				if (firstDone) {
					sleepBetweenCallsignQueries();
				}
				// at least first query done?
				firstDone = true;

				// get data from WSPRNET
				WsprNetQueryResult data = queryWsprNet(callSign);
				data.call = callSign;

				int newSpots = data.spots.size();

				// transfer statistics to callsign
				callSign.lastSpotNumber = data.lastSpotNumber;
				callSign.numSpots += newSpots;

				logger.info("{} - total={} - new={} - id={}", callSign.callSign, callSign.numSpots, newSpots, callSign.lastSpotNumber);

				for (WsprNetSpotConsumer consumer : this.spotConsumers) {
					consumer.consume(data);
				}
			}
		} catch (ProcessingException e) {
			logger.catching(e);
		} finally {
			try {
				// at the end tell all consumers we are done now
				for (WsprNetSpotConsumer consumer : this.spotConsumers) {
					consumer.endWork();
				}
				logout();

			} catch (ProcessingException e) {
				logger.catching(e);
			}
		}
		logMemoryInfo();
		logger.info(">>>>>>>>>>>>>>>>>>>>>>> END WORK");
		logger.traceExit();
	}

	private void sleepBetweenCallsignQueries() {
		logger.traceEntry();
		// sleep a while
		try {
			Thread.sleep(this.sleepBetweenCallsignQueries);
		} catch (InterruptedException e) {
			logger.catching(e);
			Thread.currentThread().interrupt();
		}
		logger.traceExit();
	}

	private void logMemoryInfo() {
		logger.traceEntry();
		// Get the Java runtime
		Runtime runtime = Runtime.getRuntime();
		// Run the garbage collector
		runtime.gc();
		// Calculate the used memory
		long memory = runtime.totalMemory() - runtime.freeMemory();
		logger.info("Memory statistics");
		logger.info("   total  {} Bytes", getMemoryFormat().format(runtime.totalMemory()));
		logger.info("   free   {} Bytes", getMemoryFormat().format(runtime.freeMemory()));
		logger.info("   used   {} Bytes", getMemoryFormat().format(memory));
		logger.traceExit();
	}
}
