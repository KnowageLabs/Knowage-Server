package it.eng.knowage.privacymanager;

import java.util.regex.Pattern;

public class EventBuilderUtils {

	private static final Pattern NAME_PATTERN = Pattern.compile(PMConfiguration.getInstance().getProperty("subject.name.pattern"), Pattern.CASE_INSENSITIVE);
	private static final Pattern LAST_NAME_PATTERN = Pattern.compile(PMConfiguration.getInstance().getProperty("subject.lastname.pattern"),
			Pattern.CASE_INSENSITIVE);
	private static final Pattern BIRTHDATE_PATTERN = Pattern.compile(PMConfiguration.getInstance().getProperty("subject.birthdate.pattern"),
			Pattern.CASE_INSENSITIVE);
	private static final Pattern TAXCODE_PATTERN = Pattern.compile(PMConfiguration.getInstance().getProperty("subject.taxcode.pattern"),
			Pattern.CASE_INSENSITIVE);

	public static final String NAME = "name";
	public static final String LAST_NAME = "lname";
	public static final String BIRTHDATE = "bdate";
	public static final String TAXCODE = "tcode";

	public static String decodeSubjectField(String fieldName) {

		if (LAST_NAME_PATTERN.matcher(fieldName).find()) {
			return LAST_NAME;
		} else if (NAME_PATTERN.matcher(fieldName).find()) {
			return NAME;
		} else if (BIRTHDATE_PATTERN.matcher(fieldName).find()) {
			return BIRTHDATE;
		} else if (TAXCODE_PATTERN.matcher(fieldName).find()) {
			return TAXCODE;
		}

		return "n.d.";
	}

}
