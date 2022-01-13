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
package it.eng.spagobi.commons.utilities.messages;

import java.util.Locale;
import java.util.Locale.Builder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerAccess;
import it.eng.spago.base.RequestContainerPortletAccess;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.message.MessageBundle;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.utilities.messages.IEngineMessageBuilder;

/**
 *
 * DATE CONTRIBUTOR/DEVELOPER NOTE 19-04-2013 Antonella Giachino (antonella.giachino@eng.it) Andrea Fantappiè (andrea.fantappiè@eng.it) Added
 * internationalization management for highchart engine
 *
 */

// Referenced classes of package it.eng.spagobi.commons.utilities.messages:
// IMessageBuilder

public class MessageBuilder implements IMessageBuilder, IEngineMessageBuilder {

	private static Logger logger = Logger.getLogger(MessageBuilder.class);
	private static final String MESSAGES_FOLDER = "MessageFiles.";

	public MessageBuilder() {
	}

	@Override
	public String getMessageTextFromResource(String resourceName, Locale locale) {
		logger.debug((new StringBuilder("IN-resourceName:")).append(resourceName).toString());
		logger.debug((new StringBuilder("IN-locale:")).append(locale == null ? "null" : locale.toString()).toString());
		if (!isValidLocale(locale)) {
			logger.warn((new StringBuilder("Request locale ")).append(locale).append(" in input is not valid since it is null or not configured.").toString());
			locale = GeneralUtilities.getDefaultLocale();
		}
		String message = "";
		try {
			String resourceNameLoc = (new StringBuilder(String.valueOf(resourceName))).append("_").append(locale.getLanguage()).append("_")
					.append(locale.getCountry()).toString();
			ClassLoader classLoad = getClass().getClassLoader();
			java.io.InputStream resIs = classLoad.getResourceAsStream(resourceNameLoc);
			if (resIs == null) {
				logger.warn((new StringBuilder("Cannot find resource ")).append(resourceName).toString());
				resIs = classLoad.getResourceAsStream(resourceName);
			}
			byte resBytes[] = GeneralUtilities.getByteArrayFromInputStream(resIs);
			message = new String(resBytes);
		} catch (Exception e) {
			message = "";
			logger.warn((new StringBuilder("Error while recovering text of the resource name ")).append(resourceName).toString(), e);
		}
		logger.debug((new StringBuilder("OUT-message:")).append(message).toString());
		return message;
	}

	@Override
	public String getMessage(String code) {
		Locale locale = getLocale(null);
		return getMessageInternal(code, null, locale);
	}

	@Override
	public String getMessage(String code, Locale locale) {
		if (!isValidLocale(locale)) {
			logger.warn((new StringBuilder("Request locale ")).append(locale).append(" in input is not valid since it is null or not configured.").toString());
			locale = GeneralUtilities.getDefaultLocale();
		}
		return getMessageInternal(code, null, locale);
	}

	@Override
	public String getMessage(String code, String bundle) {
		Locale locale = getLocale(null);
		return getMessageInternal(code, bundle, locale);
	}

	@Override
	public String getMessage(String code, String bundle, Locale locale) {
		if (!isValidLocale(locale)) {
			logger.warn((new StringBuilder("Request locale ")).append(locale).append(" in input is not valid since it is null or not configured.").toString());
			locale = GeneralUtilities.getDefaultLocale();
		}
		return getMessageInternal(code, bundle, locale);
	}

	@Override
	public String getMessage(String code, HttpServletRequest request) {
		Locale locale = getLocale(request);
		return getMessageInternal(code, null, locale);
	}

	@Override
	public String getMessage(String code, HttpServletRequest request, Locale locale) {
		if (!isValidLocale(locale)) {
			logger.warn((new StringBuilder("Request locale ")).append(locale).append(" in input is not valid since it is null or not configured.").toString());
			locale = GeneralUtilities.getDefaultLocale();
		}
		return getMessageInternal(code, null, locale);
	}

	@Override
	public String getMessage(String code, String bundle, HttpServletRequest request) {
		Locale locale = getLocale(request);
		return getMessageInternal(code, bundle, locale);
	}

	@Override
	public String getMessage(String code, String bundle, HttpServletRequest request, Locale locale) {
		if (!isValidLocale(locale)) {
			logger.warn((new StringBuilder("Request locale ")).append(locale).append(" in input is not valid since it is null or not configured.").toString());
			locale = GeneralUtilities.getDefaultLocale();
		}
		return getMessageInternal(code, bundle, locale);
	}

