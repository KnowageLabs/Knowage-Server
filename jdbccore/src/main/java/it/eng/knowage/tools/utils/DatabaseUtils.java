package it.eng.knowage.tools.utils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

public class DatabaseUtils {

	private static final Logger LOGGER = Logger.getLogger(DatabaseUtils.class);
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
	private static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat(TIMESTAMP_FORMAT);

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
				LOGGER.debug("value will be parsed as a String");
				value = getFormattedDate(value);
			}

		} catch (ParseException | SQLException e) {
			throw new RuntimeException(e);
		}

		return value;

	}

	private DatabaseUtils() {
	}

	private static long getFormattedDate(Object value) throws ParseException {
		synchronized (TIMESTAMP_FORMATTER) {
			return TIMESTAMP_FORMATTER.parse((String) value).getTime();
		}
	}

}
