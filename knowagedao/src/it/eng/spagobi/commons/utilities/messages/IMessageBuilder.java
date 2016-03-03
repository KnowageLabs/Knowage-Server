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

import javax.servlet.http.HttpServletRequest;

/**
 * This is the interface for classes that implements logic
 * to retrieve localized messages to be used in JSP pages 
 */
public interface IMessageBuilder {

	/**
	 * Gets the message.
	 * 
	 * @param code the code
	 * 
	 * @return the message associated with code in the default bundle
	 */
	//public String getMessage(RequestContainer aRequestContainer, String code);

	/**
	 * @param aRequestContainer: Spago Request Container
	 * @param code: the message code
	 * @param bundle: the bundle to use
	 * @return  the message associated with code in the given bundle
	 */
	//public String getMessage(RequestContainer aRequestContainer, String code, String bundle);


	/**
	 * Returns the message associated with code in the default bundle
	 * @param code: the message code
	 * @return: the message associated with code in the default bundle
	 */
	public String getMessage(String code);

	/**
	 * If the specified locale is valid, returns the message associated with code in the default bundle with the specified locale;
	 * otherwise returns the message associated with code in the default bundle with default locale.
	 * 
	 * @param code the code
	 * @param locale the locale
	 * 
	 * @return if the specified locale is valid, returns the message associated with code in the default bundle with the specified locale;
	 * otherwise returns the message associated with code in the default bundle with default locale
	 */
	public String getMessage(String code, Locale locale);

	/**
	 * Returns the message associated with code in the given bundle.
	 * 
	 * @param code the code
	 * @param bundle the bundle
	 * 
	 * @return  the message associated with code in the given bundle
	 */
	public String getMessage(String code, String bundle);

	/**
	 * If the specified locale is valid, returns the message associated with code in the input bundle with the specified locale;
	 * otherwise returns the message associated with code in the input bundle with default locale.
	 * 
	 * @param code the code
	 * @param bundle the bundle
	 * @param locale the locale
	 * 
	 * @return if the specified locale is valid, returns the message associated with code in the input bundle with the specified locale;
	 * otherwise returns the message associated with code in the input bundle with default locale
	 */
	public String getMessage(String code, String bundle, Locale locale);

	/**
	 * Returns the message associated with code in the default bundle.
	 * 
	 * @param code the code
	 * @param request the request
	 * 
	 * @return the message associated with code in the default bundle
	 */
	public String getMessage(String code, HttpServletRequest request);

	/**
	 * If the specified locale is valid, returns the message associated with code in the default bundle with the specified locale;
	 * otherwise returns the message associated with code in the default bundle with request locale.
	 * 
	 * @param code the code
	 * @param request the request
	 * @param locale the locale
	 * 
	 * @return if the specified locale is valid, returns the message associated with code in the default bundle with the specified locale;
	 * otherwise returns the message associated with code in the default bundle with request locale
	 */
	public String getMessage(String code, HttpServletRequest request, Locale locale);

	/**
	 * Returns the message associated with code in the given bundle.
	 * 
	 * @param code the code
	 * @param bundle the bundle
	 * @param request the request
	 * 
	 * @return  the message associated with code in the given bundle
	 */
	public String getMessage(String code, String bundle, HttpServletRequest request);

	/**
	 * If the specified locale is valid, returns the message associated with code in the input bundle with the specified locale;
	 * otherwise returns the message associated with code in the input bundle with request locale.
	 * 
	 * @param code the code
	 * @param bundle the bundle
	 * @param request the request
	 * @param locale the locale
	 * 
	 * @return if the specified locale is valid, returns the message associated with code in the input bundle with the specified locale;
	 * otherwise returns the message associated with code in the input bundle with request locale
	 */
	public String getMessage(String code, String bundle, HttpServletRequest request, Locale locale);

	/**
	 * Gets a localized information text given the resource name which contains the text
	 * information.
	 * The resource will be searched into the classpath of the application
	 * 
	 * @param resourceName The complete name of the resource.
	 * @param request The http request for locale retrieving
	 * 
	 * @return the localized text contained into the resource
	 */
	public String getMessageTextFromResource(String resourceName, HttpServletRequest request);

	/**
	 * If the specified locale is valid, returns the localized text contained into the resource with the specified locale;
	 * otherwise returns the localized text contained into the resource with default locale.
	 * 
	 * @param resourceName The complete name of the resource
	 * @param locale the locale
	 * 
	 * @return if the specified locale is valid, returns the localized text contained into the resource with the specified locale;
	 * otherwise returns the localized text contained into the resource with default locale
	 */
	public String getMessageTextFromResource(String resourceName, Locale locale);

	/** Internationalization of user messages via DB
	 * 
	 * @param locale
	 * @param code
	 * @return
	 */
	public String getI18nMessage(Locale locale, String code);

	/** Internationalization of user messages via DB
	 * 
	 * @param code
	 * @param request
	 * @return
	 */
	public String getI18nMessage(String code, HttpServletRequest request);

	/**
	 *  Previous user messages internationalized via bundle	
	 */
	// public String getUserMessage(String code, String bundle, Locale locale);
	// public String getUserMessage(String code, String bundle, HttpServletRequest request);


}
