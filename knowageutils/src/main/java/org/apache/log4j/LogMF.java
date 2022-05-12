/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j;

import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * This class is an extension of log4j 1 to log4j 2 bridge, since it does not contain LogMF class.
 *
 */
public final class LogMF {

	private static final String LOG4J1_PLACEHOLDER_SYNTAX = "\\{[0-9]*\\}";
	private static final String LOG4J2_PLACEHOLDER_SYNTAX = "{}";

	private static String getNewPattern(String oldPattern) {
		return oldPattern.replaceAll(LOG4J1_PLACEHOLDER_SYNTAX, LOG4J2_PLACEHOLDER_SYNTAX);
	}

	/**
	 * Log a parameterized message at trace level.
	 *
	 * @param logger    logger, may not be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void trace(final Logger logger, final String pattern, final Object[] arguments) {
		if (logger.isEnabledFor(Level.TRACE)) {
			String newPattern = getNewPattern(pattern);
			logger.trace(new ParameterizedMessage(newPattern, arguments).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at debug level.
	 *
	 * @param logger    logger, may not be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void debug(final Logger logger, final String pattern, final Object[] arguments) {
		if (logger.isDebugEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.debug(new ParameterizedMessage(newPattern, arguments).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at info level.
	 *
	 * @param logger    logger, may not be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void info(final Logger logger, final String pattern, final Object[] arguments) {
		if (logger.isInfoEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.info(new ParameterizedMessage(newPattern, arguments).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at warn level.
	 *
	 * @param logger    logger, may not be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void warn(final Logger logger, final String pattern, final Object[] arguments) {
		if (logger.isEnabledFor(Level.WARN)) {
			String newPattern = getNewPattern(pattern);
			logger.warn(new ParameterizedMessage(newPattern, arguments).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at error level.
	 *
	 * @param logger    logger, may not be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void error(final Logger logger, final String pattern, final Object[] arguments) {
		if (logger.isEnabledFor(Level.ERROR)) {
			String newPattern = getNewPattern(pattern);
			logger.error(new ParameterizedMessage(newPattern, arguments).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at fatal level.
	 *
	 * @param logger    logger, may not be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void fatal(final Logger logger, final String pattern, final Object[] arguments) {
		if (logger.isEnabledFor(Level.FATAL)) {
			String newPattern = getNewPattern(pattern);
			logger.fatal(new ParameterizedMessage(newPattern, arguments).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at trace level.
	 *
	 * @param logger    logger, may not be null.
	 * @param t         throwable, may be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void trace(final Logger logger, final Throwable t, final String pattern, final Object[] arguments) {
		if (logger.isEnabledFor(Level.TRACE)) {
			String newPattern = getNewPattern(pattern);
			logger.trace(new ParameterizedMessage(newPattern, arguments).getFormattedMessage(), t);
		}
	}

	/**
	 * Log a parameterized message at debug level.
	 *
	 * @param logger    logger, may not be null.
	 * @param t         throwable, may be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void debug(final Logger logger, final Throwable t, final String pattern, final Object[] arguments) {
		if (logger.isDebugEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.debug(new ParameterizedMessage(newPattern, arguments).getFormattedMessage(), t);
		}
	}

	/**
	 * Log a parameterized message at info level.
	 *
	 * @param logger    logger, may not be null.
	 * @param t         throwable, may be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void info(final Logger logger, final Throwable t, final String pattern, final Object[] arguments) {
		if (logger.isInfoEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.info(new ParameterizedMessage(newPattern, arguments).getFormattedMessage(), t);
		}
	}

	/**
	 * Log a parameterized message at warn level.
	 *
	 * @param logger    logger, may not be null.
	 * @param t         throwable, may be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void warn(final Logger logger, final Throwable t, final String pattern, final Object[] arguments) {
		if (logger.isEnabledFor(Level.WARN)) {
			String newPattern = getNewPattern(pattern);
			logger.warn(new ParameterizedMessage(newPattern, arguments).getFormattedMessage(), t);
		}
	}

	/**
	 * Log a parameterized message at error level.
	 *
	 * @param logger    logger, may not be null.
	 * @param t         throwable, may be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void error(final Logger logger, final Throwable t, final String pattern, final Object[] arguments) {
		if (logger.isEnabledFor(Level.ERROR)) {
			String newPattern = getNewPattern(pattern);
			logger.error(new ParameterizedMessage(newPattern, arguments).getFormattedMessage(), t);
		}
	}

	/**
	 * Log a parameterized message at fatal level.
	 *
	 * @param logger    logger, may not be null.
	 * @param t         throwable, may be null.
	 * @param pattern   pattern, may be null.
	 * @param arguments an array of arguments to be formatted and substituted.
	 */
	public static void fatal(final Logger logger, final Throwable t, final String pattern, final Object[] arguments) {
		if (logger.isEnabledFor(Level.FATAL)) {
			String newPattern = getNewPattern(pattern);
			logger.fatal(new ParameterizedMessage(newPattern, arguments).getFormattedMessage(), t);
		}
	}