	private String getMessageInternal(String code, String bundle, Locale locale) {
		logger.debug((new StringBuilder("IN-code:")).append(code).toString());
		logger.debug((new StringBuilder("bundle:")).append(bundle).toString());
		logger.debug((new StringBuilder("locale:")).append(locale).toString());
		String message = null;
		if (bundle == null) {
			message = MessageBundle.getMessage(code, locale);
		} else {
			message = MessageBundle.getMessage(code, MESSAGES_FOLDER + bundle, locale);
		}
		if (message == null || message.trim().equals("")) {
			message = code;
		}
		logger.debug((new StringBuilder("OUT-message:")).append(message).toString());
		return message;
	}

	public static Locale getBrowserLocaleFromSpago() {
		logger.debug("IN");
		Locale browserLocale = null;
		RequestContainer reqCont = RequestContainer.getRequestContainer();
		if (reqCont != null) {
			Object obj = reqCont.getInternalRequest();
			if (obj != null && (obj instanceof HttpServletRequest)) {
				HttpServletRequest request = (HttpServletRequest) obj;
				Locale reqLocale = request.getLocale();
				String language = reqLocale.getLanguage();
				String country = reqLocale.getCountry();
				if (StringUtils.isBlank(country)) {
					country = GeneralUtilities.getCountry(language);
				}
				browserLocale = new Locale(language, country);

			}
		}
		if (browserLocale == null) {
			browserLocale = GeneralUtilities.getDefaultLocale();
		}
		logger.debug("OUT");
		return browserLocale;
	}

	private Locale getBrowserLocale(HttpServletRequest request) {
		logger.debug("IN");
		Locale browserLocale = null;
		Locale reqLocale = request.getLocale();
		String language = reqLocale.getLanguage();
		String country = GeneralUtilities.getCountry(language);

		String script = reqLocale.getScript();
		Builder tmpLocale = new Builder().setLanguage(language).setRegion(country);
		if (StringUtils.isNotBlank(script)) {
			tmpLocale.setScript(script);
		}
		browserLocale = tmpLocale.build();

		if (browserLocale == null) {
			browserLocale = GeneralUtilities.getDefaultLocale();
		}
		logger.debug("OUT");
		return browserLocale;
	}

