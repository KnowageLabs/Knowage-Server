/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EngineMessageBundle {

	private static final Logger LOGGER = LogManager.getLogger(EngineMessageBundle.class);
	private static final String DEFAULT_BUNDLE = "messages";
	private static final Map<String, ResourceBundle> bundles = new HashMap<>();

	/**
	 * Returns an internazionalized message.
	 *
	 * @param code       the code of the message.
	 * @param bundle     the message bundle.
	 * @param userLocale the user locale
	 *
	 * @return the internazionalized message.
	 */
	public static String getMessage(String code, String bundle, Locale userLocale) {

		if (code == null)
			return null;
		if (userLocale == null)
			return code;
		if (bundle == null || bundle.trim().equals("")) {
			bundle = DEFAULT_BUNDLE;
		}

		String bundleKey = bundle + "_" + userLocale.getLanguage() + "_" + userLocale.getCountry();
		ResourceBundle messages = null;
		if (bundles.containsKey(bundleKey)) {
			messages = bundles.get(bundleKey);
		} else {
			// First access to this bundle
			try {
				messages = ResourceBundle.getBundle(bundle, userLocale);
			} catch (java.util.MissingResourceException ex) {
				LOGGER.atWarn().withThrowable(ex).log("Non fatal error getting message with code {}, bundle {} and locale {}", code, bundleKey, userLocale);
			}

			// Put bundle in cache
			bundles.put(bundleKey, messages);
		}

		if (messages == null) {
			// Bundle non existent
			return code;
		}

		String message = null;
		try {
			message = messages.getString(code);
		} catch (Exception ex) {
			// No trace: may be this is not an error
		}
		if (message == null)
			return code;
		else
			return message;
	}

	/**
	 * Gets the message.
	 *
	 * @param code       the code
	 * @param userLocale the user locale
	 *
	 * @return the message
	 */
	public static String getMessage(String code, Locale userLocale) {
		return getMessage(code, DEFAULT_BUNDLE, userLocale);
	}

	/**
	 * Gets the message.
	 *
	 * @param code       the code
	 * @param bundle     the bundle
	 * @param userLocale the user locale
	 * @param arguments  the arguments
	 *
	 * @return the message
	 */
	public static String getMessage(String code, String bundle, Locale userLocale, String[] arguments) {
		String message = getMessage(code, DEFAULT_BUNDLE, userLocale);
		for (int i = 0; i < arguments.length; i++) {
			message = replace(message, i, arguments[i].toString());
		}
		return message;
	}

	/**
	 * Gets the message.
	 *
	 * @param code       the code
	 * @param userLocale the user locale
	 * @param arguments  the arguments
	 *
	 * @return the message
	 */
	public static String getMessage(String code, Locale userLocale, String[] arguments) {
		return getMessage(code, DEFAULT_BUNDLE, userLocale, arguments);
	}

	/**
	 * Substitutes the message value to the placeholders.
	 *
	 * @param messageFormat The String representing the message format
	 * @param iParameter    The numeric value defining the replacing string
	 * @param value         Input object containing parsing information
	 * @return The parsed string
	 */
	protected static String replace(String messageFormat, int iParameter, Object value) {
		if (value != null) {
			String toParse = messageFormat;
			String replacing = "%" + iParameter;
			String replaced = value.toString();
			StringBuilder parsed = new StringBuilder();
			int parameterIndex = toParse.indexOf(replacing);
			while (parameterIndex != -1) {
				parsed.append(toParse.substring(0, parameterIndex));
				parsed.append(replaced);
				toParse = toParse.substring(parameterIndex + replacing.length());
				parameterIndex = toParse.indexOf(replacing);
			}
			parsed.append(toParse);
			return parsed.toString();
		} else {
			return messageFormat;
		}
	}

	private EngineMessageBundle() {

	}

}
