/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 24-mar-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.utilities;

import it.eng.spago.base.PortletAccess;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.log4j.Logger;


/**
 * Contains some SpagoBI's portlet utilities.
 * 
 * @author Zoppello
 */
public class PortletUtilities {
	
	
	static private Logger logger = Logger.getLogger(PortletUtilities.class);
	
	/**
	 * Starting from the original URL and the request, creates a string representing the
	 * Portlet URL.
	 * 
	 * @param aHttpServletRequest The request object at input
	 * @param originalURL The starting original URL
	 * 
	 * @return A String representing the Portlet URL
	 */
	public static String createPortletURL(HttpServletRequest aHttpServletRequest, String originalURL){

		RenderResponse renderResponse =(RenderResponse)aHttpServletRequest.getAttribute("javax.portlet.response");
		
		PortletURL aPortletURL = renderResponse.createActionURL();
		
		logger.debug("Original URL.... " + originalURL + "indexOf ? is " + originalURL.indexOf("?"));
		
		String parameters = originalURL.substring(originalURL.indexOf("?")+1);
		
		StringTokenizer st = new StringTokenizer(parameters, "&", false);
		
		String parameterToken = null;
		String parameterName = null;
		String parameterValue = null;
		while (st.hasMoreTokens()){
			parameterToken = st.nextToken();
			logger.debug("Parameter Token [" + parameterToken +"]");
			
			parameterName = parameterToken.substring(0, parameterToken.indexOf("="));
			parameterValue = parameterToken.substring(parameterToken.indexOf("=") + 1);
			
			logger.debug("Parameter Name [" + parameterName +"]");
			logger.debug("Parameter Value [" + parameterValue +"]");
			
			aPortletURL.setParameter(parameterName, parameterValue);
		}
		
		
		return aPortletURL.toString();
	}
	
	/**
	 * Creates the particular portlet URL for a resource, given its path.
	 * 
	 * @param aHttpServletRequest The request object at input
	 * @param resourceAbsolutePath The resource Absolute path
	 * 
	 * @return The resource Portlet URL String
	 */
	public static String  createPortletURLForResource(HttpServletRequest aHttpServletRequest, String resourceAbsolutePath){
		RenderResponse renderResponse =(RenderResponse)aHttpServletRequest.getAttribute("javax.portlet.response");
		RenderRequest renderRequest =(RenderRequest)aHttpServletRequest.getAttribute("javax.portlet.request");
		
		return renderResponse.encodeURL(renderRequest.getContextPath() + resourceAbsolutePath).toString();
	}
	
	/**
	 * Gets the <code>PortletRequest</code> object.
	 * 
	 * @return The portlet request object
	 */
	public static PortletRequest getPortletRequest(){
		return PortletAccess.getPortletRequest();
	}
	
	/**
	 * Gets the <code>PortletResponse</code> object.
	 * 
	 * @return The portlet response object
	 */
	public static PortletResponse getPortletResponse(){
		return PortletAccess.getPortletResponse();
	}
	
	/**
	 * Gets the first uploaded file from a portlet request. This method creates a new file upload handler, 
	 * parses the request, processes the uploaded items and then returns the first file as an
	 * <code>UploadedFile</code> object.
	 * @param portletRequest The input portlet request
	 * @return	The first uploaded file object.
	 */
	public static UploadedFile getFirstUploadedFile(PortletRequest portletRequest){
		UploadedFile uploadedFile = null;
		try{
			
			DiskFileItemFactory factory = new DiskFileItemFactory();
			//		 Create a new file upload handler
			PortletFileUpload upload = new PortletFileUpload(factory);
		
			//		 Parse the request
			List /* FileItem */ items = upload.parseRequest((ActionRequest)portletRequest);
		
		
			//		 Process the uploaded items
			Iterator iter = items.iterator();
			boolean endLoop = false;
			while (iter.hasNext() && !endLoop) {
				FileItem item = (FileItem) iter.next();

				if (item.isFormField()) {
					//serviceRequest.setAttribute(item.getFieldName(), item.getString());
				} else {
					uploadedFile = new UploadedFile();
					uploadedFile.setFileContent(item.get());
					uploadedFile.setFieldNameInForm(item.getFieldName());
					uploadedFile.setSizeInBytes(item.getSize());
					uploadedFile.setFileName(item.getName());
					
					endLoop = true;
				}
			}
		}catch(Exception e){
			logger.error("Cannot parse multipart request", e);
		}
		return uploadedFile;
		
	}
	
