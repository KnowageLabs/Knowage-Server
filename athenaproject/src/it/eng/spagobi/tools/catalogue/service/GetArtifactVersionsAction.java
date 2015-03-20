/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.catalogue.service;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetArtifactVersionsAction extends AbstractSpagoBIAction {

	// logger component
	public static Logger logger = Logger.getLogger(GetArtifactVersionsAction.class);

	public static String ARTIFACT_ID = "id";
	
	@Override
	public void doService() {
		
		logger.debug("IN");
		
		try {
			
			Integer artifactId = this.getAttributeAsInteger( ARTIFACT_ID );
			logger.debug("Artifact id = " + artifactId);
			Assert.assertNotNull(artifactId, "Input artifact id parameter cannot be null");
			
			IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
			dao.setUserProfile(this.getUserProfile());
			List<Content> versions = dao.loadArtifactVersions(artifactId);
			logger.debug("Read " + versions.size() + " existing artifact versions");

			try {
				JSONArray versionsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(versions, null);
				JSONObject results = new JSONObject();
				results.put("results", versionsJSON);
				writeBackToClient( new JSONSuccess( results ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to write back the responce to the client",
						e);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Cannot serialize objects into a JSON object", e);
			} catch (SerializationException e) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Cannot serialize objects into a JSON object", e);
			}

		} finally {
			logger.debug("OUT");
		}
		
	}

}
