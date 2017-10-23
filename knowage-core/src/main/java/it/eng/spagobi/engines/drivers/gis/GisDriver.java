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
package it.eng.spagobi.engines.drivers.gis;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class GisDriver extends GenericDriver {
	static private Logger logger = Logger.getLogger(GisDriver.class);

	@Override
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		// logger.warn("Function not implemented");

		// TODO Auto-generated method stub
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
		parameters.put(DOCUMENT_ID, documentId);
		String documentLabel = obj.getLabel();
		parameters.put(DOCUMENT_LABEL, documentLabel);
		parameters.put(IS_TECHNICAL_USER, UserUtilities.isTechnicalUser(profile));
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url.replace("/execute", "/edit"), parameters);
		logger.debug("OUT");
		return engineURL;
		// throw new InvalidOperationRequest();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.engines.drivers.IEngineDriver#getNewDocumentTemplateBuildUrl
	 * (java.lang.Object, it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		// logger.warn("Function not implemented");

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
		parameters.put(DOCUMENT_ID, documentId);
		String documentLabel = obj.getLabel();
		parameters.put(DOCUMENT_LABEL, documentLabel);
		parameters.put(IS_TECHNICAL_USER, UserUtilities.isTechnicalUser(profile));
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url.replace("/execute", "/edit"), parameters);
		logger.debug("OUT");
		return engineURL;
		// return new EngineURL("pippo.jsp", new HashMap<>());
		// throw new InvalidOperationRequest();
	}

}
