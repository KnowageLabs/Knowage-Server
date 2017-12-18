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
import it.eng.spagobi.tools.catalogue.bo.Artifact;
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

public class GetArtifactsAction extends AbstractSpagoBIAction {

	// logger component
	public static Logger logger = Logger.getLogger(GetArtifactsAction.class);

	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 15;
	public static String TYPE = "type";
	
	@Override
	public void doService() {
		
		logger.debug("IN");
		
		try {
			
			String type = this.getAttributeAsString(TYPE);
			logger.debug("Type: " + type );
			Assert.assertNotNull(type, "Input type parameter cannot be null");
			
			IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
			dao.setUserProfile(this.getUserProfile());
			List<Artifact> allArtifacts = dao.loadAllArtifacts(type);
			logger.debug("Read " + allArtifacts.size() + " existing artifacts of type [" + type + "]");
			
			Integer start = this.getStart();
			logger.debug("Start : " + start );
			Integer limit = this.getLimit();
			logger.debug("Limit : " + limit );
			
			int startIndex = Math.min(start, allArtifacts.size());
			int stopIndex = Math.min(start + limit, allArtifacts.size());
			List<Artifact> artifacts = allArtifacts.subList(startIndex, stopIndex);

			try {
				JSONArray artifactsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(artifacts, null);
				JSONObject rolesResponseJSON = createJSONResponse(
						artifactsJSON, allArtifacts.size());
				writeBackToClient(new JSONSuccess(rolesResponseJSON));
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
	
	private Integer getStart() {
		Integer start = getAttributeAsInteger( START );
		if (start == null) {
			start = START_DEFAULT;
		}
		return start;
	}
	
	private Integer getLimit() {
		Integer limit = getAttributeAsInteger( LIMIT );
		if (limit == null) {
			limit = LIMIT_DEFAULT;
		}
		return limit;
	}
	
	private JSONObject createJSONResponse(JSONArray rows, Integer totalResNumber)
			throws JSONException {
		JSONObject results;
		results = new JSONObject();
		results.put("total", totalResNumber);
		results.put("title", "Artifacts");
		results.put("rows", rows);
		return results;
	}
	
}