	public Locale getLocale(HttpServletRequest request) {
		logger.debug("IN");
		String sbiMode = getSpagoBIMode(request);
		UserProfile profile = null;
		Locale locale = null;
		if (sbiMode.equalsIgnoreCase("WEB")) {
			String language = null;
			String country = null;
			String script = null;

			RequestContainer reqCont = RequestContainer.getRequestContainer();
			if (reqCont != null) {
				SessionContainer sessCont = reqCont.getSessionContainer();
				SessionContainer permSess = sessCont.getPermanentContainer();
				language = (String) permSess.getAttribute("AF_LANGUAGE");
				country = (String) permSess.getAttribute("AF_COUNTRY");
				script = (String) permSess.getAttribute("AF_SCRIPT");
				profile = (UserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			}

			if (country == null) {
				country = "";
			}

			if (script == null) {
				script = "";
			}

			if (profile != null && !profile.getUserId().equals(SpagoBIConstants.PUBLIC_USER_ID) && language != null) {
				// check preference from user attributes if presents
//				try {
//					String userLocale = (String) profile.getUserAttribute("language");
//					if (StringUtils.isNotBlank(userLocale)) {
//						language = userLocale.substring(0, userLocale.indexOf("_"));
//						country = userLocale.substring(userLocale.indexOf("_") + 1);
//						logger.info("User attribute language: " + language);
//						logger.info("User attribute country: " + country);
//					}
//				} catch (Exception e) {
//					logger.debug("Error on reading user attribute language: " + e);
//				}

				locale = new Builder().setLanguage(language).setRegion(country).setScript(script).build();
			} else if (request == null) {
				locale = getBrowserLocaleFromSpago();
			} else {
				locale = getBrowserLocale(request);
			}
		} else if (sbiMode.equalsIgnoreCase("PORTLET")) {
			locale = PortletUtilities.getPortalLocale();
		}
		if (!isValidLocale(locale)) {
			logger.warn((new StringBuilder("Request locale ")).append(locale).append(" not valid since it is not configured.").toString());
			locale = GeneralUtilities.getDefaultLocale();
			logger.debug((new StringBuilder("Using default locale ")).append(locale).append(".").toString());
		} else if (StringUtilities.isEmpty(locale.getCountry())) {
			logger.warn((new StringBuilder("Request locale ")).append(locale)
					.append(" not contain the country value. The one specified in configuration will be used").toString());
//			SingletonConfig spagobiConfig = SingletonConfig.getInstance();
//
//			String country = GeneralUtilities.getCountry(locale.getLanguage());
			locale = GeneralUtilities.getDefaultLocale();
		}
		logger.debug((new StringBuilder("OUT-locale:")).append(locale == null ? "null" : locale.toString()).toString());
		return locale;
	}

	private boolean isValidLocale(Locale locale) {
		logger.info("IN");

		String language;
		String country;

		if (locale == null)
			return false;

		try {
			language = locale.getLanguage();
			country = locale.getCountry();

			if (StringUtilities.isEmpty(locale.getCountry())) {
				return true;
			} else if (locale.getCountry().equalsIgnoreCase(country)) {
				String script = GeneralUtilities.getScript(language);

				if (!StringUtils.isBlank(script)) {
					return locale.getScript().equalsIgnoreCase(script);
				} else {
					return true;
				}
			} else {
				return false;
			}
		} finally {
			logger.info("OUT");
		}
	}

	public String getSpagoBIMode(HttpServletRequest request) {
		logger.debug("IN");
		String sbiMode = null;
		if (request != null) {
			RequestContainer aRequestContainer = null;
			aRequestContainer = RequestContainerPortletAccess.getRequestContainer(request);
			if (aRequestContainer == null) {
				aRequestContainer = RequestContainerAccess.getRequestContainer(request);
			}
			String channelType = aRequestContainer.getChannelType();
			if ("PORTLET".equalsIgnoreCase(channelType)) {
				sbiMode = "PORTLET";
			} else {
				sbiMode = "WEB";
			}
		} else {
			sbiMode = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGOBI-MODE.mode");
			if (sbiMode == null) {
				logger.error("SPAGOBI.SPAGOBI-MODE.mode IS NULL");
				sbiMode = "WEB";
			}
		}
		logger.debug((new StringBuilder("OUT: sbiMode = ")).append(sbiMode).toString());
		return sbiMode;
	}

	@Override
	public String getMessageTextFromResource(String resourceName, HttpServletRequest request) {
		logger.debug("IN");
		Locale locale = getLocale(request);
		String message = getMessageTextFromResource(resourceName, locale);
		logger.debug("OUT");
		return message;
	}

	/**
	 * Internationalization of user messages via DB
	 *
	 * @param locale
	 * @param code
	 * @return
	 */

	@Override
	public String getI18nMessage(Locale locale, String code) {
		logger.debug("IN");
		String toreturn = null;
		if (code == null)
			return null;
		if (locale != null) {
			if (code.startsWith("i18n_") || code.startsWith("I18N_")) {
				try {
					I18NMessagesDAO dao = DAOFactory.getI18NMessageDAO();
					toreturn = dao.getI18NMessages(locale, code);
				} catch (EMFUserError e) {
					logger.error("error during internalization of " + code + " in table I18NMessages; original code will be kept", e);
				}
			}
		}
		if (toreturn == null) {
			toreturn = code;
		}
		logger.debug("OUT");
		return toreturn;
	}

	/**
	 * Internationalization of user messages via DB
	 *
	 * @param code
	 * @param request
	 * @return
	 */

	@Override
	public String getI18nMessage(String code, HttpServletRequest request) {
		Locale locale = getLocale(request);
		return getI18nMessage(locale, code);
	}

	/**
	 *
	 * Previous user message, internazionalized with bundle
	 */

	// public String getUserMessage(String code, String bundle, HttpServletRequest request)
	// {
	// Locale locale = getLocale(request);
	// String toReturn = code;
	// if(code.length() > 4)
	// {
	// String prefix = code.substring(0, 4);
	// if(prefix.equalsIgnoreCase("cod_"))
	// {
	// String newCode = code.substring(4);
	// toReturn = getMessageInternal(newCode, bundle, locale);
	// }
	// }
	// return toReturn;
	// }

	// public String getUserMessage(String code, String bundle, Locale locale)
	// {
	// String toReturn = code;
	// if(code.length() > 4)
	// {
	// String prefix = code.substring(0, 4);
	// if(prefix.equalsIgnoreCase("cod_"))
	// {
	// String newCode = code.substring(4);
	// toReturn = getMessageInternal(newCode, bundle, locale);
	// }
	// }
	// return toReturn;
	// }

}
