/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 7-lug-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.utilities;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.file.FileUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Contains some SpagoBI's general utilities.
 */
public class GeneralUtilities extends SpagoBIUtilities{

	private static transient Logger logger = Logger.getLogger(GeneralUtilities.class);

	private static final String PREVIEW_FILE_STORAGE_DIRECTORY = "preview" + File.separatorChar + "images";
	
	public static final int MAX_DEFAULT_TEMPLATE_SIZE = 5242880;
	public static final int MAX_DEFAULT_FILE_DATASET_SIZE = 10485760; // 10 mega byte
	private static String SPAGOBI_HOST = null; 
//	private static String SPAGOBI_DOMAIN = null;
	
	/**
	 * Substitutes the substrings with sintax "${code,bundle}" or "${code}" (in
	 * the second case bundle is assumed to be the default value "messages")
	 * with the correspondent internationalized messages in the input String.
	 * This method calls <code>PortletUtilities.getMessage(key, bundle)</code>.
	 * 
	 * @param message The string to be modified
	 * 
	 * @return The message with the internationalized substrings replaced.
	 */
	public static String replaceInternationalizedMessages(String message) {
		if (message == null)
			return null;
		int startIndex = message.indexOf("${");
		if (startIndex == -1)
			return message;
		else
			return replaceInternationalizedMessages(message, startIndex);
	}

	public static String trim( String s ){
	  if( s != null )
		if( s.trim().length() == 0 ) 
		  return null;
		else
		  return s.trim();
	  return null;
		
	}
	private static String replaceInternationalizedMessages(String message, int startIndex) {
		logger.trace("IN");
		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
		int endIndex = message.indexOf("}", startIndex);
		if (endIndex == -1 || endIndex < startIndex)
			return message;
		String toBeReplaced = message.substring(startIndex + 2, endIndex).trim();
		String key = "";
		String bundle = "messages";
		String[] splitted = toBeReplaced.split(",");
		if (splitted != null) {
			key = splitted[0].trim();
			if (splitted.length == 1) {
				String replacement = msgBuilder.getMessage(key, bundle);
				// if (!replacement.equalsIgnoreCase(key)) message =
				// message.replaceAll("${" + toBeReplaced + "}", replacement);
				if (!replacement.equalsIgnoreCase(key))
					message = message.replaceAll("\\$\\{" + toBeReplaced + "\\}", replacement);
			}
			if (splitted.length == 2) {
				if (splitted[1] != null && !splitted[1].trim().equals(""))
					bundle = splitted[1].trim();
				String replacement = msgBuilder.getMessage(key, bundle);
				// if (!replacement.equalsIgnoreCase(key)) message =
				// message.replaceAll("${" + toBeReplaced + "}", replacement);
				if (!replacement.equalsIgnoreCase(key))
					message = message.replaceAll("\\$\\{" + toBeReplaced + "\\}", replacement);
			}
		}
		startIndex = message.indexOf("${", endIndex);
		if (startIndex != -1)
			message = replaceInternationalizedMessages(message, startIndex);
		logger.trace("OUT");
		return message;
	}


