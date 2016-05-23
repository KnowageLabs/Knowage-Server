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
package it.eng.spagobi.commons.utilities;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class StringUtilities {
	public static final String START_PARAMETER = "$P{";

	public static final String START_USER_PROFILE_ATTRIBUTE = "${";

	public static final String DEFAULT_CHARSET = "UTF-8";

	private static transient Logger logger = Logger.getLogger(StringUtilities.class);

	private static final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static Random random = new Random();

	public static String substituteProfileAttributesInString(String str, IEngUserProfile profile) throws Exception {
		return substituteParametersInString(str, UserProfileUtils.getProfileAttributes(profile));
	}

	/**
	 * Substitutes the profile attributes with sintax "${attribute_name}" with the correspondent value in the string passed at input.
	 *
	 * @param str
	 *            The string to be modified (tipically a query)
	 * @param parameters
	 *            The IEngUserProfile object
	 *
	 * @return The statement with profile attributes replaced by their values.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public static String substituteParametersInString(String str, Map parameters) throws Exception {
		logger.debug("IN");
		int profileAttributeStartIndex = str.indexOf("${");
		if (profileAttributeStartIndex != -1) {
			str = substituteParametersInString(str, parameters, profileAttributeStartIndex);
		}
		logger.debug("OUT");
		return str;
	}

	public static String substituteProfileAttributesInString(String str, IEngUserProfile profile, int profileAttributeStartIndex) throws Exception {
		return substituteParametersInString(str, UserProfileUtils.getProfileAttributes(profile), profileAttributeStartIndex);
	}

	/**
	 * returns the first index chars
	 *
	 * @param str
	 *            string to cut
	 * @param index
	 *            number of char to take
	 * @return the left part of tre string
	 */
	public static String left(String str, int index) {

		if (str != null && str.length() > index) {
			return str.substring(0, index);
		} else
			return str;

	}

	/**
	 * Substitutes the profile attributes with sintax "${attribute_name}" with the correspondent value in the string passed at input.
	 *
	 * @param statement
	 *            The string to be modified (tipically a query)
	 * @param parameters
	 *            Profile attributes map
	 * @param parametersStartIndex
	 *            The start index for query parsing (useful for recursive calling)
	 *
	 * @return The statement with profile attributes replaced by their values.
	 *
	 * @throws Exception
	 */
	public static String substituteParametersInString(String statement, Map parameters, int parametersStartIndex) throws Exception {
		logger.debug("IN.statement=" + statement);
		int profileAttributeEndIndex = statement.indexOf("}", parametersStartIndex);
		if (profileAttributeEndIndex == -1)
			throw new Exception("Not closed profile attribute: '}' expected.");
		if (profileAttributeEndIndex < profileAttributeEndIndex)
			throw new Exception("Not opened profile attribute: '${' expected.");
		String attribute = statement.substring(parametersStartIndex + 2, profileAttributeEndIndex).trim();
		int startConfigIndex = attribute.indexOf("(");
		String attributeName = "";
		String prefix = "";
		String split = "";
		String suffix = "";
		boolean attributeExcpetedToBeMultiValue = false;
		if (startConfigIndex != -1) {
			// the attribute profile is expected to be multivalue
			attributeExcpetedToBeMultiValue = true;
			int endConfigIndex = attribute.length() - 1;
			if (attribute.charAt(endConfigIndex) != ')')
				throw new Exception("Sintax error: \")\" missing. The expected sintax for "
						+ "attribute profile is ${attributeProfileName(prefix;split;suffix)} for multivalue profile attributes "
						+ "or ${attributeProfileName} for singlevalue profile attributes. 'attributeProfileName' must not contain '(' characters.");
			String configuration = attribute.substring(startConfigIndex + 1, endConfigIndex);
			String[] configSplitted = configuration.split(";");
			if (configSplitted == null || configSplitted.length != 3)
				throw new Exception("Sintax error. The expected sintax for "
						+ "attribute profile is ${attributeProfileName(prefix;split;suffix)} for multivalue profile attributes "
						+ "or ${attributeProfileName} for singlevalue profile attributes. 'attributeProfileName' must not contain '(' characters. "
						+ "The (prefix;split;suffix) is not properly configured");
			prefix = configSplitted[0];
			split = configSplitted[1];
			suffix = configSplitted[2];
			logger.debug("Multi-value attribute profile configuration found: prefix: '" + prefix + "'; split: '" + split + "'; suffix: '" + suffix + "'.");
			attributeName = attribute.substring(0, startConfigIndex);
			logger.debug("Expected multi-value attribute profile name: '" + attributeName + "'");
		} else {
			attributeName = attribute;
			logger.debug("Expected single-value attribute profile name: '" + attributeName + "'");
		}

		Object attributeValueObj = parameters.get(attributeName);
		if (attributeValueObj == null || attributeValueObj.toString().trim().equals(""))
			throw new Exception("Profile attribute '" + attributeName + "' not existing.");

		String attributeValue = attributeValueObj.toString();
		logger.debug("Profile attribute value found: '" + attributeValue + "'");
		String replacement = null;
		String newListOfValues = null;
		if (attributeExcpetedToBeMultiValue) {
			if (attributeValue.startsWith("{")) {
				// the profile attribute is multi-value
				String[] values = findAttributeValues(attributeValue);
				logger.debug("N. " + values.length + " profile attribute values found: '" + values + "'");
				newListOfValues = values[0];
				for (int i = 1; i < values.length; i++) {
					newListOfValues = newListOfValues + split + values[i];
				}
			} else {
				logger.warn("The attribute value has not the sintax of a multi value attribute; considering it as a single value.");
				newListOfValues = attributeValue;
			}
		} else {
			if (attributeValue.startsWith("{")) {
				// the profile attribute is multi-value
				logger.warn("The attribute value seems to be a multi value attribute; trying considering it as a multi value using its own splitter and no prefix and suffix.");
				try {
					// checks the sintax
					String[] values = findAttributeValues(attributeValue);
					newListOfValues = values[0];
					for (int i = 1; i < values.length; i++) {
						newListOfValues = newListOfValues + attributeValue.charAt(1) + values[i];
					}
				} catch (Exception e) {
					logger.error("The attribute value does not respect the sintax of a multi value attribute; considering it as a single value.", e);
					newListOfValues = attributeValue;
				}
			} else {
				newListOfValues = attributeValue;
			}
		}

		replacement = prefix + newListOfValues + suffix;
		attribute = quote(attribute);
		statement = statement.replaceAll("\\$\\{" + attribute + "\\}", replacement);

		parametersStartIndex = statement.indexOf("${", profileAttributeEndIndex);
		if (parametersStartIndex != -1)
			statement = substituteParametersInString(statement, parameters, parametersStartIndex);
		logger.debug("OUT");
		return statement;
	}

	/**
	 * Find the attribute values in case of multi value attribute. The sintax is: {splitter character{list of values separated by the splitter}}. Examples:
	 * {;{value1;value2;value3....}} {|{value1|value2|value3....}}
	 *
	 * @param attributeValue
	 *            The String representing the list of attribute values
	 * @return The array of attribute values
	 * @throws Exception
	 *             in case of sintax error
	 */
	public static String[] findAttributeValues(String attributeValue) throws Exception {
		logger.debug("IN");
		String sintaxErrorMsg = "Multi value attribute sintax error.";
		// Clean specification of type (STRING, NUM..) from values (if exists!!)
		int lastBrace = attributeValue.lastIndexOf("}");
		int previousLastBrace = attributeValue.indexOf("}");
		String type = attributeValue.substring(previousLastBrace + 1, lastBrace);
		if (type.length() > 0) {
			attributeValue = attributeValue.substring(0, previousLastBrace + 1) + "}";
		}
		if (attributeValue.length() < 6)
			throw new Exception(sintaxErrorMsg);
		if (!attributeValue.endsWith("}}"))
			throw new Exception(sintaxErrorMsg);
		if (attributeValue.charAt(2) != '{')
			throw new Exception(sintaxErrorMsg);
		char splitter = attributeValue.charAt(1);
		String valuesList = attributeValue.substring(3, attributeValue.length() - 2);
		String[] values = valuesList.split(String.valueOf(splitter));
		logger.debug("OUT");
		return values;
	}

	/*
	 * This method exists since jdk 1.5 (java.util.regexp.Patter.quote())
	 */
	/**
	 * Quote.
	 *
	 * @param s
	 *            the s
	 *
	 * @return the string
	 */
	public static String quote(String s) {
		logger.debug("IN");
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1)
			return "\\Q" + s + "\\E";

		StringBuffer sb = new StringBuffer(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		logger.debug("OUT");
		return sb.toString();
	}

	/**
	 * Substitutes parameters with sintax "$P{parameter_name}" whose value is set in the map.
	 *
	 * @param statement
	 *            The string to be modified (tipically a query)
	 * @param valuesMap
	 *            Map name-value
	 * @param surroundWithQuotes
	 *            flag: if true, the replacement will be surrounded by quotes if they are missing
	 *
	 * @return The statement with profile attributes replaced by their values.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public static String substituteParametersInString(String statement, Map valuesMap, Map parType, boolean surroundWithQuotes) throws Exception {
		logger.debug("IN");

		boolean changePars = true;
		while (changePars) {
			// int profileAttributeStartIndex = statement.indexOf("$P{");
			int profileAttributeStartIndex = statement.indexOf("$P{");
			if (profileAttributeStartIndex != -1)
				statement = substituteParametersInString(statement, valuesMap, parType, profileAttributeStartIndex, surroundWithQuotes);
			else
				changePars = false;

		}
		logger.debug("OUT");
		return statement;
	}

	public static boolean isNull(String str) {
		return str == null;
	}

	public static boolean isEmpty(String str) {
		return isNull(str) || "".equals(str.trim());
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean containsOnlySpaces(String str) {
		return !isNull(str) && isEmpty(str);
	}

	public static Date stringToDate(String strDate, String format) throws Exception {
		logger.debug("IN");

		if (strDate == null || strDate.equals(""))
			return null;

		DateFormat df = new SimpleDateFormat(format);
		Date result = null;
		try {
			result = df.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error("Error while parsing the date " + strDate + ": " + e);
			throw new Exception("Error while parsing the date '" + strDate + ": " + e);
		}
		logger.debug("OUT");
		return result;
	}

	/**
	 * Substitutes the parameters with sintax "$P{attribute_name}" with the correspondent value in the string passed at input.
	 *
	 * @param statement
	 *            The string to be modified (tipically a query)
	 * @param userProfile
	 *            The IEngUserProfile object
	 * @param profileAttributeStartIndex
	 *            The start index for query parsing (useful for recursive calling)
	 * @param surroundWithQuotes
	 *            Flag: if true, the replacement will be surrounded by quotes if they are missing
	 *
	 * @return The statement with parameters replaced by their values.
	 * @throws Exception
	 */
	private static String substituteParametersInString(String statement, Map valuesMap, Map parTypeMap, int profileAttributeStartIndex,
			boolean surroundWithQuotes) throws Exception {
		logger.debug("IN");
		int profileAttributeEndIndex = statement.indexOf("}", profileAttributeStartIndex);
		if (profileAttributeEndIndex == -1)
			throw new Exception("Not closed profile attribute: '}' expected.");
		if (profileAttributeEndIndex < profileAttributeEndIndex)
			throw new Exception("Not opened profile attribute: '$P{' expected.");
		String attribute = statement.substring(profileAttributeStartIndex + 3, profileAttributeEndIndex).trim();

		String dequotePrefix = "_dequoted";
		if (attribute.endsWith(dequotePrefix)) {
			surroundWithQuotes = false;
		}

		int startConfigIndex = attribute.indexOf("(");
		String attributeName = "";
		String prefix = "";
		String split = "";
		String suffix = "";
		boolean attributeExcpetedToBeMultiValue = false;

		if (startConfigIndex != -1) {
			// the parameter is expected to be multivalue
			attributeExcpetedToBeMultiValue = true;
			int endConfigIndex = attribute.length() - 1;
			if (attribute.charAt(endConfigIndex) != ')')
				throw new Exception("Sintax error: \")\" missing. The expected sintax for " + "parameter is  $P{parameters} for singlevalue parameters. ");
			String configuration = attribute.substring(startConfigIndex + 1, endConfigIndex);
			String[] configSplitted = configuration.split(";");
			if (configSplitted == null || configSplitted.length != 3)
				throw new Exception("Sintax error. The expected sintax for parameters"
						+ "or $P{parameter} for singlevalue parameter. 'parameterName' must not contain '(' characters. "
						+ "The (prefix;split;suffix) is not properly configured");
			prefix = configSplitted[0];
			split = configSplitted[1];
			suffix = configSplitted[2];
			logger.debug("Multi-value parametet configuration found: prefix: '" + prefix + "'; split: '" + split + "'; suffix: '" + suffix + "'.");
			attributeName = attribute.substring(0, startConfigIndex);
			logger.debug("Expected multi-value parameter name: '" + attributeName + "'");
		} else {
			attributeName = attribute;
			logger.debug("Expected single-value parameter name: '" + attributeName + "'");
		}

		String value = (String) valuesMap.get(attributeName);
		if (value == null) {
			throw new Exception("Parameter '" + attributeName + "' not set.");

		} else {

			if (value.startsWith("' {"))
				value = value.substring(1);
			if (value.endsWith("}'"))
				value = value.substring(0, value.indexOf("}'") + 1);
			value = value.trim();
			logger.debug("Parameter value found: " + value);
			String replacement = null;
			String newListOfValues = null;
			if (attributeExcpetedToBeMultiValue) {
				if (value.startsWith("{")) {
					// the parameter is multi-value
					String[] values = findAttributeValues(value);
					logger.debug("N. " + values.length + " parameter values found: '" + values + "'");
					newListOfValues = values[0];
					for (int i = 1; i < values.length; i++) {
						newListOfValues = newListOfValues + split + values[i];
					}
				} else {
					logger.warn("The attribute value has not the sintax of a multi value parameter; considering it as a single value.");
					newListOfValues = value;
				}
			} else {
				if (value.startsWith("{")) {
					// the profile attribute is multi-value
					logger.warn("The attribute value seems to be a multi value parameter; trying considering it as a multi value using its own splitter and no prefix and suffix.");
					try {
						// checks the sintax
						String[] values = findAttributeValues(value);
						newListOfValues = values[0];
						for (int i = 1; i < values.length; i++) {
							newListOfValues = newListOfValues + value.charAt(1) + values[i];
						}
					} catch (Exception e) {
						logger.error("The attribute value does not respect the sintax of a multi value attribute; considering it as a single value.", e);
						newListOfValues = value;
					}
				} else {
					newListOfValues = value;
				}
			}

			replacement = prefix + newListOfValues + suffix;

			// if is specified a particular type for the parameter can add '' in case of String or Date
			String parType = null;
			if (parTypeMap != null) {
				parType = (String) parTypeMap.get(attributeName);
			}
			if (parType == null)
				parType = new String("");

			if (surroundWithQuotes || parType.equalsIgnoreCase("STRING") || parType.equalsIgnoreCase("DATE")) {
				if (!replacement.startsWith("'"))
					replacement = "'" + replacement;
				if (!replacement.endsWith("'"))
					replacement = replacement + "'";
			}

			attribute = quote(attribute);
			statement = statement.replaceAll("\\$P\\{" + attribute + "\\}", replacement);

			logger.debug("OUT");
		}

		return statement;

	}

	/**
	 * Substitutes parameters with sintax "$P{parameter_name}" whose value is set in the map. This is only for dataset, had to duplicate to handle null values,
	 * in case ogf null does not throw an exception but substitute null!
	 *
	 * @param statement
	 *            The string to be modified (tipically a query)
	 * @param valuesMap
	 *            Map name-value
	 * @param surroundWithQuotes
	 *            flag: if true, the replacement will be surrounded by quotes if they are missing
	 *
	 * @return The statement with profile attributes replaced by their values.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public static String substituteDatasetParametersInString(String statement, Map valuesMap, Map parType, boolean surroundWithQuotes) throws Exception {
		logger.debug("IN");

		boolean changePars = true;
		while (changePars) {
			// int profileAttributeStartIndex = statement.indexOf("$P{");
			int profileAttributeStartIndex = statement.indexOf("$P{");
			if (profileAttributeStartIndex != -1)
				statement = substituteDatasetParametersInString(statement, valuesMap, parType, profileAttributeStartIndex, surroundWithQuotes);
			else
				changePars = false;

		}
		logger.debug("OUT");
		return statement;
	}

	/**
	 * Substitutes the parameters with sintax "$P{attribute_name}" with the correspondent value in the string passed at input. Only for datatset parameters, had
	 * to duplicate to handle null values, not throw an exception but put null!
	 *
	 * @param statement
	 *            The string to be modified (tipically a query)
	 * @param userProfile
	 *            The IEngUserProfile object
	 * @param profileAttributeStartIndex
	 *            The start index for query parsing (useful for recursive calling)
	 * @param surroundWithQuotes
	 *            Flag: if true, the replacement will be surrounded by quotes if they are missing
	 *
	 * @return The statement with parameters replaced by their values.
	 * @throws Exception
	 */
	private static String substituteDatasetParametersInString(String statement, Map valuesMap, Map parTypeMap, int profileAttributeStartIndex,
			boolean surroundWithQuotes) throws Exception {
		logger.debug("IN");

		int profileAttributeEndIndex = statement.indexOf("}", profileAttributeStartIndex);
		if (profileAttributeEndIndex == -1)
			throw new Exception("Not closed profile attribute: '}' expected.");
		if (profileAttributeEndIndex < profileAttributeEndIndex)
			throw new Exception("Not opened profile attribute: '$P{' expected.");
		String attribute = statement.substring(profileAttributeStartIndex + 3, profileAttributeEndIndex).trim();

		String dequotePrefix = "_dequoted";
		if (attribute.endsWith(dequotePrefix)) {
			surroundWithQuotes = false;
		}

		int startConfigIndex = attribute.indexOf("(");
		String attributeName = "";
		String prefix = "";
		String split = "";
		String suffix = "";
		boolean attributeExcpetedToBeMultiValue = false;

		if (startConfigIndex != -1) {
			// the parameter is expected to be multivalue
			attributeExcpetedToBeMultiValue = true;
			int endConfigIndex = attribute.length() - 1;
			if (attribute.charAt(endConfigIndex) != ')')
				throw new Exception("Sintax error: \")\" missing. The expected sintax for " + "parameter is  $P{parameters} for singlevalue parameters. ");
			String configuration = attribute.substring(startConfigIndex + 1, endConfigIndex);
			// check the configuration content and add empty prefix/suffix as default if they are null
			if (configuration.equals(";,;"))
				configuration = " ;,; ";
			String[] configSplitted = configuration.split(";");
			if (configSplitted == null || configSplitted.length != 3)
				throw new Exception("Sintax error. The expected sintax for parameters"
						+ "or $P{parameter} for singlevalue parameter. 'parameterName' must not contain '(' characters. "
						+ "The (prefix;split;suffix) is not properly configured");
			prefix = configSplitted[0];
			split = configSplitted[1];
			suffix = configSplitted[2];
			logger.debug("Multi-value parameter configuration found: prefix: '" + prefix + "'; split: '" + split + "'; suffix: '" + suffix + "'.");
			attributeName = attribute.substring(0, startConfigIndex);
			logger.debug("Expected multi-value parameter name: '" + attributeName + "'");
		} else {
			attributeName = attribute;
			logger.debug("Expected single-value parameter name: '" + attributeName + "'");
		}

		String value = (String) valuesMap.get(attributeName);
		boolean isNullValue = false;
		if (value == null) {
			isNullValue = true;
			value = "null";
		}

		if (value.startsWith("' {"))
			value = value.substring(1);
		if (value.endsWith("}'"))
			value = value.substring(0, value.indexOf("}'") + 1);
		value = value.trim();
		logger.debug("Parameter value found: " + value);
		String replacement = null;
		String newListOfValues = null;

		// if is specified a particular type for the parameter can add '' in case of String or Date
		String parType = null;
		if (parTypeMap != null) {
			parType = (String) parTypeMap.get(attributeName);
		}
		if (parType == null)
			parType = new String("");

		if (attributeExcpetedToBeMultiValue) {
			if (value.startsWith("{")) {
				// the parameter is multi-value
				String[] values = findAttributeValues(value);
				logger.debug("N. " + values.length + " parameter values found: '" + values + "'");
				// newListOfValues = values[0];
				newListOfValues = ((values[0].startsWith(prefix))) ? "" : prefix + values[0] + ((values[0].endsWith(suffix)) ? "" : suffix);
				for (int i = 1; i < values.length; i++) {
					// newListOfValues = newListOfValues + split + values[i];
					String singleValue = ((values[i].startsWith(prefix))) ? "" : prefix + values[i] + ((values[i].endsWith(suffix)) ? "" : suffix);
					singleValue = checkParType(singleValue, parType, attribute);
					newListOfValues = newListOfValues + split + singleValue;
				}
			} else {
				logger.warn("The attribute value has not the sintax of a multi value parameter; considering it as a single value.");
				newListOfValues = value;
			}

		} else {
			if (value.startsWith("{")) {
				// the profile attribute is multi-value
				logger.warn("The attribute value seems to be a multi value parameter; trying considering it as a multi value using its own splitter and no prefix and suffix.");
				try {
					// checks the sintax
					String[] values = findAttributeValues(value);
					newListOfValues = values[0];
					for (int i = 1; i < values.length; i++) {
						newListOfValues = newListOfValues + value.charAt(1) + values[i];
					}
				} catch (Exception e) {
					logger.error("The attribute value does not respect the sintax of a multi value attribute; considering it as a single value.", e);
					newListOfValues = value;
				}
			} else {
				newListOfValues = value;
			}
		}
		String nullValueString = null;
		if (newListOfValues.equals("") || newListOfValues.equals("''") || newListOfValues.equals("null")) {
			try {
				nullValueString = SingletonConfig.getInstance().getConfigValue("DATA_SET_NULL_VALUE");
				if (nullValueString != null) {
					newListOfValues = "'" + nullValueString + "'";
				}
			} catch (Throwable e) {
				// try to read engine_config settings
				nullValueString = ((SourceBean) EnginConf.getInstance().getConfig().getAttribute("DATA_SET_NULL_VALUE")).getCharacters();
				if (nullValueString != null) {
					newListOfValues = "'" + nullValueString + "'";

				}
			}

		}
		replacement = ((newListOfValues.startsWith(prefix)) ? "" : prefix) + newListOfValues + ((newListOfValues.endsWith(suffix)) ? "" : suffix);

		if (!attributeExcpetedToBeMultiValue)
			replacement = checkParType(replacement, parType, attribute);
		// // check if numbers are number otherwise throw exception
		// try {
		// if (parType.equalsIgnoreCase("NUMBER")) {
		// replacement = replacement.replaceAll("\'", "");
		// Double double1 = Double.valueOf(replacement);
		// }
		// } catch (NumberFormatException e) {
		// String me = e.getMessage();
		// me += " - attribute " + attribute + " should be of number type";
		// NumberFormatException numberFormatException = new NumberFormatException(attribute);
		// numberFormatException.setStackTrace(e.getStackTrace());
		// throw numberFormatException;
		// }
		//
		// // check when type is RAW that there are not '' surrounding values (in case remove them)
		// // remotion done here in order to not modify SpagoBI Analytical driver of type string handling
		// try {
		// if (parType.equalsIgnoreCase("RAW")) {
		// logger.debug("Parmaeter is Raw type, check if there are '' and remove them");
		// if (replacement.length() > 2) {
		// if (replacement.startsWith("'")) {
		// logger.debug("first character is ', remove");
		// replacement = replacement.substring(1);
		// }
		// if (replacement.endsWith("'")) {
		// logger.debug("last character is ', remove");
		// replacement = replacement.substring(0, replacement.length() - 1);
		// }
		// }
		// }
		// } catch (Exception e) {
		// logger.error("Error in removing the '' in value " + replacement + " do not substitute them");
		// }
		//
		if (surroundWithQuotes || parType.equalsIgnoreCase("STRING") || parType.equalsIgnoreCase("DATE")) {
			if (!isNullValue) {
				if (!replacement.startsWith("'"))
					replacement = "'" + replacement;
				if (!replacement.endsWith("'"))
					replacement = replacement + "'";
			}
		}

		attribute = quote(attribute);
		statement = statement.replaceAll("\\$P\\{" + attribute + "\\}", replacement);

		// statement = statement.replaceAll("\\P\\{" + attribute + "\\}", replacement);
		/*
		 * profileAttributeStartIndex = statement.indexOf("$P{", profileAttributeEndIndex-1); if (profileAttributeStartIndex != -1) statement =
		 * substituteParametersInString(statement, valuesMap, profileAttributeStartIndex);
		 */
		logger.debug("OUT");

		return statement;

	}

	/**
	 * Check the correct validity of the parameter value
	 *
	 * @param replacement
	 *            : the parameter
	 * @param parType
	 *            : the parameter type
	 * @param attribute
	 *            : the attribute
	 * @return
	 */
	private static String checkParType(String replacement, String parType, String attribute) throws NumberFormatException {
		logger.debug("IN");
		String toReturn = replacement;
		// check if numbers are number otherwise throw exception
		try {
			if (parType.equalsIgnoreCase("NUMBER")) {
				toReturn = replacement.replaceAll("\'", "");
				toReturn = replacement.replaceAll(";", ",");
				if (toReturn.indexOf(",") >= 0) {
					// multivalues management
					String[] values = toReturn.split(",");
					for (int i = 0; i < values.length; i++) {
						Double double1 = Double.valueOf(values[i]);
					}
				} else {
					Double double1 = Double.valueOf(toReturn);
				}
			}
		} catch (NumberFormatException e) {
			String me = e.getMessage();
			me += " - attribute " + attribute + " should be of number type";
			NumberFormatException numberFormatException = new NumberFormatException(attribute);
			numberFormatException.setStackTrace(e.getStackTrace());
			throw numberFormatException;
		}

		// check when type is RAW that there are not '' surrounding values (in case remove them)
		// remotion done here in order to not modify SpagoBI Analytical driver of type string handling
		try {
			if (parType.equalsIgnoreCase("RAW")) {
				logger.debug("Parmaeter is Raw type, check if there are '' and remove them");
				if (toReturn.length() > 2) {
					if (toReturn.startsWith("'")) {
						logger.debug("first character is ', remove");
						toReturn = toReturn.substring(1);
					}
					if (toReturn.endsWith("'")) {
						logger.debug("last character is ', remove");
						toReturn = toReturn.substring(0, replacement.length() - 1);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in removing the '' in value " + toReturn + " do not substitute them");
		}

		logger.debug("OUT");
		return toReturn;
	}

	public static String convertStreamToString(InputStream is) throws IOException {

		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, DEFAULT_CHARSET));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	static public String[] convertCollectionInArray(Collection coll) {
		String[] array = new String[coll.size()];
		int i = 0;
		for (Iterator iterator = coll.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			String role = object.toString();
			array[i] = role;
			i++;
		}
		return array;
	}

	static public Collection convertArrayInCollection(String[] array) {
		Collection coll = new ArrayList();

		for (int i = 0; i < array.length; i++) {
			String role = array[i];
			coll.add(role);
		}
		return coll;
	}

	public static String getRandomString(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(random.nextInt(AB.length())));
		return sb.toString();
	}

	/**
	 * Format the string in html: replace \n with <br>
	 * .. and blank spaces &nbsp;
	 *
	 * @param s
	 * @return
	 */
	public static String fromStringToHTML(String s) {
		if (s != null) {
			s = s.replace(" ", "&nbsp;");
			s = s.replace("\n", "<br>");
		}
		return s;
	}

	/**
	 * Thanks to http://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
	 *
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public static String sha256(String s) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(s.getBytes(DEFAULT_CHARSET));
			byte[] digest = md.digest();
			String res = String.format("%064x", new java.math.BigInteger(1, digest));
			return res;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Error during hashing", e);
		}
	}

	public static String readStream(InputStream inputStream) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream, DEFAULT_CHARSET));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return stringBuilder.toString();
	}

	public static String getMultiValue(String value, String type) {
		String toReturn = "";

		String[] tempArrayValues = value.split(",");
		for (int j = 0; j < tempArrayValues.length; j++) {
			String tempValue = tempArrayValues[j];
			if (j == 0) {
				toReturn = getSingleValue(tempValue, type);
			} else {
				toReturn = toReturn + "," + getSingleValue(tempValue, type);
			}
		}

		return toReturn;
	}

	public static String getSingleValue(String value, String type) {
		String toReturn = "";
		value = value.trim();
		if (type.equalsIgnoreCase("")) {
			// this is the case of testing lov
			toReturn = value;
		} else if (type.equalsIgnoreCase("STRING") || type.equalsIgnoreCase("DATE")) {
			if (!(value.startsWith("'") && value.endsWith("'"))) {
				toReturn = "'" + value + "'";
			}
		} else if (type.equalsIgnoreCase("NUMBER")) {

			if ((value.startsWith("'") && value.endsWith("'"))) {
				toReturn = value.substring(1, value.length() - 1);
			} else {
				toReturn = value;
			}
			if (toReturn == null || toReturn.length() == 0) {
				toReturn = "0";
			}
		}
		return toReturn;
	}

}
