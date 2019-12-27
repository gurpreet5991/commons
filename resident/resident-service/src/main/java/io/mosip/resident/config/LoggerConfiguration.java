package io.mosip.resident.config;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.appender.ConsoleAppender;
import io.mosip.kernel.logger.logback.factory.Logfactory;


public class LoggerConfiguration {
	/**
	 * Private Constructor to prevent instantiation.
	 */
	private LoggerConfiguration() {
	}

	/**
	 * This method sets the logger target, and returns appender.
	 * 
	 * @param clazz the class.
	 * @return the appender.
	 */
	public static Logger logConfig(Class<?> clazz) {
		ConsoleAppender appender = new ConsoleAppender();
		appender.setTarget("System.err");
		return Logfactory.getDefaultConsoleLogger(appender, clazz);
	}
}
