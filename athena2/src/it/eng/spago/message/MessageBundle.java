/**

 Copyright 2004, 2007 Engineering Ingegneria Informatica S.p.A.

 This file is part of Spago.

 Spago is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 Spago is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with Spago; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spago.message;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spagobi.utilities.messages.UTF8Control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Questa classe permette il recupero di stringhe censite in alcuni files di properties. Modifications by Davide Zerbetto on Jan 7th 2015: management of UTF-8
 * properties files.
 */
public class MessageBundle {

	private static final String DEFAULT_USER_LANGUAGE = "COMMON.default_user_language";
	private static final String DEFAULT_USER_COUNTRY = "COMMON.default_user_country";
	private static final String DEFAULT_BUNDLE = "messages";
	private static HashMap bundles = null;
	private static List bundlesNames = null;

	/**
	 * Retrieves the Locale of the current user checking for its language profile in session.
	 *
	 * @return The Locale corresponding to the current user.
	 */
	public static Locale getUserLocale() {
		String language = null;
		String country = null;
		Object defaultLanguage = ConfigSingleton.getInstance().getAttribute(DEFAULT_USER_LANGUAGE);
		Object defaultCountry = ConfigSingleton.getInstance().getAttribute(DEFAULT_USER_COUNTRY);
		RequestContainer requestContainer = RequestContainer.getRequestContainer();
		if (requestContainer == null)
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "MessageBundle::getMessage: requestContainer nullo");
		else {
			SessionContainer sessionContainer = requestContainer.getSessionContainer().getPermanentContainer();
			language = (String) sessionContainer.getAttribute(Constants.USER_LANGUAGE);
			country = (String) sessionContainer.getAttribute(Constants.USER_COUNTRY);
		} // if (requestContainer != null)
		if (language == null) {
			// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
			// "MessageBundle::getMessage: language non specificato in sessione");
			if (defaultLanguage != null) {
				language = (String) defaultLanguage;
			} else {
				// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
				// "MessageBundle::getMessage: language non specificato in common.xml imposta 'en'");
				language = "en";
			}
		} // if (language == null)
		if (country == null) {
			// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
			// "MessageBundle::getMessage: country non specificato in sessione");
			if (defaultCountry != null) {
				country = (String) defaultCountry;
			} else {
				// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
				// "MessageBundle::getMessage: country non specificato in common.xml imposta 'US'");
				country = "US";
			}
		} // if (country == null)
		Locale currentLocale = new Locale(language, country);
		return currentLocale;
	}

	/**
	 * Questo metodo recupera una stringa individuata da un codice e da alcune proprietà dell'utente che ha richiesto il servizio (language,country).
	 * <p>
	 *
	 * @param code
	 *            il codice associato alla stringa.
	 * @return la stringa recuperata da un file di properties.
	 */
	public static String getMessage(String code) {
		return getMessage(code, getUserLocale());
	}

	/**
	 * Questo metodo recupera una stringa individuata da un codice e da alcune proprietà dell'utente che ha richiesto il servizio (language,country).
	 * <p>
	 *
	 * @param code
	 *            il codice associato alla stringa.
	 * @param params
	 *            lista dei placeholder da sostituire nel messaggio.
	 * @return la stringa recuperata da un file di properties.
	 */
	public static String getMessage(String code, List params) {
		return substituteParams(getMessage(code), params);
	}

	/**
	 * Questo metodo recupera una stringa individuata da un codice e da alcune proprietà dell'utente che ha richiesto il servizio (language,country).
	 * <p>
	 *
	 * @param code
	 *            il codice associato alla stringa.
	 * @param bundle
	 *            il bundle da cui recuperare la stringa.
	 * @return la stringa recuperata da un file di properties.
	 */
	public static String getMessage(String code, String bundle) {
		String message = getMessage(code, bundle, getUserLocale());
		if (message == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "MessageBundle::getMessage: codice [" + code + "] non trovato");
			message = code;
		}
		return message;
	} // public String getMessage(String code)

	/**
	 * Questo metodo recupera una stringa individuata da un codice e da alcune proprietà dell'utente che ha richiesto il servizio (language,country).
	 * <p>
	 *
	 * @param code
	 *            il codice associato alla stringa.
	 * @param bundle
	 *            il bundle da cui recuperare la stringa.
	 * @param params
	 *            lista dei placeholder da sostituire nel messaggio.
	 * @return la stringa recuperata da un file di properties.
	 */
	public static String getMessage(String code, String bundle, List params) {
		return substituteParams(getMessage(code, bundle), params);
	}

	/**
	 * Questo metodo recupera una stringa individuata da un codice e da alcune proprietà dell'utente che ha richiesto il servizio (language,country).
	 * <p>
	 *
	 * @param code
	 *            il codice associato alla stringa.
	 * @param bundle
	 *            il bundle da cui recuperare la stringa.
	 * @return la stringa recuperata da un file di properties.
	 */
	public static String getMessage(final String code, final String bundle, final Locale userLocale) {

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
				// start modifications by Davide Zerbetto: managing UTF-8 properties files
				// messages = ResourceBundle.getBundle(bundle, userLocale);
				// start modifications by Alessandro Portosa: managing properties files according to Zanata needs
				if (userLocale.equals(Locale.US)) {
					messages = ResourceBundle.getBundle(bundle, Locale.ROOT, new UTF8Control());
				} else {
					messages = ResourceBundle.getBundle(bundle, userLocale, new UTF8Control());
				}
				// end modifications by Alessandro Portosa: managing properties files according to Zanata needs
				// end modifications by Davide Zerbetto: managing UTF-8 properties files
			} catch (java.util.MissingResourceException ex) {
				// Bundle non esistente
			}

			// Put bundle in cache
			bundles.put(bundleKey, messages);
		}

		if (messages == null) {
			// Bundle non existent
			return null;
		} // if (messages == null)

		String message = null;
		try {
			message = messages.getString(code);
		} // try
		catch (Exception ex) {
			// No trace: may be this is not an error
		} // catch (Exception ex)
		return message;
	} // public String getMessage(String code)

	/**
	 * Questo metodo recupera una stringa individuata da un codice e da alcune proprietà dell'utente che ha richiesto il servizio (language,country).
	 * <p>
	 *
	 * @param code
	 *            il codice associato alla stringa.
	 * @param bundle
	 *            il bundle da cui recuperare la stringa.
	 * @return la stringa recuperata da un file di properties.
	 */
	public static String getMessage(final String code, final Locale userLocale) {
		for (Iterator it = bundlesNames.iterator(); it.hasNext();) {
			String bundleName = (String) it.next();
			String message = getMessage(code, bundleName, userLocale);
			if (message != null) {
				return message;
			}
		}
		TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "MessageBundle::getMessage: codice [" + code + "] non trovato");
		// Return message code instead of message
		return code;
	} // public String getMessage(String code)

	/**
	 * Add a bundle name in the lookup list.
	 *
	 * @param bundleName
	 *            Name of the bundle to add to the lookup list.
	 */
	public static void addBundleName(final String bundleName) {
		if (!bundlesNames.contains(bundleName)) {
			bundlesNames.add(bundleName);
		}
	}

	/**
	 * Retrieve a message in a hierarchical way: first it search in the path "properties.action.<action name>.code" or "properties.page.<page name>.code", if
	 * the search fails it searches in the path "action.<action name>.code" or "page.<page name>.code" in the files enlisted in messages.xml, and if it fails
	 * again it searches in the path "code" in the files enlisted in messages.xml.
	 *
	 * @param code
	 *            Identifier of the message
	 * @param responseContainer
	 * @return The message
	 */
	public static String getServiceMessage(final String code, final ResponseContainer responseContainer) {
		String businessType = responseContainer.getBusinessType().toLowerCase();
		String businessName = responseContainer.getBusinessName().toLowerCase();
		String businessTypeAndName = businessType + "." + businessName;
		String bundleName = "properties." + businessTypeAndName;
		Locale userLocale = getUserLocale();

		String message = getMessage(code, bundleName, userLocale);
		if (message == null) {
			String remappedCode = (businessTypeAndName + "." + code).toUpperCase();
			message = MessageBundle.getMessage(remappedCode);

			if (message.equals(remappedCode)) {
				// Try default handling
				message = getMessage(code, userLocale);
			}
		}
		return message;
	}

	/**
	 * Replace the placeholders with the elements supplied in the list
	 *
	 * @param message
	 *            Original message
	 * @param params
	 *            Value to sbstitute for placeholders
	 * @return Modified message
	 */
	public static String substituteParams(String message, List params) {
		if (params == null) {
			return message;
		}
		if (message != null) {
			for (int i = 0; i < params.size(); i++) {
				String toParse = message;
				String replacing = "%" + i;
				String replaced = (String) params.get(i);
				StringBuffer parsed = new StringBuffer();
				int parameterIndex = toParse.indexOf(replacing);
				while (parameterIndex != -1) {
					parsed.append(toParse.substring(0, parameterIndex));
					parsed.append(replaced);
					toParse = toParse.substring(parameterIndex + replacing.length(), toParse.length());
					parameterIndex = toParse.indexOf(replacing);
				} // while (parameterIndex != -1)
				parsed.append(toParse);
				message = parsed.toString();
			} // for (int i = 0; i < params.size(); i++)
		} // if (message != null)

		return message;
	}

	static {
		bundlesNames = new ArrayList();
		bundlesNames.add(DEFAULT_BUNDLE);

		bundles = new HashMap();
	}

} // public class MessageBundle
