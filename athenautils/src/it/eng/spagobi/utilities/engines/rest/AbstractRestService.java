/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.utilities.engines.rest;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineInstance;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The Class AbstractRestService.
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
public abstract class AbstractRestService {

	public ExecutionSession es;

	/**
	 * Creates the context manager
	 *
	 * @return ExecutionSession container of the execution manager
	 */
	public ExecutionSession getExecutionSession() {
		if (es == null) {
			es = new ExecutionSession(getServletRequest(), getServletRequest().getSession());
		}
		return es;
	}

	/**
	 * Gets the what if engine instance.
	 *
	 * @return the console engine instance
	 */
	public IEngineInstance getEngineInstance() {
		return (IEngineInstance) es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);
	}

	/**
	 * Check if the number is null
	 *
	 * @param value
	 *            the value to check
	 * @return true if the value is null
	 */
	public boolean isNull(Number value) {
		return value == null;
	}

	/**
	 * Check if the string is null
	 *
	 * @param value
	 *            the value to check
	 * @return true if the value is null
	 */
	public boolean isNull(String value) {
		return value == null || value.equals("null") || value.equals("undefined");
	}

	/**
	 * Check if the string is null or ""
	 *
	 * @param value
	 *            the value to check
	 * @return true if the value is null or ""
	 */
	public boolean isNullOrEmpty(String value) {
		return isNull(value) || value.equals("");
	}

	public Map getEnv() {
		return getEngineInstance().getEnv();
	}

	public Locale getLocale() {
		return (Locale) getEnv().get(EngineConstants.ENV_LOCALE);
	}

	public Locale buildLocaleFromSession() {
		Locale locale = null;
		Object countryO = getHttpSession().getAttribute(SpagoBIConstants.AF_COUNTRY);
		Object languageO = getHttpSession().getAttribute(SpagoBIConstants.AF_LANGUAGE);
		String country = countryO != null ? countryO.toString() : null;
		String language = languageO != null ? languageO.toString() : null;
		if (country != null && language != null) {
			locale = new Locale(language, country);
		}
		return locale;
	}

	/**
	 * Gets the HttpServletRequest.. A standard implementation is to get the HttpServletRequest from the context.. The implementing class can be:
	 *
	 * public class XXXEngineService extends AbstractRestService{
	 *
	 * @Context protected HttpServletRequest servletRequest;
	 *
	 *          public HttpServletRequest getServletRequest(){ return servletRequest; }
	 * @return the HttpServletRequest
	 */
	public abstract HttpServletRequest getServletRequest();

	public HttpSession getHttpSession() {
		return getServletRequest().getSession();
	}

	public Object getAttributeFromHttpSession(String attrName) {
		return getHttpSession().getAttribute(attrName);
	}

	public Object getAttributeFromExecutionSession(String attrName) {
		return getExecutionSession().getAttributeFromSession(attrName);
	}

	public String getAttributeFromSessionAsString(String attrName) {
		return getExecutionSession().getAttributeFromSessionAsString(attrName);
	}

	public boolean requestContainsAttribute(String attrName) {
		return getExecutionSession().requestContainsAttribute(attrName);
	}

	public boolean requestContainsAttribute(String attrName, String attrValue) {
		return (requestContainsAttribute(attrName) && getAttribute(attrName).toString().equalsIgnoreCase(attrValue));
	}

	public Object getAttribute(String attrName) {
		return getExecutionSession().getAttribute(attrName);
	}

	public String getAttributeAsString(String attrName) {
		return getExecutionSession().getAttributeAsString(attrName);
	}

	public Integer getAttributeAsInteger(String attrName) {
		return getExecutionSession().getAttributeAsInteger(attrName);
	}
}
