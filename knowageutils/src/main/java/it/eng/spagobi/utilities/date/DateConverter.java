package it.eng.spagobi.utilities.date;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import javax.ws.rs.ext.ParamConverter;

public class DateConverter implements ParamConverter<Date> {
	@Deprecated
	private SimpleDateFormat formatter;
	private final DateTimeFormatter formatter_v2;

	public DateConverter(String format) {
		formatter_v2 = DateTimeFormatter.ofPattern(format);
	}

	@Override
	public Date fromString(String str) {
		try {
			Date data = new Date(Instant.from(formatter_v2.parse(str)).toEpochMilli());
			return data;
		} catch (DateTimeParseException e) {
			try {
				Date data = Date.from((LocalDate.parse(str, formatter_v2)).atStartOfDay(ZoneId.systemDefault()).toInstant());
				return data;
			} catch (DateTimeParseException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	@Override
	public String toString(Date date) {
		return formatter_v2.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
	}
}
