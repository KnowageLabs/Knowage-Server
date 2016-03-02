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
package it.eng.spagobi.tools.catalogue.service;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetMetaModelVersionsAction extends AbstractSpagoBIAction {

	// logger component
	public static Logger logger = Logger.getLogger(GetMetaModelVersionsAction.class);

	public static String MODEL_ID = "id";
	
	@Override
	public void doService() {
		
		logger.debug("IN");
		
		try {
			
			Integer modelId = this.getAttributeAsInteger( MODEL_ID );
			logger.debug("Model id = " + modelId);
			Assert.assertNotNull(modelId, "Input model id parameter cannot be null");
			
			IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
			dao.setUserProfile(this.getUserProfile());
			List<Content> versions = dao.loadMetaModelVersions(modelId);
			logger.debug("Read " + versions.size() + " existing model versions");

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
