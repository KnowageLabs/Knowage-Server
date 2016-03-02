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

package it.eng.spagobi.engines.drivers.jpalo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;

import org.apache.log4j.Logger;

/**
 * Driver Implementation (IEngineDriver Interface) for Jasper Report Engine.
 */
public class JPaloDriver extends GenericDriver implements IEngineDriver {

    static private Logger logger = Logger.getLogger(JPaloDriver.class);
	/**
	 * Returns the url to be invoked for editing template document.
	 * 
	 * @param biobject The biobject
	 * @param profile the profile
	 * 
	 * @return the url to be invoked for editing template document
	 * 
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl();
		HashMap parameters = new HashMap();
		String documentId = obj.getId().toString();
		parameters.put("document", documentId);
		parameters.put("isSpagoBIDev", "true");
		applyLocale(parameters);
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url, parameters);
		logger.debug("OUT");
		return engineURL;
	}

	/**
	 * Returns the url to be invoked for creating a new template document.
	 * 
	 * @param biobject The biobject
	 * @param profile the profile
	 * 
	 * @return the url to be invoked for creating a new template document
	 * 
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl();
		HashMap parameters = new HashMap();
		String documentId = obj.getId().toString();
		parameters.put("document", documentId);
		parameters.put("isSpagoBIDev", "true");
		parameters.put("isNewDocument", "true");
		applyLocale(parameters);
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url, parameters);
		logger.debug("OUT");
		return engineURL;
	}

    private Map applyLocale(Map map) {
    	logger.debug("IN");

    	
    	Locale locale = getLocale();
    	map.put("country", locale.getCountry());
    	map.put("language", locale.getLanguage());

    	logger.debug("OUT");
    	return map;
    }
    private Locale getLocale() {
    	logger.debug("IN");
		try {
			Locale locale = null;
			RequestContainer requestContainer = RequestContainer.getRequestContainer();
			SessionContainer permanentSession = requestContainer.getSessionContainer().getPermanentContainer();
			String language = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String country = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
			locale = new Locale(language, country);
			return locale;
		} catch (Exception e) {
		    logger.error("Error while getting locale; using default one", e);
		    return GeneralUtilities.getDefaultLocale();
		} finally  {
			logger.debug("OUT");
		}	
	}
}
