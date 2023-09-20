package it.eng.knowage.tools.utils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;

public class DatabaseUtils {

	private static final Logger LOGGER = Logger.getLogger(DatabaseUtils.class);
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.S";

	public static Object timestampFormatter(Object value) {

		try {

			if (value instanceof oracle.sql.TIMESTAMP) {
				LOGGER.debug("value will be parsed as oracle.sql.TIMESTAMP");
				oracle.sql.TIMESTAMP valToChange = (oracle.sql.TIMESTAMP) value;
				java.sql.Timestamp time = valToChange.timestampValue();
				return time.getTime();
			} else if (value instanceof Timestamp) {
				LOGGER.debug("value will be parsed as java.sql.Timestamp");
				Timestamp timestamp = (Timestamp) value;
				return timestamp.getTime();
			} else {
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
				Instant instant = dateTimeFormatter.parse((String) value, Instant::from);
				LOGGER.debug("value will be parsed as a String");
				value = instant.toEpochMilli();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return value;

	}

	private DatabaseUtils() {
	}

}
