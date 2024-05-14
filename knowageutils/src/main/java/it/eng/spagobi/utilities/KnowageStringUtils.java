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
package it.eng.spagobi.utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * The Class StringUtils.
 *
 * @author Andrea Gioia
 */
public class KnowageStringUtils {

	protected static Logger logger = Logger.getLogger(KnowageStringUtils.class);

	private static final String PLAIN_ASCII = "AaEeIiOoUu" // grave
			+ "AaEeIiOoUuYy" // acute
			+ "AaEeIiOoUuYy" // circumflex
			+ "AaOoNn" // tilde
			+ "AaEeIiOoUuYy" // umlaut
			+ "Aa" // ring
			+ "Cc" // cedilla
			+ "OoUu" // double acute
	;

	private static final String UNICODE = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
			+ "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
			+ "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
			+ "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" + "\u00C5\u00E5" + "\u00C7\u00E7" + "\u0150\u0151\u0170\u0171";

	// remove accentued from a string and replace with ascii equivalent
	// See http://www.rgagnon.com/javadetails/java-0456.html
	public static String convertNonAscii(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			int pos = UNICODE.indexOf(c);
			if (pos > -1) {
				sb.append(PLAIN_ASCII.charAt(pos));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Replace parameters.
	 *
	 * @param filterCondition
	 *            the filter condition
	 * @param parameterTypeIdentifier
	 *            the parameter type identifier
	 * @param parameters
	 *            the parameters
	 *
	 * @return the string
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String replaceParameters(String filterCondition, String parameterTypeIdentifier, Properties parameters) throws IOException {
		String result = filterCondition;
		Set params;

		params = getParameters(filterCondition, parameterTypeIdentifier);
		Iterator it = params.iterator();
		while (it.hasNext()) {
			String parameterName = (String) it.next();
			String parameterValue = parameters.getProperty(parameterName);
			if (parameterValue == null)
				throw new IOException("No value for the parameter: " + parameterName);
			result = filterCondition.replaceAll(parameterTypeIdentifier + "\\{" + parameterName + "\\}", parameterValue);
		}

		return result;
	}

	/**
	 * Replace parameters.
	 *
	 * @param filterCondition
	 *            the filter condition
	 * @param parameterTypeIdentifier
	 *            the parameter type identifier
	 * @param parameters
	 *            the parameters
	 *
	 * @return the string
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String replaceParameters(String filterCondition, String parameterTypeIdentifier, Map parameters) throws IOException {
		String result = filterCondition;

		logger.debug("query string:  " + filterCondition);
		Set params;
		Map paramsFromEnv = new HashMap<>();
		Map parTypeMap = new HashMap<>();
		params = getParameters(filterCondition, parameterTypeIdentifier);
		Iterator it = params.iterator();
		while (it.hasNext()) {
			String parameterName = (String) it.next();
			logger.debug("parameterName:  " + parameterName);
			if (!parameters.containsKey(parameterName)) {
				logger.error("No value for the parameter: " + parameterName);
				throw new IOException("No value for the parameter: " + parameterName);
			}
			String parameterValue = parameters.get(parameterName) == null ? null : parameters.get(parameterName).toString();

			logger.debug("parameterValue:  " + parameterValue);
			String parameterType = parameters.get(parameterName + SpagoBIConstants.PARAMETER_TYPE) == null ? null
					: parameters.get(parameterName + SpagoBIConstants.PARAMETER_TYPE).toString();

			logger.debug("parameterType:  " + parameterType);
			paramsFromEnv.put(parameterName, parameterValue);
			parTypeMap.put(parameterName, parameterType);

			/*
			 * parameterValue = escapeHQL(parameterValue); result = result.replaceAll("\\" + parameterTypeIdentifier + "\\{" + parameterName + "\\}",
			 * parameterValue);
			 */

		}
		try {
			result = StringUtilities.substituteDatasetParametersInString(result, paramsFromEnv, parTypeMap, false);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while settin up parameters", e);
		}

		return result;
	}

	/**
	 * Gets the parameters.
	 *
	 * @param str
	 *            the str
	 * @param parameterTypeIdentifier
	 *            the parameter type identifier
	 *
	 * @return the parameters
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Set getParameters(String str, String parameterTypeIdentifier) throws IOException {
		Set parameters = new HashSet();
		int fromIndex = 0;
		int beginIndex = -1;
		int endIndex = -1;
		while ((beginIndex = str.indexOf(parameterTypeIdentifier + "{", fromIndex)) != -1) {
			endIndex = str.indexOf("}", beginIndex);
			if (endIndex == -1)
				throw new IOException("Malformed parameter: " + str.substring(beginIndex));
			String parameter = "";
			if (parameterTypeIdentifier.equals("$P")) {
				parameter = str.substring(beginIndex + 3, endIndex).trim();
			} else {
				parameter = str.substring(beginIndex + 2, endIndex).trim();
			}
			logger.debug("Parameter name : " + parameter);
			parameters.add(parameter);
			fromIndex = endIndex;
		}

		logger.debug("parameters set size: " + parameters.size());
		return parameters;
	}

	/**
	 * Escapes the input string as a HQL static operand. At the time being, it replaces "'" with "''"
	 *
	 * @param parameter
	 *            the parameter to be escaped
	 * @return the escaped String
	 */
	public static String escapeHQL(String parameter) {
		String toReturn = null;
		if (parameter != null) {
			toReturn = parameter.replaceAll("'", "''");
		}
		return toReturn;
	}

