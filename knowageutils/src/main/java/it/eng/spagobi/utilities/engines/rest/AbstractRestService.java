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

package it.eng.spagobi.utilities.engines.rest;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineInstance;

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
		return (IEngineInstance) getExecutionSession().getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);
	}

	/**
	 * Check if the number is null
	 *
	 * @param value the value to check
	 * @return true if the value is null
	 */
	public boolean isNull(Number value) {
		return value == null;
	}

	/**
	 * Check if the string is null
	 *
	 * @param value the value to check
	 * @return true if the value is null
	 */
	public boolean isNull(String value) {
		return value == null || value.equals("null") || value.equals("undefined");
	}

	/**
	 * Check if the string is null or ""
	 *
	 * @param value the value to check
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
		HttpSession httpSession = getHttpSession();
		if (httpSession != null) {
			Object countryO = httpSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			Object languageO = httpSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String country = countryO != null ? countryO.toString() : null;
			String language = languageO != null ? languageO.toString() : null;
			if (country != null && language != null) {
				locale = new Locale(language, country);
			}
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
		if (getServletRequest() != null) {
			return getServletRequest().getSession(false);
		} else {
			return null;
		}
	}

	public Object getAttributeFromHttpSession(String attrName) {
		Assert.assertNotNull(getHttpSession(), "Trying to get an attribute from session but session was not even initialized!");
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

	public JSONObject getAttributeAsJSONObject(String attrName) {
		return getExecutionSession().getAttributeAsJSONObject(attrName);
	}

	public JSONArray getAttributeAsJSONArray(String attrName) {
		return getExecutionSession().getAttributeAsJSONArray(attrName);
	}
}
