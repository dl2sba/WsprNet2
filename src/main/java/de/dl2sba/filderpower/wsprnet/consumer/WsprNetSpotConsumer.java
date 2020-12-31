package de.dl2sba.filderpower.wsprnet.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dl2sba.common.ProcessingException;
import de.dl2sba.filderpower.wsprnet.WsprNetQueryResult;

public abstract class WsprNetSpotConsumer {
	protected static final Logger logger = LogManager.getLogger(WsprNetSpotConsumer.class.getName());

	public abstract void consume(WsprNetQueryResult data) throws ProcessingException;

	public abstract void beginWork() throws ProcessingException;

	public abstract void endWork() throws ProcessingException;
}