	/**
	 * Joins the input string array into a unique string using the specified separator
	 *
	 * @param strings
	 *            The strings to be joined
	 * @param separator
	 * @return Joins the input string array into a unique string using the specified separator
	 */
	public static String join(String[] strings, String separator) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strings.length; i++) {
			if (i != 0)
				sb.append(separator);
			sb.append(strings[i]);
		}
		return sb.toString();
	}

	/**
	 * Joins the input collection of string into a unique string using the specified separator
	 *
	 * @param strings
	 *            The collection to be joined
	 * @param separator
	 * @return Joins the input collection of string into a unique string using the specified separator
	 */
	public static String join(Collection<String> strings, String separator) {
		StringBuffer sb = new StringBuffer();
		Iterator<String> i = strings.iterator();
		while (i.hasNext()) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	/**
	 * Joins the input collection of string into a unique string using the specified separator
	 *
	 * @param objects
	 *            The collection to be joined
	 * @param separator
	 * @param prefix
	 *            The prefix to be applied on each element
	 * @return Joins the input collection of string into a unique string using the specified separator
	 */
	public static String join(Collection<Object> objects, String separator, String prefix) {
		StringBuffer sb = new StringBuffer();
		Iterator<Object> i = objects.iterator();
		while (i.hasNext()) {
			sb.append(prefix);
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	/**
	 * escape all the occurences of '. As escape char use ' so all the ' in the original string will be replaced with ''
	 *
	 * @param the
	 *            string that must be escaped
	 *
	 * @return the escaped string
	 */
	public static String escapeQuotes(String str) {
		return escape(str, '\'', '\'');
	}

	/**
	 * escape all the occurences of c. As escape char use escapeChar so all the c in the original string will be replaced with escapeChar + c
	 *
	 * @param str
	 *            the string that must be escaped
	 * @param c
	 *            the char to escape
	 * @param escapeChar
	 *            the char that will be use to escape
	 *
	 * @return the escaped string
	 */
	public static String escape(String str, char c, char escapeChar) {
		if (str == null)
			return null;
		return str.replace("" + c, "" + escapeChar + c);
	}

	public static boolean isBounded(String str, String boundingStr) {
		return (str.startsWith(boundingStr) && str.endsWith(boundingStr));
	}

	public static String bound(String str, String boundingStr) {
		return boundingStr + str + boundingStr;
	}

	/**
	 * Escapes the characters for the html code: ' --> &#39;
	 *
	 * @param str
	 * @return
	 */
	public static String escapeForHtml(String str) {
		str = it.eng.spago.util.StringUtils.replace(str, "'", "&#39;");
		return str;
	}

	public static String escapeForSQLColumnName(String fieldName) {
		return fieldName == null ? "" : fieldName.trim().replaceAll("'", "").replaceAll("\"", "");
	}

	public static boolean matchesLikeNotLikeCriteria(String tableName, String tableNamePatternLike, String tableNamePatternNotLike) {
		boolean result;
		String[] tableNamePatternLikeTmp = null;
		String[] tableNamePatternNotLikeTmp = null;

		if ((tableNamePatternLike != null && !tableNamePatternLike.isEmpty()) && (tableNamePatternNotLike != null && !tableNamePatternNotLike.isEmpty())) {

			tableNamePatternLikeTmp = tableNamePatternLike.toUpperCase().trim().replaceAll(" ", "").split(",", -1);
			tableNamePatternNotLikeTmp = tableNamePatternNotLike.toUpperCase().trim().replaceAll(" ", "").split(",", -1);
			result = false;
			for (String likePattern : tableNamePatternLikeTmp) {
				result |= tableName.toUpperCase().startsWith(likePattern);

				for (String notLikePattern : tableNamePatternNotLikeTmp) {
					result &= !tableName.toUpperCase().startsWith(notLikePattern);
				}
			}
		} else if (tableNamePatternLike != null && !tableNamePatternLike.isEmpty()) {
			tableNamePatternLikeTmp = tableNamePatternLike.toUpperCase().trim().replaceAll(" ", "").split(",", -1);
			result = false;
			for (String likePattern : tableNamePatternLikeTmp) {
				result |= tableName.toUpperCase().startsWith(likePattern);
			}
		} else if (tableNamePatternNotLike != null && !tableNamePatternNotLike.isEmpty()) {
			tableNamePatternNotLikeTmp = tableNamePatternNotLike.toUpperCase().trim().replaceAll(" ", "").split(",", -1);
			result = true;
			for (String notLikePattern : tableNamePatternNotLikeTmp) {
				result &= !tableName.toUpperCase().startsWith(notLikePattern);
			}
		} else {
			result = true;
		}

		return result;
	}
	
	/**
     * Imported from apache.commons
     */
    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("'", "''");
    }
}
