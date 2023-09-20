package it.eng.spagobi.utilities.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.ext.ParamConverter;

public class DateConverter implements ParamConverter<Date> {
	
	private SimpleDateFormat formatter;
	
	public DateConverter(String format) {
		formatter = new SimpleDateFormat(format);
	}

	@Override
	public Date fromString(String str) {
		try {
			return formatter.parse(str);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString(Date date) {
		return formatter.format(date);
	}
}