	/**
	 * Log a parameterized message at trace level.
	 *
	 * @param logger   logger, may not be null.
	 * @param pattern  pattern, may be null.
	 * @param argument a value to be formatted and substituted.
	 */
	public static void trace(final Logger logger, final String pattern, final Object argument) {
		if (logger.isEnabledFor(Level.TRACE)) {
			String newPattern = getNewPattern(pattern);
			logger.trace(new ParameterizedMessage(newPattern, argument).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at trace level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 */
	public static void trace(final Logger logger, final String pattern, final Object arg0, final Object arg1) {
		if (logger.isEnabledFor(Level.TRACE)) {
			String newPattern = getNewPattern(pattern);
			logger.trace(new ParameterizedMessage(newPattern, arg0, arg1).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at trace level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 * @param arg2    a value to be formatted and substituted.
	 */
	public static void trace(final Logger logger, final String pattern, final Object arg0, final Object arg1, final Object arg2) {
		if (logger.isEnabledFor(Level.TRACE)) {
			String newPattern = getNewPattern(pattern);
			logger.trace(new ParameterizedMessage(newPattern, arg0, arg1, arg2).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at trace level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 * @param arg2    a value to be formatted and substituted.
	 * @param arg3    a value to be formatted and substituted.
	 */
	public static void trace(final Logger logger, final String pattern, final Object arg0, final Object arg1, final Object arg2, final Object arg3) {
		if (logger.isEnabledFor(Level.TRACE)) {
			String newPattern = getNewPattern(pattern);
			logger.trace(new ParameterizedMessage(newPattern, arg0, arg1, arg2, arg3).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at debug level.
	 *
	 * @param logger   logger, may not be null.
	 * @param pattern  pattern, may be null.
	 * @param argument a value to be formatted and substituted.
	 */
	public static void debug(final Logger logger, final String pattern, final Object argument) {
		if (logger.isDebugEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.debug(new ParameterizedMessage(newPattern, argument).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at debug level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 */
	public static void debug(final Logger logger, final String pattern, final Object arg0, final Object arg1) {
		if (logger.isDebugEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.debug(new ParameterizedMessage(newPattern, arg0, arg1).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at debug level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 * @param arg2    a value to be formatted and substituted.
	 */
	public static void debug(final Logger logger, final String pattern, final Object arg0, final Object arg1, final Object arg2) {
		if (logger.isDebugEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.debug(new ParameterizedMessage(newPattern, arg0, arg1, arg2).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at debug level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 * @param arg2    a value to be formatted and substituted.
	 * @param arg3    a value to be formatted and substituted.
	 */
	public static void debug(final Logger logger, final String pattern, final Object arg0, final Object arg1, final Object arg2, final Object arg3) {
		if (logger.isDebugEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.debug(new ParameterizedMessage(newPattern, arg0, arg1, arg2, arg3).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at info level.
	 *
	 * @param logger   logger, may not be null.
	 * @param pattern  pattern, may be null.
	 * @param argument a value to be formatted and substituted.
	 */
	public static void info(final Logger logger, final String pattern, final Object argument) {
		if (logger.isInfoEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.info(new ParameterizedMessage(newPattern, argument).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at info level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 */
	public static void info(final Logger logger, final String pattern, final Object arg0, final Object arg1) {
		if (logger.isInfoEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.info(new ParameterizedMessage(newPattern, arg0, arg1).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at info level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 * @param arg2    a value to be formatted and substituted.
	 */
	public static void info(final Logger logger, final String pattern, final Object arg0, final Object arg1, final Object arg2) {
		if (logger.isInfoEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.info(new ParameterizedMessage(newPattern, arg0, arg1, arg2).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at info level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 * @param arg2    a value to be formatted and substituted.
	 * @param arg3    a value to be formatted and substituted.
	 */
	public static void info(final Logger logger, final String pattern, final Object arg0, final Object arg1, final Object arg2, final Object arg3) {
		if (logger.isInfoEnabled()) {
			String newPattern = getNewPattern(pattern);
			logger.info(new ParameterizedMessage(newPattern, arg0, arg1, arg2, arg3).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at warn level.
	 *
	 * @param logger   logger, may not be null.
	 * @param pattern  pattern, may be null.
	 * @param argument a value to be formatted and substituted.
	 */
	public static void warn(final Logger logger, final String pattern, final Object argument) {
		if (logger.isEnabledFor(Level.WARN)) {
			String newPattern = getNewPattern(pattern);
			logger.warn(new ParameterizedMessage(newPattern, argument).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at warn level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 */
	public static void warn(final Logger logger, final String pattern, final Object arg0, final Object arg1) {
		if (logger.isEnabledFor(Level.WARN)) {
			String newPattern = getNewPattern(pattern);
			logger.warn(new ParameterizedMessage(newPattern, arg0, arg1).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at warn level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 * @param arg2    a value to be formatted and substituted.
	 */
	public static void warn(final Logger logger, final String pattern, final Object arg0, final Object arg1, final Object arg2) {
		if (logger.isEnabledFor(Level.WARN)) {
			String newPattern = getNewPattern(pattern);
			logger.warn(new ParameterizedMessage(newPattern, arg0, arg1, arg2).getFormattedMessage());
		}
	}

	/**
	 * Log a parameterized message at warn level.
	 *
	 * @param logger  logger, may not be null.
	 * @param pattern pattern, may be null.
	 * @param arg0    a value to be formatted and substituted.
	 * @param arg1    a value to be formatted and substituted.
	 * @param arg2    a value to be formatted and substituted.
	 * @param arg3    a value to be formatted and substituted.
	 */
	public static void warn(final Logger logger, final String pattern, final Object arg0, final Object arg1, final Object arg2, final Object arg3) {
		if (logger.isEnabledFor(Level.WARN)) {
			String newPattern = getNewPattern(pattern);
			logger.warn(new ParameterizedMessage(newPattern, arg0, arg1, arg2, arg3).getFormattedMessage());
		}
	}

}
