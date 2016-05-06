package it.eng.spagobi.utilities.date;

import it.eng.spagobi.services.rest.annotations.DateFormat;

import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.resteasy.spi.StringParameterUnmarshaller;
import org.jboss.resteasy.util.FindAnnotation;

public class DateFormatter implements StringParameterUnmarshaller<Date> {
	private SimpleDateFormat formatter;

	@Override
	public void setAnnotations(Annotation[] annotations) {
		DateFormat format = FindAnnotation.findAnnotation(annotations, DateFormat.class);
		formatter = new SimpleDateFormat(format.value());
	}

	@Override
	public Date fromString(String str) {
		try {
			return formatter.parse(str);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
