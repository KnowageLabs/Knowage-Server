package it.eng.knowage.privacymanager;

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventBuilderUtils {

	private static final Logger LOGGER = LogManager.getLogger(EventBuilderUtils.class);
	private static final PMConfiguration PM_CONFIG_INSTANCE = PMConfiguration.getInstance();
	private static final Pattern NAME_PATTERN = Pattern.compile(PM_CONFIG_INSTANCE.getProperty("subject.name.pattern"),
			Pattern.CASE_INSENSITIVE);
	private static final Pattern LAST_NAME_PATTERN = Pattern
			.compile(PM_CONFIG_INSTANCE.getProperty("subject.lastname.pattern"), Pattern.CASE_INSENSITIVE);
	private static final Pattern BIRTHDATE_PATTERN = Pattern
			.compile(PM_CONFIG_INSTANCE.getProperty("subject.birthdate.pattern"), Pattern.CASE_INSENSITIVE);
	private static final Pattern TAXCODE_PATTERN = Pattern
			.compile(PM_CONFIG_INSTANCE.getProperty("subject.taxcode.pattern"), Pattern.CASE_INSENSITIVE);

	public static final String NAME = "name";
	public static final String LAST_NAME = "lname";
	public static final String BIRTHDATE = "bdate";
	public static final String TAXCODE = "tcode";

	static {
		LOGGER.debug("Name pattern is: {}", NAME_PATTERN);
		LOGGER.debug("Last name pattern is: {}", LAST_NAME_PATTERN);
		LOGGER.debug("Birthdate pattern is: {}", BIRTHDATE_PATTERN);
		LOGGER.debug("Tax code pattern is: {}", TAXCODE_PATTERN);
	}

	public static String decodeSubjectField(String fieldName) {
		LOGGER.debug("Decoding {} for subject field", fieldName);

		String ret = "n.d.";

		if (LAST_NAME_PATTERN.matcher(fieldName).find()) {
			ret = LAST_NAME;
		} else if (NAME_PATTERN.matcher(fieldName).find()) {
			ret = NAME;
		} else if (BIRTHDATE_PATTERN.matcher(fieldName).find()) {
			ret = BIRTHDATE;
		} else if (TAXCODE_PATTERN.matcher(fieldName).find()) {
			ret = TAXCODE;
		}

		LOGGER.debug("Decoded {} as subject field {}", fieldName, ret);
		return ret;
	}

}
