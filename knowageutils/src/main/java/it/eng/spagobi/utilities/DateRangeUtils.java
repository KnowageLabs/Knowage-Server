package it.eng.spagobi.utilities;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.utilities.assertion.Assert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utilities to manage the Date Range Analitycs Driver
 *
 * @author fabrizio
 *
 */
public class DateRangeUtils {

	private static final String WEEKS_SHORT = "W";

	private static final String DAYS_SHORT = "D";

	private static final String MONTHS_SHORT = "M";

	private static final String YEARS_SHORT = "Y";

	private static final String WEEKS_LONG = "weeks";

	private static final String DAYS_LONG = "days";

	private static final String MONTHS_LONG = "months";

	private static final String YEARS_LONG = "years";

	public static final String END_SUFFIX = "_end";

	public static final String DURATION_SUFFIX = "_duration";

	public static final String BEGIN_SUFFIX = "_begin";

	public static final String DATE_RANGE_VALUE_PATTERN = "dd-MM-yyyy";

	public static final SimpleDateFormat DATE_RANGE_VALUE_FORMAT = new SimpleDateFormat(DATE_RANGE_VALUE_PATTERN);

	/**
	 * for testing reasons
	 */
	private static String DATE_FORMAT_SERVER;

	/**
	 * Get the correctly date
	 *
	 * @param startDateString
	 * @return
	 */
	private static Date getDateRangeStartDate(String startDateString) {
		Date startDate = null;
		try {
			String serverDateformat = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format");
			SimpleDateFormat SERVER_DATE_RANGE_VALUE_FORMAT = new SimpleDateFormat(serverDateformat);
			startDate = SERVER_DATE_RANGE_VALUE_FORMAT.parse(startDateString);
			// startDate = DATE_RANGE_VALUE_FORMAT.parse(startDateString);
		} catch (ParseException e) {
			Assert.assertUnreachable("date not in correctly format");
		}
		return startDate;
	}

	/**
	 * Get the duration in short format (es. 6W) from the option which can be in long format or short format
	 *
	 * @param option
	 * @return
	 */
	public static String getDuration(String option) {
		Helper.checkNotTrimNotEmpty(option, "option");

		int under = option.indexOf('_');
		if (under == -1) {
			// short format
			return option;
		}

		// long format
		String typeLong = option.substring(under + 1);
		String type;

		switch (typeLong) {
		case YEARS_LONG:
			type = YEARS_SHORT;
			break;

		case MONTHS_LONG:
			type = MONTHS_SHORT;
			break;

		case DAYS_LONG:
			type = DAYS_SHORT;
			break;

		case WEEKS_LONG:
			type = WEEKS_SHORT;
			break;

		default:
			Assert.assertUnreachable("type of option not supported");
			type = null; // for compilation
			break;
		}

		String res = option.replace("_" + typeLong, type);
		return res;
	}

	/**
	 * Get the end date by the start date and option. Note that both dates are is at start of the day (00:00).
	 *
	 * @param startDate
	 * @param option
	 * @return
	 */
	public static Date getDateRangeEndDate(Date startDate, String option) {
		int calendarAddType = 0;
		int under = option.indexOf('_');
		int endQuantity;
		String type;
		if (under != -1) {
			// format 6_weeks
			type = option.substring(under + 1);
			endQuantity = under;
		} else {
			// format , 6W
			type = "" + option.charAt(option.length() - 1);
			endQuantity = option.length() - 1;
		}
		switch (type) {
		case YEARS_LONG:
		case YEARS_SHORT:
			calendarAddType = Calendar.YEAR;
			break;

		case MONTHS_LONG:
		case MONTHS_SHORT:
			calendarAddType = Calendar.MONTH;
			break;

		case DAYS_LONG:
		case DAYS_SHORT:
			calendarAddType = Calendar.DAY_OF_YEAR;
			break;

		case WEEKS_LONG:
		case WEEKS_SHORT:
			calendarAddType = Calendar.WEEK_OF_YEAR;
			break;

		default:
			Assert.assertUnreachable("type of option not supported");
			break;
		}

		int quantity = Integer.parseInt(option.substring(0, endQuantity));
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.add(calendarAddType, quantity);
		Date res = c.getTime();
		return res;
	}

