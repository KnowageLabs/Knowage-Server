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

import static it.eng.spagobi.commons.constants.ConfigurationConstants.SPAGOBI_SPAGOBI_SERVICE_JNDI;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.dataset.utils.ParamDefaultValue;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.file.FileUtils;

/**
 * Contains some SpagoBI's general utilities.
 */
public class GeneralUtilities extends SpagoBIUtilities {

	private static final Logger LOGGER = LogManager.getLogger(GeneralUtilities.class);
	private static final String PREVIEW_FILE_STORAGE_DIRECTORY = "preview" + File.separatorChar + "images";
	private static final String BACKEND_EXTENSION = "BackEnd";
	private static final String VUE_ENVIRONMENT = "vue.environment";
	private static final int MAX_DEFAULT_FILE_5M_SIZE = 5242880;
	private static final int MAX_DEFAULT_FILE_10M_SIZE = 10485760; // 10 mega byte

	private static boolean isProduction = true;

	static {
		String vueEnvironment = System.getProperty(VUE_ENVIRONMENT);
		LOGGER.info("Retrieved {} system property. Vue environment is: [{}]", VUE_ENVIRONMENT, vueEnvironment);
		if (vueEnvironment != null && vueEnvironment.equalsIgnoreCase("development")) {
			LOGGER.info("Setting production mode to off. Development mode is now enabled.");
			isProduction = false;
		}
	}

	/**
	 * Substitutes the substrings with sintax "${code,bundle}" or "${code}" (in the second case bundle is assumed to be the default value "messages") with the
	 * correspondent internationalized messages in the input String. This method calls <code>PortletUtilities.getMessage(key, bundle)</code>.
	 *
	 * @param message The string to be modified
	 *
	 * @return The message with the internationalized substrings replaced.
	 */
	public static String replaceInternationalizedMessages(String message) {
		if (message == null) {
			return null;
		}
		int startIndex = message.indexOf("${");
		if (startIndex == -1) {
			return message;
		} else {
			return replaceInternationalizedMessages(message, startIndex);
		}
	}

	public static String trim(String s) {
		if (s != null) {
			if (s.trim().length() == 0) {
				return null;
			} else {
				return s.trim();
			}
		}
		return null;
	}

	private static String replaceInternationalizedMessages(String message, int startIndex) {
		LOGGER.trace("IN");
		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
		int endIndex = message.indexOf("}", startIndex);
		if (endIndex == -1 || endIndex < startIndex) {
			return message;
		}
		String toBeReplaced = message.substring(startIndex + 2, endIndex).trim();
		String key = "";
		String bundle = "messages";
		String[] splitted = toBeReplaced.split(",");
		if (splitted != null) {
			key = splitted[0].trim();
			if (splitted.length == 1) {
				String replacement = msgBuilder.getMessage(key, bundle);
				if (!replacement.equalsIgnoreCase(key))
					message = message.replaceAll("\\$\\{" + toBeReplaced + "\\}", replacement);
			}
			if (splitted.length == 2) {
				if (splitted[1] != null && !splitted[1].trim().equals(""))
					bundle = splitted[1].trim();
				String replacement = msgBuilder.getMessage(key, bundle);
				if (!replacement.equalsIgnoreCase(key))
					message = message.replaceAll("\\$\\{" + toBeReplaced + "\\}", replacement);
			}
		}
		startIndex = message.indexOf("${", endIndex);
		if (startIndex != -1) {
			message = replaceInternationalizedMessages(message, startIndex);
		}
		LOGGER.trace("OUT");
		return message;
	}

