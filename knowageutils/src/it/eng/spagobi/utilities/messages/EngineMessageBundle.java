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
package it.eng.spagobi.utilities.messages;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class EngineMessageBundle {

	private static final String DEFAULT_BUNDLE = "messages";
	private static HashMap bundles = null;

	static {
		bundles = new HashMap();
	}

	/**
	 * Returns an internazionalized message.
	 *
	 * @param code
	 *            the code of the message.
	 * @param bundle
	 *            the message bundle.
	 * @param userLocale
	 *            the user locale
	 *
	 * @return the internazionalized message.
	 */
	public static String getMessage(String code, String bundle, Locale userLocale) {

		if (code == null)
			return null;
		if (userLocale == null)
			return code;
		// logger.debug("Input parameters: code = [" + code + "] ; bundle = [" + bundle + "] ; " +
		// "userlocale = [" + userLocale + "]");
		if (bundle == null || bundle.trim().equals("")) {
			// logger.debug("Bundle not specified; considering \"" + DEFAULT_BUNDLE + "\" as default value");
			bundle = DEFAULT_BUNDLE;
		}

		String bundleKey;
		// start modifications by Alessandro Portosa: managing properties files according to Zanata needs
		if (userLocale.equals(Locale.US)) {
			bundleKey = bundle;
		} else {
			bundleKey = bundle + "_" + userLocale.getLanguage() + "_" + userLocale.getCountry();
		}
		// end modifications by Alessandro Portosa: managing properties files according to Zanata needs
		ResourceBundle messages = null;
		if (bundles.containsKey(bundleKey)) {
			messages = (ResourceBundle) bundles.get(bundleKey);
		} else {
			// First access to this bundle
			try {
				// start modifications by Alessandro Portosa: managing properties files according to Zanata needs
				if (userLocale.equals(Locale.US)) {
					messages = ResourceBundle.getBundle(bundle, Locale.ROOT, new UTF8Control());
				} else {
					messages = ResourceBundle.getBundle(bundle, userLocale, new UTF8Control());
				}
				// end modifications by Alessandro Portosa: managing properties files according to Zanata needs
			} catch (java.util.MissingResourceException ex) {
				// logger.error("ResourceBundle with bundle = [" + bundle + "] and locale = " +
				// "[" + userLocale + "] missing.");
			}

			// Put bundle in cache
			bundles.put(bundleKey, messages);
		}

		if (messages == null) {
			// Bundle non existent
			return code;
		} // if (messages == null)

		String message = null;
		try {
			message = messages.getString(code);
		} // try
		catch (Exception ex) {
			// No trace: may be this is not an error
		} // catch (Exception ex)
		if (message == null)
			return code;
		else
			return message;
	}

	/**
	 * Gets the message.
	 *
	 * @param code
	 *            the code
	 * @param userLocale
	 *            the user locale
	 *
	 * @return the message
	 */
	public static String getMessage(String code, Locale userLocale) {
		return getMessage(code, DEFAULT_BUNDLE, userLocale);
	}

	/**
	 * Gets the message.
	 *
	 * @param code
	 *            the code
	 * @param bundle
	 *            the bundle
	 * @param userLocale
	 *            the user locale
	 * @param arguments
	 *            the arguments
	 *
	 * @return the message
	 */
	public static String getMessage(String code, String bundle, Locale userLocale, String[] arguments) {
		String message = getMessage(code, bundle, userLocale);
		for (int i = 0; i < arguments.length; i++) {
			message = replace(message, i, arguments[i].toString());
		}
		return message;
	}

	/**
	 * Gets the message.
	 *
	 * @param code
	 *            the code
	 * @param userLocale
	 *            the user locale
	 * @param arguments
	 *            the arguments
	 *
	 * @return the message
	 */
	public static String getMessage(String code, Locale userLocale, String[] arguments) {
		return getMessage(code, DEFAULT_BUNDLE, userLocale, arguments);
	}

	/**
	 * Substitutes the message value to the placeholders.
	 *
	 * @param messageFormat
	 *            The String representing the message format
	 * @param iParameter
	 *            The numeric value defining the replacing string
	 * @param value
	 *            Input object containing parsing information
	 * @return The parsed string
	 */
	protected static String replace(String messageFormat, int iParameter, Object value) {
		if (value != null) {
			String toParse = messageFormat;
			String replacing = "%" + iParameter;
			String replaced = value.toString();
			StringBuffer parsed = new StringBuffer();
			int parameterIndex = toParse.indexOf(replacing);
			while (parameterIndex != -1) {
				parsed.append(toParse.substring(0, parameterIndex));
				parsed.append(replaced);
				toParse = toParse.substring(parameterIndex + replacing.length(), toParse.length());
				parameterIndex = toParse.indexOf(replacing);
			} // while (parameterIndex != -1)
			parsed.append(toParse);
			return parsed.toString();
		} else {
			return messageFormat;
		}
	}

}
