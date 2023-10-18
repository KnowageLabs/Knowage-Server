package it.eng.spagobi.utilities.date;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.ws.rs.ext.ParamConverter;

public class DateConverter implements ParamConverter<Date> {

	private final DateTimeFormatter formatter;

	public DateConverter(String format) {
		formatter = DateTimeFormatter.ofPattern(format);
	}

	@Override
	public Date fromString(String str) {
		try {
			LocalDateTime localDateTime = LocalDateTime.parse(str, formatter);
			return Timestamp.valueOf(localDateTime);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString(Date date) {
		Instant instant = date.toInstant();
		return formatter.format(instant);
	}
}