	/**
	 * Subsitute bi object parameters lov profile attributes.
	 *
	 * @param obj     the obj
	 * @param session the session
	 *
	 * @throws Exception        the exception
	 * @throws EMFInternalError the EMF internal error
	 */
	public static void subsituteBIObjectParametersLovProfileAttributes(BIObject obj, SessionContainer session)
			throws Exception {
		LOGGER.trace("IN");
		List<BIObjectParameter> biparams = obj.getDrivers();
		Iterator<BIObjectParameter> iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			// if the param is a Fixed Lov, Make the profile attribute
			// substitution at runtime
			BIObjectParameter biparam = iterParams.next();
			Parameter param = biparam.getParameter();
			ModalitiesValue modVal = param.getModalityValue();
			if (modVal.getITypeCd().equals(SpagoBIConstants.INPUT_TYPE_FIX_LOV_CODE)) {
				String value = modVal.getLovProvider();
				int profileAttributeStartIndex = value.indexOf("${");
				if (profileAttributeStartIndex != -1) {
					IEngUserProfile profile = (IEngUserProfile) session.getPermanentContainer()
							.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
					value = StringUtilities.substituteProfileAttributesInString(value, profile,
							profileAttributeStartIndex);
					biparam.getParameter().getModalityValue().setLovProvider(value);
				}
			}
		}
		LOGGER.trace("OUT");
	}

	/**
	 * Creates a new user profile, given his identifier.
	 *
	 * @param userId The user identifier
	 *
	 * @return The newly created user profile
	 *
	 * @throws Exception the exception
	 */
	public static IEngUserProfile createNewUserProfile(String userId) throws Exception {
		return UserUtilities.getUserProfile(userId);
	}

	/**
	 * Returns the complete HTTP URL and puts it into a string.
	 *
	 * @param userId the user id
	 *
	 * @return A String with complete HTTP Url
	 */
	public static String getSpagoBIProfileBaseUrl(String userId) {
		LOGGER.debug("Trying to recover Spago Adapter HTTP Url. userId = {}", userId);
		String url = "";
		String path = "";
		String adapUrlStr = "";
		try {
			adapUrlStr = getSpagoAdapterHttpUrl();
			path = KnowageSystemConfiguration.getKnowageContext();
			if (isSSOEnabled()) {
				url = path + adapUrlStr + "?NEW_SESSION=TRUE";
			} else {
				url = path + adapUrlStr + "?NEW_SESSION=TRUE&" + SsoServiceInterface.USER_ID + "=" + userId;
			}
		} catch (Exception e) {
			LOGGER.error("Error while recovering complete HTTP Url", e);
		}
		LOGGER.debug("Using URL: {}", url);
		return url;
	}

	/**
	 * Returns true if the SSO is enabled (SPAGOBI_SSO.ACTIVE in spagobi_SSO.xml equals true ignoring the case), false otherwise
	 *
	 * @return true if the SSO is enabled (SPAGOBI_SSO.ACTIVE in spagobi_SSO.xml equals true ignoring the case), false otherwise
	 */
	public static boolean isSSOEnabled() {
		boolean toReturn;
		SingletonConfig config = SingletonConfig.getInstance();
		String activeSso = config.getConfigValue("SPAGOBI_SSO.ACTIVE");
		LOGGER.debug("Active SSO: {}", activeSso);
		if (activeSso != null && activeSso.equalsIgnoreCase("true")) {
			toReturn = true;
		} else {
			toReturn = false;
		}
		LOGGER.debug("Returning: {}", toReturn);
		return toReturn;
	}

	/**
	 * Gets the Spago Adapter HTTP URL.
	 *
	 * @return the Spago Adapter HTTP URL
	 */
	public static String getSpagoAdapterHttpUrl() {
		LOGGER.debug("Getting Spago Adapter HTTP URL");
		String adapUrlStr = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGO_ADAPTERHTTP_URL");
		if (adapUrlStr != null) {
			adapUrlStr = adapUrlStr.trim();
		}
		LOGGER.debug("Returning: {}", adapUrlStr);
		return adapUrlStr;
	}

	/**
	 * Gets the default locale from SpagoBI configuration file, the behaviors is the same of getDefaultLocale() function, with difference that if not finds returns
	 * null
	 *
	 * TODO : merge its behaviour with GetDefaultLocale (not done know cause today is release date). Gets the default locale.
	 *
	 * @return the default locale
	 */
	public static Locale getStartingDefaultLocale() {
		LOGGER.trace("Getting starting default locale");
		Locale locale = null;
		SingletonConfig config = SingletonConfig.getInstance();
		String languageConfig = config.getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default");
		if (languageConfig != null) {
			locale = Locale.forLanguageTag(languageConfig);
		}
		LOGGER.trace("Locale is: {}", locale);
		return locale;
	}

	/**
	 * Gets the default locale.
	 *
	 * @return the default locale
	 */
	public static Locale getDefaultLocale() {
		LOGGER.trace("Getting default locale");
		Locale locale = null;
		try {
			String defaultLanguageTag = SingletonConfig.getInstance()
					.getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default");
			String languageTag = StringUtils.isNotBlank(defaultLanguageTag) ? defaultLanguageTag : "en-US";
			LOGGER.trace("Default locale found: {}", languageTag);
			locale = Locale.forLanguageTag(languageTag);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Error while getting default locale", t);
		}
		LOGGER.debug("Default locale is: {}", locale);
		return locale;
	}

	public static List<Locale> getSupportedLocales() {
		LOGGER.trace("Getting supported locales");
		List<Locale> ret = new ArrayList<>();
		String supportedLanguages = SingletonConfig.getInstance()
				.getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGES");
		if (StringUtils.isNotBlank(supportedLanguages)) {
			for (String supportedLanguageTag : supportedLanguages.split(",", -1)) {
				Locale locale = Locale.forLanguageTag(supportedLanguageTag);
				LOGGER.trace("Found locale with language = [{}] and script = [{}] and country = [{}]",
						locale.getLanguage(), locale.getScript(), locale.getCountry());
				ret.add(locale);
			}

		} else {
			LOGGER.error("NO LOCALES CONFIGURED!!!");
		}
		LOGGER.trace("Returning supported locales: {}", ret);
		return ret;
	}

	public static String getCountry(String language) {
		LOGGER.trace("Getting country for language: {}", language);
		String country = null;
		List<Locale> locales = GeneralUtilities.getSupportedLocales();
		Iterator<Locale> iter = locales.iterator();
		while (iter.hasNext()) {
			Locale localeTmp = iter.next();
			String languageTmp = localeTmp.getLanguage();
			country = localeTmp.getCountry();
			if (languageTmp.equals(language)) {
				break;
			}
		}
		LOGGER.trace("Country is: {}", country);
		return country;
	}

	public static String getScript(String language) {
		LOGGER.trace("Getting script for language: {}", language);
		String script = null;
		List<Locale> locales = GeneralUtilities.getSupportedLocales();
		Iterator<Locale> iter = locales.iterator();
		while (iter.hasNext()) {
			Locale localeTmp = iter.next();
			String languageTmp = localeTmp.getLanguage();
			if (languageTmp.equals(language)) {
				script = localeTmp.getScript();
				break;
			}
		}
		LOGGER.trace("Script is: {}", script);
		return script;
	}

	public static JSONArray getSupportedLocalesAsJSONArray() {
		LOGGER.trace("Getting supported locales as JSONArray");
		JSONArray ret = new JSONArray();
		try {
			List<Locale> locales = getSupportedLocales();
			Iterator<Locale> it = locales.iterator();
			while (it.hasNext()) {
				Locale locale = it.next();
				JSONObject localeJSON = new JSONObject();
				localeJSON.put("language", locale.getLanguage());
				localeJSON.put("country", locale.getCountry());
				ret.put(localeJSON);
			}
		} catch (Exception e) {
			LOGGER.error("Error while retrieving supported locales as JSONArray", e);
		}
		LOGGER.trace("Supported locales as JSONArray: {}", ret);
		return ret;
	}

	public static Locale getCurrentLocale(RequestContainer requestContainer) {
		LOGGER.trace("Getting current locale from request");
		Locale ret = null;
		if (requestContainer != null) {
			SessionContainer permSession = requestContainer.getSessionContainer().getPermanentContainer();
			if (permSession != null) {
				String languageTag = (String) permSession.getAttribute(SpagoBIConstants.AF_LANGUAGE_TAG);
				if (StringUtils.isNotBlank(languageTag)) {
					ret = Locale.forLanguageTag(languageTag);
				} else {
					String language = (String) permSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
					String country = (String) permSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
					String script = (String) permSession.getAttribute(SpagoBIConstants.AF_SCRIPT);

					ret = new Builder().setLanguage(language).setRegion(country).setScript(script).build();
				}
			}
		}
		if (ret == null) {
			ret = getDefaultLocale();
		}
		LOGGER.trace("Current locale from request: {}", ret);
		return ret;
	}

	public static String getLocaleDateFormat(SessionContainer permSess) {
		LOGGER.debug("Getting locale date format from session");
		String languageTag = (String) permSess.getAttribute(SpagoBIConstants.AF_LANGUAGE_TAG);
		// if a particular language is specified take the corrisponding date-format
		String ret = null;

		if (StringUtils.isBlank(languageTag)) {

			String language = (String) permSess.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String country = (String) permSess.getAttribute(SpagoBIConstants.AF_COUNTRY);
			String script = (String) permSess.getAttribute(SpagoBIConstants.AF_SCRIPT);

			Locale locale = new Builder().setLanguage(language).setRegion(country).setScript(script).build();
			languageTag = locale.toLanguageTag();
		}
		if (languageTag != null) {
			ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-" + languageTag + ".format");
		}
		if (ret == null) {
			ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT.format");
		}
		LOGGER.debug("Locale date format from session: {}", ret);
		return ret;

	}

	public static String getScriptFromLocale(SessionContainer permSess) {
		String toReturn = "";
		String script = (String) permSess.getAttribute(SpagoBIConstants.AF_SCRIPT);

		if (StringUtils.isNotBlank(script)) {
			toReturn = script + "-";
		}

		return toReturn;

	}

	public static String getLocaleDateFormat(Locale locale) {
		LOGGER.debug("Getting date format from locale");
		String ret = null;
		// if a particular language is specified take the corrisponding date-format
		if (locale != null) {
			ret = SingletonConfig.getInstance()
					.getConfigValue("SPAGOBI.DATE-FORMAT-" + locale.toLanguageTag() + ".format");
		}
		if (ret == null) {
			ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT.format");
		}
		LOGGER.debug("Date format from locale: {}", ret);
		return ret;

	}

	public static String getLocaleDateFormatForExtJs(SessionContainer permSess) {
		LOGGER.debug("Getting date format from locale for ExtJS");
		String languageTag = (String) permSess.getAttribute(SpagoBIConstants.AF_LANGUAGE_TAG);
		String ret = null;

		if (StringUtils.isBlank(languageTag)) {

			String language = (String) permSess.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String country = (String) permSess.getAttribute(SpagoBIConstants.AF_COUNTRY);
			String script = (String) permSess.getAttribute(SpagoBIConstants.AF_SCRIPT);

			Locale locale = new Builder().setLanguage(language).setRegion(country).setScript(script).build();
			languageTag = locale.toLanguageTag();
		}
		// if a particular language is specified take the corrisponding date-format
		if (languageTag != null) {
			ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-" + languageTag + ".format");
		}
		if (ret == null) {
			ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT.extJsFormat");
		}
		if (ret == null) {
			LOGGER.warn("Locale date format for ExtJs not found, using d/m/Y as default");
			ret = "d/m/Y";
		}
		LOGGER.debug("Date format from locale for ExtJS: {}", ret);
		return ret;

	}

	public static String getServerDateFormat() {
		LOGGER.debug("Getting server date format");
		String ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format");
		LOGGER.debug("Server date format is: {}", ret);
		return ret;
	}

	public static String getServerTimeStampFormat() {
		LOGGER.debug("Getting server timestamp format");
		String ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.TIMESTAMP-FORMAT.format");
		LOGGER.debug("Server timestamp format is: {}", ret);
		return ret;
	}

	public static String getServerDateFormatExtJs() {
		LOGGER.debug("Getting server date format for ExtJS");
		String ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.extJsFormat");
		LOGGER.debug("Server date format for ExtJS is: {}", ret);
		return ret;
	}

	public static String getServerTimestampFormatExtJs() {
		LOGGER.debug("Getting server timestamp format for ExtJS");
		String ret = SingletonConfig.getInstance().getConfigValue("SPAGOBI.TIMESTAMP-FORMAT.extJsFormat");
		LOGGER.debug("Server timestamp format for ExtJS is: {}", ret);
		return ret;
	}

	public static char getDecimalSeparator(Locale locale) {
		LOGGER.debug("Getting decimal separator for locale: {}", locale);
		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(locale);
		DecimalFormatSymbols decimalFormatSymbols = df.getDecimalFormatSymbols();
		char ret = decimalFormatSymbols.getDecimalSeparator();

		LOGGER.debug("Decimal separator is: {}", ret);
		return ret;
	}

	public static char getGroupingSeparator(Locale locale) {
		LOGGER.debug("Getting grouping separator for locale: {}", locale);
		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(locale);
		DecimalFormatSymbols decimalFormatSymbols = df.getDecimalFormatSymbols();
		char ret = decimalFormatSymbols.getGroupingSeparator();

		LOGGER.debug("Grouping separator for locale is: {}", ret);
		return ret;
	}

	public static int getTemplateMaxSize() {
		LOGGER.debug("Getting template max size");
		int ret = MAX_DEFAULT_FILE_5M_SIZE;
		try {
			SingletonConfig serverConfig = SingletonConfig.getInstance();
			String maxSizeStr = serverConfig.getConfigValue("SPAGOBI.TEMPLATE_MAX_SIZE");
			if (maxSizeStr != null) {
				LOGGER.debug("Configuration found for max template size: {}", maxSizeStr);
				ret = Integer.parseInt(maxSizeStr);
			} else {
				LOGGER.debug("No configuration found for max template size");
			}
		} catch (Exception e) {
			LOGGER.error("Error while retrieving max template size", e);
			LOGGER.debug("Considering default value {}", MAX_DEFAULT_FILE_5M_SIZE);
			ret = MAX_DEFAULT_FILE_5M_SIZE;
		}
		LOGGER.debug("Template max size is: {}", ret);
		return ret;
	}

	public static int getDataSetFileMaxSize() {
		LOGGER.debug("Getting dataset file max size");
		int toReturn = MAX_DEFAULT_FILE_10M_SIZE;
		try {
			SingletonConfig serverConfig = SingletonConfig.getInstance();
			String maxSizeStr = serverConfig.getConfigValue("SPAGOBI.DATASET_FILE_MAX_SIZE");
			if (maxSizeStr != null) {
				LOGGER.debug("Configuration found for max dataset file size: {}", maxSizeStr);
				toReturn = Integer.parseInt(maxSizeStr);
			} else {
				LOGGER.debug("No configuration found for max dataset file size");
			}
		} catch (Exception e) {
			LOGGER.error("Error while retrieving max dataset file size", e);
			LOGGER.debug("Considering default value {}", MAX_DEFAULT_FILE_10M_SIZE);
			toReturn = MAX_DEFAULT_FILE_10M_SIZE;
		}
		LOGGER.debug("Dataset file max size is: {}", toReturn);
		return toReturn;
	}

	public static int getGisLayerFileMaxSize() {
		LOGGER.debug("Getting GIS layer file max size");
		int ret = (2 * MAX_DEFAULT_FILE_10M_SIZE);
		try {
			SingletonConfig serverConfig = SingletonConfig.getInstance();
			String maxSizeStr = serverConfig.getConfigValue("GIS_LAYER_FILE_MAX_SIZE");
			if (maxSizeStr != null) {
				LOGGER.debug("Configuration found for max layer gis file size: {}", maxSizeStr);
				ret = Integer.parseInt(maxSizeStr);
			} else {
				LOGGER.debug("No configuration found for max layer gis file size");
			}
		} catch (Exception e) {
			LOGGER.error("Error while retrieving max dataset file size", e);
			LOGGER.debug("Considering default value " + (2 * MAX_DEFAULT_FILE_10M_SIZE));
			ret = (2 * MAX_DEFAULT_FILE_10M_SIZE); // 20M
		}
		LOGGER.debug("GIS layer file max size is: {}", ret);
		return ret;
	}

	public static String getSessionExpiredURL() {
		LOGGER.debug("Getting session expired URL");
		String sessionExpiredUrl = null;
		try {
			LOGGER.debug("Trying to recover SpagoBI session expired url from ConfigSingleton");
			SingletonConfig spagoConfig = SingletonConfig.getInstance();
			sessionExpiredUrl = spagoConfig.getConfigValue("SPAGOBI.SESSION_EXPIRED_URL");
		} catch (Exception e) {
			LOGGER.error("Error while recovering SpagoBI session expired url", e);
		}
		LOGGER.debug("Session expired URL: {}", sessionExpiredUrl);
		return sessionExpiredUrl;
	}

	/**
	 * Returns an url starting with the given base url and adding parameters retrieved by the input parameters map. Each parameter value is encoded using
	 * URLEncoder.encode(value, StandardCharsets.UTF_8);
	 *
	 * @param baseUrl The base url
	 * @param mapPars The parameters map; those parameters will be added to the base url (values will be encoded using UTF-8 encoding)
	 * @return an url starting with the given base url and adding parameters retrieved by the input parameters map
	 */
	public static String getUrl(String baseUrl, Map mapPars) {
		LOGGER.debug("Getting URL (???) from {} base URL and parameters {}", baseUrl, mapPars);
		Assert.assertNotNull(baseUrl, "Base url in input is null");
		StringBuilder sb = new StringBuilder();
		sb.append(baseUrl);
		sb.append(baseUrl.indexOf("?") == -1 ? "?" : "&");
		if (mapPars != null && !mapPars.isEmpty()) {
			Set keys = mapPars.keySet();
			Iterator iterKeys = keys.iterator();
			while (iterKeys.hasNext()) {
				String key = iterKeys.next().toString();
				Object valueObj = mapPars.get(key);
				if (valueObj != null) {
					String value = valueObj.toString();
					// encoding value
					try {
						value = URLEncoder.encode(value, UTF_8.name());

						// put all + to space! that is because
						// otherwise %2B (encoding of plus) and + (substitution of white space in an url)
						// will otherwise be interpreted in the same way
						// and when using exporter I would no more be able to distinguish + from ' '
						// value = value.replaceAll(Pattern.quote("+") , " ");

					} catch (UnsupportedEncodingException e) {
						LOGGER.warn("UTF-8 encoding is not supported!!!", e);
						LOGGER.warn("Using system encoding...");
						value = URLEncoder.encode(value);
					}

					sb.append(key + "=" + value);
					if (iterKeys.hasNext()) {
						sb.append("&");
					}
				}
			}
		}
		LOGGER.debug("URL is: {}", sb);
		return sb.toString();
	}

	/**
	 * getParametersFromURL: takes an url and return a Map containing URL parameters
	 *
	 * @param urlString
	 * @return map containing url parameters
	 */

	public static Map getParametersFromURL(String urlString) {
		LOGGER.debug("Getting parameters from URL: {}", urlString);
		Map<String, Object> ret = new HashMap<>();
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			LOGGER.error("Malformed URL Exception {}", urlString, e);
			return null;
		}
		// get parameters string
		String parameters = url.getQuery();
		StringTokenizer st = new StringTokenizer(parameters, "&", false);

		String parameterToken = null;
		String parameterName = null;
		String parameterValue = null;
		while (st.hasMoreTokens()) {
			parameterToken = st.nextToken();
			parameterName = parameterToken.substring(0, parameterToken.indexOf("="));
			String parameterValueEncoded = parameterToken.substring(parameterToken.indexOf("=") + 1);

			// do the decode
			try {
				parameterValue = URLDecoder.decode(parameterValueEncoded, UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("Error in decoding parameter: UTF 8 not supported {}; use previous value {}",
						parameterName, parameterValueEncoded, e);
				parameterValue = parameterValueEncoded;
			} catch (java.lang.IllegalArgumentException e) { // can happen when in document composition a '%' char is given
				LOGGER.warn(
						"Error in decoding parameter, illegal argument for (probably value % is present); use preceding value {}",
						parameterName, parameterValueEncoded);
				parameterValue = parameterValueEncoded;
			} catch (Exception e) {
				LOGGER.warn("Generic Error in decoding parameter {} ; use previous value {}", parameterName,
						parameterValueEncoded);
				parameterValue = parameterValueEncoded;
			}

			// if is already present create a list
			if (ret.keySet().contains(parameterName)) {
				Object prevValue = ret.get(parameterName).toString();
				List<String> toInsert = null;
				// if was alrady a list
				if (prevValue instanceof List) {
					toInsert = (List<String>) prevValue;
					toInsert.add(parameterValue);
				} else { // else create a new list and add both elements
					toInsert = new ArrayList<>();
					toInsert.add(prevValue.toString());
					toInsert.add(parameterValue);
				}
				// put list
				ret.put(parameterName, toInsert);
			} else { // case single value
				ret.put(parameterName, parameterValue);
			}
		}
		LOGGER.debug("Map is: {}", ret);
		return ret;
	}

	public static int getDatasetMaxResults() {
		int maxResults = Integer.MAX_VALUE;
		String maxResultsStr = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATASET.maxResult");
		if (maxResultsStr != null) {
			maxResults = Integer.parseInt(maxResultsStr);
		} else {
			LOGGER.warn(
					"Dataset max results configuration not found. Check spagobi.xml, SPAGOBI.DATASET.maxResults attribute");
			LOGGER.debug("Using default value that is Integer.MAX_VALUE = {}", Integer.MAX_VALUE);
		}
		return maxResults;
	}

	public static File getPreviewFilesStorageDirectoryPath() {
		String resourcePath = SpagoBIUtilities.getResourcePath();
		if (resourcePath.endsWith("/") || resourcePath.endsWith("\\")) {
			resourcePath += PREVIEW_FILE_STORAGE_DIRECTORY;
		} else {
			resourcePath += File.separatorChar + PREVIEW_FILE_STORAGE_DIRECTORY;
		}
		return FileUtils.checkAndCreateDir(resourcePath);
	}

	/**
	 * Return the default values from dataSet. If they are not present then retrieve them from db.
	 *
	 * @param dataSet
	 * @return null if some errors occur (no exceptions thrown)
	 */
	public static Map<String, ParamDefaultValue> getParamsDefaultValuesUseAlsoDB(IDataSet dataSet) {
		Map<String, ParamDefaultValue> res = DataSetUtilities.getParamsDefaultValues(dataSet);
		if (res != null) {
			return res;
		}
		LOGGER.warn("No params default values found on dataSet. I try from db.");

		// res=null, load dataset from persistence
		try {
			String label = dataSet.getLabel();
			if (label == null) {
				LOGGER.warn("Label not found -> no default values from database");
				return null;
			}

			IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
			IDataSet ds = dsDAO.loadDataSetByLabel(label);
			if (ds == null) {
				LOGGER.warn("Dataset not found -> no default values from database");
				return null;
			}

			res = DataSetUtilities.getParamsDefaultValues(ds);
		} catch (Exception e) {
			LOGGER.warn("Default parameters values can't be retrieved from dataSet db.", e);
		}

		return res;
	}

	/**
	 * Return the value associated with the provided property name
	 *
	 * @param propertyName
	 * @return the value of the property if present, null elsewhere
	 */
	public static String getSpagoBIConfigurationProperty(String propertyName) {
		try {
			String propertyValue = null;
			IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
			Config config = configDao.loadConfigParametersByLabel(propertyName);
			if ((config != null) && (config.isActive())) {
				propertyValue = config.getValueCheck();
			}
			return propertyValue;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException(
					"An unexpected exception occured while loading spagobi property [" + propertyName + "]", t);
		}
	}

	public static URIBuilder getBE2BEEngineUrl(Engine eng) {
		LOGGER.debug("Generating BE2BE for engine: {}", eng);
		String urlEngine = getExternalEngineContextPath(eng);
		LOGGER.debug("Engine url is {}", urlEngine);
		if (!"it.eng.spagobi.engines.drivers.dashboard.DashboardDriver".equals(eng.getDriverName())) {
			Assert.assertTrue(urlEngine != null && !urlEngine.trim().equals(""),
					"External engine url is not defined!!");
		}
		if ("it.eng.spagobi.engines.drivers.dashboard.DashboardDriver".equals(eng.getDriverName())) {
			urlEngine = resolveRelativeUrlsForVue(urlEngine);
		} else {
			urlEngine = resolveRelativeUrls(urlEngine);
		}

		if (EngineUtilities.hasBackEndService(eng)) {
			// ADD this extension because this is a BackEnd engine invocation
			urlEngine = urlEngine + BACKEND_EXTENSION;
		}
		URIBuilder ret = null;
		try {
			ret = new URIBuilder(urlEngine);
		} catch (URISyntaxException e) {
			throw new SpagoBIRuntimeException("The URL " + urlEngine + " is not valid", e);
		}
		LOGGER.debug("BE2BE for engine is: {}", ret);
		return ret;
	}

	private static String resolveRelativeUrlsForVue(String url) {
		LOGGER.debug("IN: url = " + url);
		if (url.startsWith("/")) {
			LOGGER.debug("Url is relative");
			String domain = getServiceHostUrl();
			if (!isProduction)
				domain = domain.replaceAll("8080", "3000"); // for testing purposes
			LOGGER.debug("SpagoBI domain is " + domain);
			url = domain + url;
			LOGGER.debug("Absolute url is " + url);
		}
		LOGGER.debug("OUT: returning " + url);
		return url;
	}

	private static String resolveRelativeUrls(String url) {
		LOGGER.debug("IN: url = " + url);
		if (url.startsWith("/")) {
			LOGGER.debug("Url is relative");
			String domain = getServiceHostUrl();
			LOGGER.debug("SpagoBI domain is " + domain);
			url = domain + url;
			LOGGER.debug("Absolute url is " + url);
		}
		LOGGER.debug("OUT: returning " + url);
		return url;
	}

	public static String getServiceHostUrl() {
		String serviceURL = SpagoBIUtilities
				.readJndiResource(SingletonConfig.getInstance().getConfigValue(SPAGOBI_SPAGOBI_SERVICE_JNDI));
		serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf('/'));

		return serviceURL;
	}

	public static String getAngularPropertiesFileName(Locale locale, String separator) {
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String script = locale.getScript();

		StringBuilder sb = new StringBuilder();
		sb.append(language);
		sb.append(separator);
		if (StringUtils.isNotBlank(script)) {
			sb.append(script);
			sb.append(separator);
		}
		sb.append(country);

		return sb.toString();
	}

	public static String getAngularPropertiesFileName(String currLanguage, String currScript, String currCountry,
			String separator) {
		Locale locale = new Builder().setLanguage(currLanguage).setRegion(currCountry).setScript(currScript).build();
		return "/js/lib/angular-localization/" + getAngularPropertiesFileName(locale, separator) + ".js";
	}

	public static String getExternalEngineContextPath(Engine engine) {
		// in case there is a Secondary URL, use it
		String urlEngine = engine.getSecondaryUrl();
		if (StringUtils.isEmpty(urlEngine)) {
			LOGGER.debug("Secondary url is not defined for engine " + engine.getLabel() + "; main url will be used.");
			// in case there is not a Secondary URL, use the main url
			if (!"it.eng.spagobi.engines.drivers.dashboard.DashboardDriver".equals(engine.getDriverName())) {
				urlEngine = engine.getUrl();
			} else {
				urlEngine = "/";
			}
		}
		return urlEngine;
	}
}