	/**
	 * Subsitute bi object parameters lov profile attributes.
	 * 
	 * @param obj the obj
	 * @param session the session
	 * 
	 * @throws Exception the exception
	 * @throws EMFInternalError the EMF internal error
	 */
	public static void subsituteBIObjectParametersLovProfileAttributes(BIObject obj, SessionContainer session)
	throws Exception, EMFInternalError {
		logger.trace("IN");
		List biparams = obj.getBiObjectParameters();
		Iterator iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			// if the param is a Fixed Lov, Make the profile attribute
			// substitution at runtime
			BIObjectParameter biparam = (BIObjectParameter) iterParams.next();
			Parameter param = biparam.getParameter();
			ModalitiesValue modVal = param.getModalityValue();
			if (modVal.getITypeCd().equals(SpagoBIConstants.INPUT_TYPE_FIX_LOV_CODE)) {
				String value = modVal.getLovProvider();
				int profileAttributeStartIndex = value.indexOf("${");
				if (profileAttributeStartIndex != -1) {
					IEngUserProfile profile = (IEngUserProfile) session.getPermanentContainer().getAttribute(
							IEngUserProfile.ENG_USER_PROFILE);
					value = StringUtilities.substituteProfileAttributesInString(value, profile,
							profileAttributeStartIndex);
					biparam.getParameter().getModalityValue().setLovProvider(value);
				}
			}
		}
		logger.trace("OUT");
	}


	/**
	 * Gets the lov map result.
	 * 
	 * @param lovs the lovs
	 * 
	 * @return the lov map result
	 */
	/*
	public static String getLovMapResult(Map lovs) {
		logger.debug("IN");
		String toReturn = "<DATA>";
		Set keys = lovs.keySet();
		Iterator keyIter = keys.iterator();
		while (keyIter.hasNext()) {
			String key = (String) keyIter.next();
			String lovname = (String) lovs.get(key);
			String lovResult = "";
			try {
				lovResult = getLovResult(lovname);
			} catch (Exception e) {
				logger.error("Error while getting result of the lov " + lovname
						+ ", the result of the won't be inserted into the response", e);
				continue;
			}
			toReturn = toReturn + "<" + key + ">";
			toReturn = toReturn + lovResult;
			toReturn = toReturn + "</" + key + ">";
		}
		toReturn = toReturn + "</DATA>";
		logger.debug("OUT:" + toReturn);
		return toReturn;
	}
	 */

	/**
	 * Gets the lov result.
	 * 
	 * @param lovLabel the lov label
	 * 
	 * @return the lov result
	 * 
	 * @throws Exception the exception
	 */
	/*
	public static String getLovResult(String lovLabel) throws Exception {
		logger.debug("IN");
		IModalitiesValueDAO lovDAO = DAOFactory.getModalitiesValueDAO();
		ModalitiesValue lov = lovDAO.loadModalitiesValueByLabel(lovLabel);
		String toReturn = getLovResult(lov, null);
		logger.debug("OUT:" + toReturn);
		return toReturn;
	}
	 */

	/**
	 * Gets the lov result.
	 * 
	 * @param lovLabel the lov label
	 * @param profile the profile
	 * 
	 * @return the lov result
	 * 
	 * @throws Exception the exception
	 */
	/*
	public static String getLovResult(String lovLabel, IEngUserProfile profile) throws Exception {
		logger.debug("IN");
		IModalitiesValueDAO lovDAO = DAOFactory.getModalitiesValueDAO();
		ModalitiesValue lov = lovDAO.loadModalitiesValueByLabel(lovLabel);
		String toReturn = getLovResult(lov, profile);
		logger.debug("OUT" + toReturn);
		return toReturn;
	}
	 */

	/*
	private static String getLovResult(ModalitiesValue lov, IEngUserProfile profile) throws Exception {
		logger.debug("IN");
		if (profile == null) {
			profile = new UserProfile("anonymous");
		}
		String dataProv = lov.getLovProvider();
		ILovDetail lovDetail = LovDetailFactory.getLovFromXML(dataProv);
		String lovResult = lovDetail.getLovResult(profile, null, null);
		logger.debug("OUT:" + lovResult);
		return lovResult;
	}
	 */



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
	 * Returns the complete HTTP URL and puts it into a
	 * string.
	 * 
	 * @param userId the user id
	 * 
	 * @return A String with complete HTTP Url
	 */ 
	public static String getSpagoBIProfileBaseUrl(String userId) {
		logger.debug("IN.Trying to recover spago Adapter HTTP Url. userId="+userId);
		String url = "";
		String path = "";
		String adapUrlStr = "";
		try {
			adapUrlStr = getSpagoAdapterHttpUrl();
			path= getSpagoBiHost()+getSpagoBiContext();
			if (isSSOEnabled()) {
				url = path + adapUrlStr + "?NEW_SESSION=TRUE";
			} else {
				url = path + adapUrlStr + "?NEW_SESSION=TRUE&"+SsoServiceInterface.USER_ID+"="+userId;	
			}

			logger.debug("using URL: " + url);
		} catch (Exception e) {
			logger.error("Error while recovering complete HTTP Url", e);
		}
		logger.debug("OUT");
		return url;
	}   


	/**
	 * Returns true if the SSO is enabled (SPAGOBI_SSO.ACTIVE in spagobi_SSO.xml equals true ignoring the case), false otherwise
	 * @return true if the SSO is enabled (SPAGOBI_SSO.ACTIVE in spagobi_SSO.xml equals true ignoring the case), false otherwise
	 */
	public static boolean isSSOEnabled() {
		boolean toReturn;
		SingletonConfig config = SingletonConfig.getInstance();
		String active = config.getConfigValue("SPAGOBI_SSO.ACTIVE");
		logger.debug("active SSO: " + active);
		if (active != null && active.equalsIgnoreCase("true") ){
			toReturn = true;
		} else {
			toReturn = false;	
		}
		logger.debug("returning " + toReturn);
		return toReturn;
	}

	/**
	 * Gets the spagoBI's dashboards servlet information as a string.
	 * 
	 * @return A string containing spagoBI's dashboards servlet information
	 */
	public static String getSpagoBiDashboardServlet() {
		return getSpagoBiHost()+getSpagoBiContext() + "/DashboardService";
	}



	public static String getSpagoBiHost() {
		logger.debug("IN");
		if (SPAGOBI_HOST == null) {
			String tmp = null;
			try {
				logger.debug("Trying to recover SpagoBiHost from ConfigSingleton");
				SingletonConfig spagoConfig = SingletonConfig.getInstance();
				String sbTmp = spagoConfig.getConfigValue("SPAGOBI.SPAGOBI_HOST_JNDI");
				if (sbTmp != null) {
					tmp = readJndiResource(sbTmp);
				}
				if (tmp == null) {
					logger.debug("SPAGOBI_HOST not set, using the default value ");
					tmp = "http://localhost:8080";
				}
			} catch (Exception e) {
				logger.error("Error while recovering SpagoBI host url", e);
				throw new SpagoBIRuntimeException("Error while recovering SpagoBI host url", e);
			}
			try {
				new URL(tmp);
			} catch (MalformedURLException e) {
				SpagoBIRuntimeException sre = new SpagoBIRuntimeException("SpagoBI host URL is malformed!!", e);
				sre.addHint("Check configuration for spagobi_host_url environment variable");
				throw sre;
			}
			SPAGOBI_HOST = tmp;
		}
		logger.debug("OUT:" + SPAGOBI_HOST);
		return SPAGOBI_HOST;
	}

	/*
	public static String getSpagoBiDomain() {
		logger.debug("IN");
		if (SPAGOBI_DOMAIN == null) {
			try {
				logger.debug("Trying to recover SpagoBI domain from ConfigSingleton");
				ConfigSingleton spagoConfig = ConfigSingleton.getInstance();
				SourceBean sbTmp = (SourceBean) spagoConfig.getAttribute("SPAGOBI.SPAGOBI_DOMAIN_JNDI_NAME");
				if (sbTmp != null) {
					String jndi = sbTmp.getCharacters();
					SPAGOBI_DOMAIN = readJndiResource(jndi);
				}
				if (SPAGOBI_DOMAIN == null) {
					logger.debug("SPAGOBI_DOMAIN not set, using the default value ");
					SPAGOBI_DOMAIN = "http://localhost:8080";
				}
			} catch (Exception e) {
				logger.error("Error while recovering getSpagoBiHost", e);
			}
		}
		logger.debug("OUT:" + SPAGOBI_DOMAIN);
		return SPAGOBI_DOMAIN;
	}
	 */



	/**
	 * Gets the spago adapter http url.
	 * 
	 * @return the spago adapter http url
	 */
	public static String getSpagoAdapterHttpUrl() {
		logger.debug("IN");
		String adapUrlStr = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGO_ADAPTERHTTP_URL");
		if (adapUrlStr!=null) adapUrlStr = adapUrlStr.trim();
		logger.debug("OUT:" + adapUrlStr);
		return adapUrlStr;
	}



	/** Gets the default locale from SpagoBI configuraiton file,
	 *  the behaviours is the same of getDefaultLocale() function, with difference that if not finds returns null
	 *  
	 *  TODO : merge its behaviour with GetDefaultLocale (not done know cause today is release date).
	 * Gets the default locale.
	 * 
	 * @return the default locale
	 */
	public static Locale getStartingDefaultLocale() {
		logger.trace("IN");
		String country = null;
		String language = null;
		Locale locale = null;
		SingletonConfig config = SingletonConfig.getInstance();
		String languageConfig = config.getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default");
		if (languageConfig != null ) {
			language = languageConfig.substring(0, 2);
			country = languageConfig.substring(3);
			if ((country == null) || country.trim().equals("") || (language == null) || language.trim().equals("")) {
				logger.warn("Problem reading locale");
			}
			else{
				// set the locale!
				locale = new Locale(language, country);				
				logger.trace("locale set to "+locale);
			}
		} 

		logger.trace("OUT");
		return locale;
	}


	/**
	 * Gets the default locale.
	 * 
	 * @return the default locale
	 */
	public static Locale getDefaultLocale() {
		logger.trace("IN");
		Locale locale = null;
		String languageConfig = null;
		try {
			String country = null;
			String language = null;
			languageConfig = SingletonConfig.getInstance().getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default");
			logger.trace("Default locale found: " + languageConfig);
			if (languageConfig != null && !languageConfig.trim().equals("")) {
				language = languageConfig.substring(0, 2);
				country = languageConfig.substring(3);
				if ((country == null) || country.trim().equals("") || (language == null) || language.trim().equals("")) {
					country = "US";
					language = "en";
				}
			} else {
				country = "US";
				language = "en";
			}
			locale = new Locale(language, country);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Error while getting default locale", t);
		}
		logger.debug("OUT:" + locale.toString());
		return locale;
	}

	public static List<Locale> getSupportedLocales() {
		logger.trace("IN");
		List<Locale> toReturn = new ArrayList<Locale>();
		String locales = SingletonConfig.getInstance().getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGES");
		if (locales != null && locales.length() > 0) {
			//Iterator it = locales.iterator();
			while (locales.length() > 1) {
				String temp = locales;
				String language = locales.substring(1,3);
				String country = locales.substring(4,6);
				if(locales.length() > 8){
					locales = temp.substring(8,locales.length());
				}
				else{
					locales = "0";
				}
				logger.trace("Found locale with language = [" + language + "] and country = [" + country + "]");
				Locale locale = new Locale(language, country);
				toReturn.add(locale);
				
			}
		} else {
			logger.error("NO LOCALES CONFIGURED!!!");
		}
		logger.trace("OUT");
		return toReturn;
	}
	
	public static String getCountry(String language) {
		logger.trace("IN");
		String country=null;
    	List locales=GeneralUtilities.getSupportedLocales();
    	Iterator iter=locales.iterator();
    	while (iter.hasNext() ){
    		 Locale localeTmp=(Locale)iter.next();
    		 String languageTmp = localeTmp.getLanguage();
			 country = localeTmp.getCountry();
			 if (languageTmp.equals(language)) {
				 logger.trace("OUT:"+country);
				 return country;
			 }
    	}
		logger.trace("OUT:"+country);
		return country;
	}

	public static JSONArray getSupportedLocalesAsJSONArray() {
		logger.trace("IN");
		JSONArray toReturn = new JSONArray();
		try {
			List<Locale> locales = getSupportedLocales();
			Iterator<Locale> it = locales.iterator();
			while (it.hasNext()) {
				Locale locale = it.next();
				JSONObject localeJSON = new JSONObject();
				localeJSON.put("language", locale.getLanguage());
				localeJSON.put("country", locale.getCountry());
				toReturn.put(localeJSON);
			}
		} catch (Exception e) {
			logger.error("Error while retrieving supported locales as JSONArray", e);
		}
		logger.trace("OUT");
		return toReturn;
	}

	public static Locale getCurrentLocale(RequestContainer requestContainer) {
		Locale locale=null;
		if(requestContainer!=null){    	
			SessionContainer permSession = requestContainer.getSessionContainer().getPermanentContainer();
			if(permSession!=null){			
				String language=(String)permSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
				String country=(String)permSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
				if(language!=null && country!=null){
					locale=new Locale(language,country,"");
				}
			}
		}
		if(locale==null)locale=getDefaultLocale();
		return locale;
	}

	public static String getLocaleDateFormat(SessionContainer permSess){
		String language=(String)permSess.getAttribute("AF_LANGUAGE");
		String country=(String)permSess.getAttribute("AF_COUNTRY");
		String format=null;
		// if a particular language is specified take the corrisponding date-format
		if(language!=null ){
			if(country==null){
				format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-"+language.toUpperCase()+".format");
			}
			else{
				format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-"+language.toUpperCase()+"_"+country.toUpperCase()+".format");				
			}		
		}
		if(format==null){
			format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT.format");
		}
		logger.debug("DATE FORMAT.format:"+format);
		return format;

	}
	
	public static String getLocaleDateFormat(Locale locale){
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String format = null;
		// if a particular language is specified take the corrisponding date-format
		if(language!=null ){
			if(country==null){
				format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-"+language.toUpperCase()+".format");
			}
			else{
				format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-"+language.toUpperCase()+"_"+country.toUpperCase()+".format");				
			}		
		}
		if(format==null){
			format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT.format");
		}
		logger.debug("DATE FORMAT.format:"+format);
		return format;

	}

	public static String getLocaleDateFormatForExtJs(SessionContainer permSess){
		String language=(String)permSess.getAttribute("AF_LANGUAGE");
		String country=(String)permSess.getAttribute("AF_COUNTRY");
		String format=null;
		// if a particular language is specified take the corrisponding date-format
		if(language!=null ){
			if(country==null){
				format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-"+language.toUpperCase()+".extJsFormat");
			}
			else{
				format =SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-"+language.toUpperCase()+"_"+country.toUpperCase()+".extJsFormat");	
			}		
		}
		if(format==null){
			format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT.extJsFormat");
		}
		if (format == null) {
			logger.warn("Locale date format for ExtJs not found, using d/m/Y as deafult");
			format = "d/m/Y";
		}
		logger.debug("DATE FORMAT.extJsFormat:"+format);
		return format;

	}

	public static String getServerDateFormat(){
		logger.debug("IN");
		String format="dd/MM/yyyy"; 
		// if a particular language is specified take the corrisponding date-format
		format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format");

		logger.debug("OUT");
		return format;
	}

	public static String getServerTimeStampFormat(){
		logger.debug("IN");
		String format="dd/MM/yyyy HH:mm:ss";
		// if a particular language is specified take the corrisponding date-format
		format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.TIMESTAMP-FORMAT.format");

		logger.debug("OUT");
		return format;
	}

	public static String getServerDateFormatExtJs(){
		logger.debug("IN");
		String format="d/m/Y";
		// if a particular language is specified take the corrisponding date-format
		format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.extJsFormat");

		logger.debug("OUT");
		return format;
	}

	public static String getServerTimestampFormatExtJs(){
		logger.debug("IN");
		String format="d/m/Y H:i:s";
		// if a particular language is specified take the corrisponding date-format
		format = SingletonConfig.getInstance().getConfigValue("SPAGOBI.TIMESTAMP-FORMAT.extJsFormat");

		logger.debug("OUT");
		return format;
	}

	public static int getTemplateMaxSize() {
		logger.debug("IN");
		int toReturn = MAX_DEFAULT_TEMPLATE_SIZE;
		try {
			SingletonConfig serverConfig = SingletonConfig.getInstance();
			String maxSizeStr = serverConfig.getConfigValue("SPAGOBI.TEMPLATE_MAX_SIZE");
			if (maxSizeStr != null) {
				logger.debug("Configuration found for max template size: " + maxSizeStr);
				Integer maxSizeInt = new Integer(maxSizeStr);
				toReturn = maxSizeInt.intValue();
			} else {
				logger.debug("No configuration found for max template size");
			}
		} catch (Exception e) {
			logger.error("Error while retrieving max template size", e);
			logger.debug("Considering default value " + MAX_DEFAULT_TEMPLATE_SIZE);
			toReturn = MAX_DEFAULT_TEMPLATE_SIZE;
		}
		logger.debug("OUT: max size = " + toReturn);
		return toReturn;
	}
	
	public static int getDataSetFileMaxSize() {
		logger.debug("IN");
		int toReturn = MAX_DEFAULT_FILE_DATASET_SIZE;
		try {
			SingletonConfig serverConfig = SingletonConfig.getInstance();
			String maxSizeStr = serverConfig.getConfigValue("SPAGOBI.DATASET_FILE_MAX_SIZE");
			if (maxSizeStr != null) {
				logger.debug("Configuration found for max dataset file size: " + maxSizeStr);
				Integer maxSizeInt = new Integer(maxSizeStr);
				toReturn = maxSizeInt.intValue();
			} else {
				logger.debug("No configuration found for max dataset file size");
			}
		} catch (Exception e) {
			logger.error("Error while retrieving max dataset file size", e);
			logger.debug("Considering default value " + MAX_DEFAULT_FILE_DATASET_SIZE);
			toReturn = MAX_DEFAULT_FILE_DATASET_SIZE;
		}
		logger.debug("OUT: max size = " + toReturn);
		return toReturn;
	}

	public static String getSpagoBiContext() {
		logger.debug("IN");
		String path = "";
		try {
			logger.debug("Trying to recover spagobi context from ConfigSingleton");
			SingletonConfig spagoConfig = SingletonConfig.getInstance();
			path = spagoConfig.getConfigValue("SPAGOBI.SPAGOBI_CONTEXT");
			if (path==null){
				logger.debug("SPAGOBI_CONTEXT not set, using the default value ");
				path="/SpagoBI";
			}
			logger.debug("SPAGOBI_CONTEXT: " + path);
		} catch (Exception e) {
			logger.error("Error while recovering SpagoBI context address", e);
		}
		logger.debug("OUT:" + path);
		return path;
	}      


	public static String getSessionExpiredURL() {
		logger.debug("IN");
		String sessionExpiredUrl = null;
		try {
			logger.debug("Trying to recover SpagoBI session expired url from ConfigSingleton");
			SingletonConfig spagoConfig = SingletonConfig.getInstance();
			sessionExpiredUrl = spagoConfig.getConfigValue("SPAGOBI.SESSION_EXPIRED_URL");
		} catch (Exception e) {
			logger.error("Error while recovering SpagoBI session expired url", e);
		}
		logger.debug("OUT: SpagoBI session expired url is " + sessionExpiredUrl);
		return sessionExpiredUrl;
	}  

	/**
	 * Returns an url starting with the given base url and adding parameters retrieved by the input parameters map.
	 * Each parameter value is encoded using URLEncoder.encode(value, "UTF-8");
	 * @param baseUrl The base url
	 * @param mapPars The parameters map; those parameters will be added to the base url (values will be encoded using UTF-8 encoding)
	 * @return an url starting with the given base url and adding parameters retrieved by the input parameters map
	 */
	public static String getUrl(String baseUrl, Map mapPars) {
		logger.debug("IN");
		Assert.assertNotNull(baseUrl, "Base url in input is null");
		StringBuffer buffer = new StringBuffer();
		buffer.append(baseUrl);
		buffer.append(baseUrl.indexOf("?") == -1 ? "?" : "&");
		if (mapPars != null && !mapPars.isEmpty()) {
			java.util.Set keys = mapPars.keySet();
			Iterator iterKeys = keys.iterator();
			while (iterKeys.hasNext()) {
				String key = iterKeys.next().toString();
				Object valueObj = mapPars.get(key);
				if (valueObj != null) {
					String value = valueObj.toString();
					// encoding value
					try {
						value = URLEncoder.encode(value, "UTF-8");

						// put all + to space!  that is because 
						// otherwise %2B (encoding of plus) and + (substitution of white space in an url) 
						//will otherwise be interpreted in the same way
						// and when using exporter I would no more be able to distinguish + from ' '
						//value = value.replaceAll(Pattern.quote("+") , " "); 

					} catch (UnsupportedEncodingException e) {
						logger.warn("UTF-8 encoding is not supported!!!", e);
						logger.warn("Using system encoding...");
						value = URLEncoder.encode(value);
					}

					buffer.append(key + "=" + value);
					if (iterKeys.hasNext()) {
						buffer.append("&");
					}
				}
			}
		}
		logger.debug("OUT: " + buffer.toString());
		return buffer.toString();
	}

	/** getParametersFromURL: 
	 *  takes an url and return a Map containing URL parameters
	 * @param urlString
	 * @return map containing url parameters
	 */

	public static Map getParametersFromURL(String urlString) {
		logger.debug("IN");
		Map toReturn=new HashMap<String, String>();
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			logger.error("Malformed URL Exception "+urlString,e);
			return null;
		}
		//get parameters string
		String parameters=url.getQuery();
		StringTokenizer st = new StringTokenizer(parameters, "&", false);

		String parameterToken = null;
		String parameterName = null;
		String parameterValue = null;
		while (st.hasMoreTokens()){
			parameterToken = st.nextToken();
			parameterName = parameterToken.substring(0, parameterToken.indexOf("="));
			String parameterValueEncoded = parameterToken.substring(parameterToken.indexOf("=") + 1);

			// do the decode
			try {
				parameterValue = URLDecoder.decode(parameterValueEncoded, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("Error in decoding parameter: UTF 8 not supported "+parameterName+ "; use preceding value "+parameterValueEncoded, e);
				parameterValue = parameterValueEncoded;
			}
			catch (java.lang.IllegalArgumentException e) { // can happen when in document composition a '%' char is given
				logger.warn("Error in decoding parameter, illegal argument for "+parameterName+ " (probably value % is present); use preceding value "+parameterValueEncoded);
				parameterValue = parameterValueEncoded;
			}
			catch (Exception e) { 
				logger.warn("Generic Error in decoding parameter "+parameterName+ " ; use preceding value "+parameterValueEncoded);
				parameterValue = parameterValueEncoded;
			}

			// if is already present create a list
			if(toReturn.keySet().contains(parameterName)){
				Object prevValue=toReturn.get(parameterName).toString();
				List<String> toInsert=null;
				// if was alrady a list
				if( prevValue instanceof List ){
					toInsert=(List<String>)prevValue;
					toInsert.add(parameterValue);
				}
				else{ // else create a new list and add both elements
					toInsert=new ArrayList<String>();
					toInsert.add(prevValue.toString());
					toInsert.add(parameterValue);
				}
				// put list
				toReturn.put(parameterName, toInsert);
			}
			else{ // case single value
				toReturn.put(parameterName, parameterValue);
			}
		}
		logger.debug("OUT");
		return toReturn;
	}
	
	public static int getDatasetMaxResults() {
		int maxResults = Integer.MAX_VALUE;
		String maxResultsStr = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATASET.maxResult");
		if (maxResultsStr != null) {
			maxResults = Integer.parseInt(maxResultsStr);
		} else {
			logger.warn("Dataset max results configuration not found. Check spagobi.xml, SPAGOBI.DATASET.maxResults attribute");
			logger.debug("Using default value that is Integer.MAX_VALUE = " + Integer.MAX_VALUE);
		}
		return maxResults;
	}
	
	public static File getPreviewFilesStorageDirectoryPath () {
		String path = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
		String resourcePath = SpagoBIUtilities.readJndiResource(path);
		if (resourcePath.endsWith("/") || resourcePath.endsWith("\\")) {
			resourcePath += PREVIEW_FILE_STORAGE_DIRECTORY;
		} else {
			resourcePath += File.separatorChar + PREVIEW_FILE_STORAGE_DIRECTORY;
		}
		File file = FileUtils.checkAndCreateDir(resourcePath);
		return file;
	}
	
}
