package com.nhl.bootique.jersey.client.log;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JULSlf4jLogger extends Logger {

	private static final Consumer<LogRecord> DEFAULT_LOGGER = message -> {
	};

	private Map<Level, Consumer<LogRecord>> loggers;

	public JULSlf4jLogger(String name, org.slf4j.Logger slfLogger, Level level) {
		super(name, null);

		setLevel(level);

		this.loggers = new HashMap<>();

		Consumer<LogRecord> trace = log -> slfLogger.trace(log.getMessage(), log.getThrown());
		loggers.put(Level.ALL, trace);
		loggers.put(Level.CONFIG, trace);
		loggers.put(Level.FINER, trace);
		loggers.put(Level.FINEST, trace);
		loggers.put(Level.FINE, log -> slfLogger.debug(log.getMessage(), log.getThrown()));
		loggers.put(Level.INFO, log -> slfLogger.info(log.getMessage(), log.getThrown()));
		loggers.put(Level.WARNING, log -> slfLogger.warn(log.getMessage(), log.getThrown()));
		loggers.put(Level.SEVERE, log -> slfLogger.error(log.getMessage(), log.getThrown()));
	}

	@Override
	public void log(LogRecord record) {
		forLevel(record.getLevel()).accept(record);
	}

	private Consumer<LogRecord> forLevel(Level level) {
		return loggers.getOrDefault(level, DEFAULT_LOGGER);
	}

}
