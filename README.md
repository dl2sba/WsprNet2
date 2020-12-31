# WsprNet
Java client to pull WSPRNET.ORG spots

## Basic function
Polls on a regular basis the JSON interface of WSPRNET.ORG and publishes the pulled spots to the configured consumers.

## Polling
This code is expected to run forever. The main loop is defined like this:

- The main loops sleeps the number of seconds defined in WsprNet.properties(Runner.sleep)
- For all callsigns
	- it executes the first poll for a call
	- it sleeps for the number of seconds defined in _WsprNet.properties(Runner.interCallsign)_
- After all callsigns are polled, the application sleeps for _WsprNet.properties(Runner.intervall)_ seconds

## Defining callsigns
The callsigns to be polled are defined in the properties collection _WsprNet.properties(WsprNet.callsigns.*)_
 
Each callsign entry is defined like this:

	WsprNet.callsigns.<id>=<call>,<band>,<nodeid>,<sensorid>
	<id>		unique identifier - easiest way use increasing numbers
	<call>		the call to pull spots for
	<band>		the band to pull spots for the call. Check https://github.com/garymcm/wsprnet_api for definition. !case-sensitive!
	<nodeid>	used by the WsprNetConsumerMQTT
	<sensorid>	used by the WsprNetConsumerMQTT

## WSPRNET.ORG access
To pull spots from WSPRNET.ORG you need a named account. After registering an account put the credentials into these properties in *WsprNet.properties*:

	WsprNet.password=PASSWORD
	WsprNet.user=USERID
	
The properties for the URIs can be changed in *WsprNet.properties*:

	WsprNet.uri.data=http\://wsprnet.org/drupal/wsprnet/spots/json
	WsprNet.uri.login=http\://wsprnet.org/drupal/rest/user/login
	WsprNet.uri.logoff=http\://wsprnet.org/drupal/rest/user/logout.json

## Dependencies
The following libs (jar-files) are needed to get it up and running:

- ApacheLog4J
	- log4j-api-2.11.1.jar
	- log4j-core-2.11.1.jar
	- log4j-1.2-api-2.11.1.jar

- ApacheHttpClient
	- commons-logging-1.2.jar
	- httpclient-4.5.7.jar
	- httpclient-cache-4.5.7.jar
	- httpcore-4.4.11.jar
	- jna-4.5.2.jar
	- jna-platform-4.5.2.jar

## Spots processing
After pulling the spots from WSPRNET.ORG the spots are forwarded to the configured WsprNetSpotConsumers.

Each consumer must implement the abstract class _WsprNetSpotConsumer_. Currently I've implemented three samples for spot consumers. 

### MYSQL WsprNetConsumerMYSQL
This is a simple implementation of an SQL backend interface. All spots received are inserted into a simple table.

The DDL is defined in _DDL_SPOTS.sql_:

	CREATE TABLE 'spots' (
	'time' timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	'transmitter' varchar(50) NOT NULL,
	'transmitter_loc' varchar(10) NOT NULL,
	'transmitter_dBm' decimal(10,1) DEFAULT NULL,
	'transmitter_W' decimal(10,3) DEFAULT NULL,
	'reporter' varchar(50) NOT NULL,
	'reporter_loc' varchar(10) NOT NULL,
	'frequency' int(11) NOT NULL,
	'band' varchar(10) DEFAULT NULL,
	'snr' int(11) NOT NULL,
	'drift' int(11) NOT NULL,
	'distance' int(11) DEFAULT NULL,
	'bearing' int(11) DEFAULT NULL,
	PRIMARY KEY ('time','transmitter','reporter','frequency'),
	KEY 'time' ('time','transmitter','frequency'),
	KEY 'A' ('transmitter','band','bearing')
	) ENGINE=InnoDB DEFAULT CHARSET=utf8


The following properties must be defined in _WsprNet.properties_:

	SQL.database.user=USERID
	SQL.database.password=PASSWORD
	SQL.database.uri=jdbc:mysql://1.2.3.4/wspr

I use the genuine MYSQL connect: _mysql-connector-java-8.0.15.jar_

### MQTT WsprNetConsumerMQTT
This is a simple implementation of an MQTT backend interface. The number of spots receive by each callsign is published to an MQTT server.

This is a special solution for my environment - use it as a template ...

The following properties must be defined in _WsprNet.properties_:

	MQTT.Password=PASSWORD
	MQTT.Server=tcp\://1.2.3.4\:1883
	MQTT.User=USER
	MQTT.qos=1
	MQTT.Topic=dl2sba.de/sensorData/%d/%d

I use the Eclipse PAHO MQTTT client: _org.eclipse.paho.client.mqttv3-1.2.0.jar_

	
### Console WspNetConsumerConsole

This consumer simply dumps some data of each sport to the console.


## credits
This work is based on the published API from https://github.com/garymcm/wsprnet_api by Gary