	/**
	 * Gets the portal locale.
	 * 
	 * @return the portal locale
	 */
	public static Locale getPortalLocale() {
		return PortletAccess.getPortalLocale();
	}
	
	/**
	 * Gets a localized message given its code and bundle
	 * information. If there isn't any message matching to these infromation, a
	 * warning is traced.
	 * 
	 * @param code The message's code string
	 * @param bundle The message's bundel string
	 * 
	 * @return A string containing the message
	 */
	 public static String getMessage(String code, String bundle) {

		Locale locale = getLocaleForMessage();  
		 
	 	ResourceBundle messages = ResourceBundle.getBundle(bundle, locale);
        if (messages == null) {
            return null;
        } 
        String message = code;
        try {
            message = messages.getString(code);
        } 
        catch (Exception ex) {
        	logger.warn("code [" + code + "] not found ", ex);
        } 
        return message;
    } // public String getMessage(String code)
	 
	 
	
		/**
		 * Get the locale of the portal or the one setted into the configuration files 
		 * @return locale for message resolution
		 */
		public static Locale getLocaleForMessage() {
			logger.info("IN");
			
			Locale locale = null;
			Locale portalLocale;
			 
			
			try {
			 	portalLocale =  PortletAccess.getPortalLocale();
			 	if (portalLocale == null) {
		        	logger.error("Portal locale not found by PortletAccess.getPortalLocale() method");
			 	} else {
			 		logger.debug("Portal locale read succesfully: [" +  portalLocale.getLanguage() + "," + portalLocale.getCountry() + "]");
			 	}
			 	
			 	if( isLocaleSupported(portalLocale) ) {
			 		logger.debug("Portal locale [" +  portalLocale.getLanguage() + "," + portalLocale.getCountry() + "] is supported by SpagoBI");
			 		locale = portalLocale; 
			 	} else {
			 		logger.warn("Portal locale [" +  portalLocale.getLanguage() + "," + portalLocale.getCountry() + "] is not supported by SpagoBI");
			 		locale = getDefaultLocale();
			 		logger.debug("Default locale [" +  locale.getLanguage() + "," + locale.getCountry() + "] will be used");
			 	}
			 	
			} catch (Exception e) {
				logger.error("Error while getting portal locale", e);
			}
						
		 	logger.info("OUT");
		 	
		 	return locale;
		}
	
		private static boolean isLocaleSupported(Locale locale) {
				

			String defaultLocal=SingletonConfig.getInstance().getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGES");
			String tmp="["+locale.getLanguage()+","+locale.getCountry()+"]";
			logger.debug("Check if "+tmp+" is supported");	
			return defaultLocal.contains(tmp);
		}
		
		
		
	 
	 /**
 	 * Gets the language code of the user portal language. If it's not possible to gather
 	 * the locale of the portal it returns the default language code
 	 * 
 	 * @return A string containing the language code
 	 */
		 public static String getPortalLanguageCode() {
			 try {
			 	Locale portalLocale =  PortletAccess.getPortalLocale();
			 	if(portalLocale == null) {
		        	logger.error( "Portal locale not found by PortletAccess.getPortalLocale() method!! " +
				              			"May be there is not a portlet request");
			 	} else {
			 		String portalLang = portalLocale.getLanguage();
			 		return portalLang;
			 	}
			 } catch (Exception e) {
				 logger.error(  "Error while getting portal locale", e);
				 
			 }
			 
			 // get the configuration sourceBean/language code/country code of the default language
			 String languageConfig = SingletonConfig.getInstance().getConfigValue("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default");

		 	 return languageConfig.substring(0, 2);
	    } 
	
	/*
	 * Methods copied from GeneralUtilities for DAO Refactoring
	 */

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
}