	/**
	 * Get the date range from the Date Range value. Es. of value: 20-10-2014_6M.
	 *
	 * @param value
	 * @return
	 */
	public static Date[] getDateRangeDates(String value) {
		Assert.assertTrue(value != null && !value.isEmpty(), "value must be present");
		String[] startOption = value.split("_");
		Assert.assertTrue(startOption.length == 2, "must be in the format day-month-year_date range option");
		String startDateString = startOption[0];
		String option = startOption[1];
		Assert.assertTrue(option.length() >= 2, "option must be in the format typeQuantity");
		Date startDate = getDateRangeStartDate(startDateString);
		Date endDate = getDateRangeEndDate(startDate, option);
		return new Date[] { startDate, endDate };
	}

	/**
	 * Get the Date Range option from the global value, es: 10-12-2019_2Y
	 *
	 * @param value
	 * @return
	 */
	public static String getOption(String value) {
		Assert.assertTrue(value != null && !value.isEmpty(), "value must be present");
		String[] startOption = value.split("_");
		Assert.assertTrue(startOption.length == 2, "must be in the format day-month-year_date range option");
		String option = startOption[1];
		return option;
	}

	/**
	 * Add a day to to the end date fix the problem with the date range
	 *
	 * @param d
	 * @return
	 */
	public static Date addDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_YEAR, 1);
		Date res = c.getTime();
		Assert.assertTrue(isBeginOfDay(c), "must be begin of day");
		return res;
	}

	private static boolean isBeginOfDay(Calendar c) {
		int[] fields = new int[] { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND };
		for (int f : fields) {
			if (c.get(f) != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the value is admitted by the Date Range Filter
	 *
	 * @param valuefilter
	 *            the value of filter, es: 10-12-2019_2Y
	 * @param typeFilter
	 *            the type of filter: in range or not in range
	 * @param value
	 *            : the value, Es.: 12/05/2020
	 * @return
	 */
	public static boolean isInDateRangeFilter(String valuefilter, String typeFilter, String value) {
		try {
			String format = DATE_FORMAT_SERVER != null ? DATE_FORMAT_SERVER : SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format");
			Assert.assertNotNull(format, "date format is null");
			Date valueDate = toDate(value, format);
			Assert.assertNotNull(valuefilter, "date range filter is null");
			Date[] startEnd = getDateRangeDates(valuefilter);
			startEnd[1] = addDay(startEnd[1]);

			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.IN_RANGE_FILTER)) {
				return valueDate.compareTo(startEnd[0]) >= 0 && valueDate.compareTo(startEnd[1]) < 0;
			} else if (typeFilter.equalsIgnoreCase(SpagoBIConstants.NOT_IN_RANGE_FILTER)) {
				return valueDate.compareTo(startEnd[0]) < 0 && valueDate.compareTo(startEnd[1]) >= 0;
			} else {
				throw new RuntimeException(String.format("Filter %s not supported", typeFilter));
			}
		} catch (Exception e) {
			throw new RuntimeException("Error in date range post processing filtering", e);
		}
	}

	/**
	 * Converts a String representing a date into a Date object, given the date format.
	 *
	 * @param dateStr
	 *            The String representing the date
	 * @param format
	 *            The date format
	 *
	 * @return the relevant Date object
	 * @throws ParseException
	 *
	 * @throws Exception
	 *             if any parsing exception occurs
	 */
	public static Date toDate(String value, String format) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern(format);
		dateFormat.setLenient(false);
		Date date = dateFormat.parse(value);
		return date;
	}

	protected static String getDATE_FORMAT_SERVER() {
		return DATE_FORMAT_SERVER;
	}

	protected static void setDATE_FORMAT_SERVER(String dATE_FORMAT_SERVER) {
		DATE_FORMAT_SERVER = dATE_FORMAT_SERVER;
	}

}
