package it.eng.spagobi.utilities.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.ext.ParamConverter;

public class DateConverter implements ParamConverter<Date> {

	private static SimpleDateFormat formatter;

	public DateConverter(String format) {
		formatter = new SimpleDateFormat(format);
	}

	@Override
	public Date fromString(String str) {
		try {
			return getFormattedDateFromString(str);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString(Date date) {
		try {
			return getFormattedStringFromDate(date);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private static Date getFormattedDateFromString(String str) throws ParseException {
		synchronized (formatter) {
			return formatter.parse(str);
		}
	}

	private static String getFormattedStringFromDate(Date date) throws ParseException {
		synchronized (formatter) {
			return formatter.format(date);
		}
	}
}
