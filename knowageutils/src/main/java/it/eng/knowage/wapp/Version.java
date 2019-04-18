/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.wapp;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Reading Knowage version dynamically from Maven. It is used to concatenate project version with main src folder, parent of all static files, in purpose of
 * managing caching properly.
 *
 * @author Predrag Josipovic
 *
 */
public class Version {

	private static transient Logger logger = Logger.getLogger(Version.class);

	private static String COMPLETE_VERSION;
	private static String MAJOR_VERSION;
	private static String MINOR_VERSION;
	private static String PATCH_VERSION;
	private static String OPTIONAL;
	private static String RELEASE_DATE;
	private static Environment ENVIRONMENT;
	private static String VERSION_FOR_DATABASE;

	private static final String KNOWAGE_APPLICATION_PROPERTIES = "application.properties";
	private static final String KNOWAGE_APPLICATION_VERSION_PROPERTY = "application.version";
	private static final String KNOWAGE_APPLICATION_REALEASEDATE_PROPERTY = "application.releasedate";
	private static final String KNOWAGE_APPLICATION_ENVIRONMENT_PROPERTY = "application.environment";

	// map for short optional versions ("S", "RC")
	private static Map<String, String> optionalVersions = new HashMap<String, String>();
	private static final String SNAPSHOT = "SNAPSHOT"; // shorten will be "S"
	private static final String RELEASE_CANDIDATE = "RELEASE-CANDIDATE"; // shorten will be "RC"

	public static final String VERSION_SEPARATOR = ".";
	public static String VERSION_FOR_EXPORT = Version.MAJOR_VERSION + Version.VERSION_SEPARATOR + Version.MINOR_VERSION;

	static {
		try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(KNOWAGE_APPLICATION_PROPERTIES)) {
			Properties properties = new Properties();
			properties.load(inputStream);
			COMPLETE_VERSION = properties.getProperty(KNOWAGE_APPLICATION_VERSION_PROPERTY);
			MAJOR_VERSION = separateCompleteVersion(COMPLETE_VERSION, "MAJOR");
			MINOR_VERSION = separateCompleteVersion(COMPLETE_VERSION, "MINOR");
			PATCH_VERSION = separateCompleteVersion(COMPLETE_VERSION, "PATCH");
			OPTIONAL = separateCompleteVersion(COMPLETE_VERSION, "OPTIONAL");
			RELEASE_DATE = properties.getProperty(KNOWAGE_APPLICATION_REALEASEDATE_PROPERTY);
			ENVIRONMENT = Environment.valueOf(properties.getProperty(KNOWAGE_APPLICATION_ENVIRONMENT_PROPERTY).toUpperCase());

			optionalVersions.put(SNAPSHOT, "S");
			optionalVersions.put(RELEASE_CANDIDATE, "RC");
			VERSION_FOR_DATABASE = Version.getCompleteVersion();
			if (VERSION_FOR_DATABASE.length() > 10) {
				VERSION_FOR_DATABASE = getVersionForDB();
			}
		} catch (Exception e) {
			logger.error("Cannot read " + KNOWAGE_APPLICATION_PROPERTIES + " file", e);
			throw new RuntimeException("Cannot read " + KNOWAGE_APPLICATION_PROPERTIES + " file", e);
		}
	}

	/**
	 *
	 * @return Maven ${project.version} variable
	 */
	public static String getCompleteVersion() {
		return COMPLETE_VERSION;
	}

	public static String getMajorVersion() {
		return MAJOR_VERSION;
	}

	public static String getMinorVersion() {
		return MINOR_VERSION;
	}

	public static String getPatchVersion() {
		return PATCH_VERSION;
	}

	public static String getOptional() {
		return OPTIONAL;
	}

	public static String getReleaseDate() {
		return RELEASE_DATE;
	}

	public static Environment getEnvironment() {
		return ENVIRONMENT;
	}

	/**
	 *
	 * @return Knowage Version for Database. It is a shorten version, like [7.0.0-S] instead of [7.0.0-SNAPSHOT]
	 */
	public static String getVersionForDatabase() {
		return VERSION_FOR_DATABASE;
	}

	private static String separateCompleteVersion(String completeVersion, String part) {
		logger.debug("IN");
		String toReturn = "";
		// Find all digits separated by dot (.) + optional i.e. [7.0.0-SNAPSHOT]
		Pattern regexPattern = Pattern.compile("([\\d]+)\\.([\\d]+)\\.([\\d]+)(-(.+))?");
		Matcher matcher = regexPattern.matcher(completeVersion);
		if (matcher.find()) {
			switch (part) {
			case "MAJOR":
				toReturn = matcher.group(1);
				break;
			case "MINOR":
				toReturn = matcher.group(2);
				break;
			case "PATCH":
				toReturn = matcher.group(3);
				break;
			case "OPTIONAL":
				toReturn = matcher.group(5);
				break;
			default:
				toReturn = completeVersion;
				break;
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private static String getVersionForDB() {
		String version = Version.getMajorVersion() + VERSION_SEPARATOR + Version.getMinorVersion() + VERSION_SEPARATOR + Version.getPatchVersion();
		String optional = Version.getOptional();
		if (optional != null && !optional.isEmpty()) {
			String shortOptional = optionalVersions.get(optional.toUpperCase());
			if (shortOptional != null)
				version = version + "-" + shortOptional;
			else {
				version = version + "-" + optional;
				version = version.substring(0, 10);
			}
		}
		return version;
	}

}
