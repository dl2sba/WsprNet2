package de.dl2sba.filderpower.wsprnet.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import de.dl2sba.common.ProcessingException;
import de.dl2sba.filderpower.wsprnet.WsprNetQueryResult;
import de.dl2sba.filderpower.wsprnet.helpers.WsprNetProperties;

public class WsprNetConsumerMQTT extends WsprNetSpotConsumer implements MqttCallback {

	protected static final Logger logger = LogManager.getLogger(WsprNetConsumerMQTT.class.getName());
	protected final WsprNetProperties props = WsprNetProperties.getSingleton();
	private MemoryPersistence mqttPersistence = null;
	private MqttClient mqttClient = null;

	public WsprNetConsumerMQTT() {
		logger.traceEntry();
		logger.traceExit();
	}

	@Override
	public void beginWork() throws ProcessingException {
		logger.traceEntry();
		String clientId = MqttClient.generateClientId();
		mqttPersistence = new MemoryPersistence();

		try {
			logger.info("Connecting to broker {}", this.props.getProperty("MQTT.Server"));
			this.mqttClient = new MqttClient(this.props.getProperty("MQTT.Server"), clientId, mqttPersistence);
			this.mqttClient.setCallback(this);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setPassword(this.props.getProperty("MQTT.Password").toCharArray());
			connOpts.setUserName(this.props.getProperty("MQTT.User"));
			this.mqttClient.connect(connOpts);
			logger.info("Connected to broker with client {}", clientId);
		} catch (MqttException e) {
			throw new ProcessingException(e);
		}
		logger.traceExit();
	}

	@Override
	public void connectionLost(Throwable arg0) {
		logger.traceEntry();
		logger.error(arg0);
		logger.traceExit();
	}

	@Override
	public void consume(WsprNetQueryResult data) throws ProcessingException {
		logger.traceEntry();
		int sz = data.spots.size();
		if (sz > 0) {
			try {
				String topicPattern = this.props.getProperty("MQTT.Topic");

				int qos = this.props.getInteger("MQTT.qos", 1);
				String topic = String.format(topicPattern, data.call.nodeId, data.call.sensorId);
				String val = Double.toString(sz);

				this.mqttClient.publish(topic, val.getBytes(), qos, false);
				logger.info("Published {} to {}/{}", val, data.call.nodeId, data.call.sensorId);
			} catch (MqttException e) {
				throw new ProcessingException(e);
			}
		}
		logger.traceExit();

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		logger.traceEntry();
		logger.traceExit();
	}

	@Override
	public void endWork() throws ProcessingException {
		logger.traceEntry();
		if (this.mqttClient != null) {
			try {
				this.mqttClient.disconnect();
				this.mqttClient = null;
				this.mqttPersistence = null;
				logger.info("Disconnected from broker");
			} catch (MqttException e) {
				throw new ProcessingException(e);
			}
		} else {
			logger.info("No work started.");
		}
		logger.traceExit();
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		logger.traceEntry();
		logger.traceExit();
	}

}
