package it.eng.spagobi.utilities.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtils {

	static protected Logger logger = Logger.getLogger(DateUtils.class);

	public static boolean isValidFormat(String dateToValidate, String dateFormat) {

		if (dateToValidate == null || dateToValidate.isEmpty() || dateFormat == null || dateToValidate.isEmpty()) {
			return false;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		sdf.setLenient(false);

		try {

			// if not valid, it will throw ParseException
			Date date = sdf.parse(dateToValidate);
			logger.debug("Parsed date is equal to [" + date + "]");

		} catch (ParseException e) {

			logger.debug("Value [" + dateToValidate + "] not formatted as [" + dateFormat + "]");
			return false;
		}

		return true;
	}

}
