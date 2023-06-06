package it.eng.knowage.tools.utils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
				SimpleDateFormat timestampFormatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
				LOGGER.debug("value will be parsed as a String");
				value = timestampFormatter.parse((String) value).getTime();
			}

		} catch (ParseException | SQLException e) {
			throw new RuntimeException(e);
		}

		return value;

	}

	private DatabaseUtils() {
	}

}
