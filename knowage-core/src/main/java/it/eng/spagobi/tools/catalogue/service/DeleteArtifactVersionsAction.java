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
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Zerbetto Davide
 */
public class DeleteArtifactVersionsAction extends AbstractSpagoBIAction {
	
	public static final String VERSIONS_ID = "id";
	
	// logger component
	private static Logger logger = Logger.getLogger(DeleteArtifactVersionsAction.class);
	
	public void doService() {
		logger.debug("IN");
		
		try {
			IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
			dao.setUserProfile(this.getUserProfile());
			
			String ids = this.getAttributeAsString(VERSIONS_ID);
			// ids contains the id of the versions to be deleted separated by ,
			String[] idArray = ids.split(",");
			for (int i = 0; i < idArray.length; i++) {
				Integer id = new Integer(idArray[i]);
				logger.debug("Deleting artifact content with id = " + id);
				dao.eraseArtifactContent(id);
				logger.debug("Artifact content with id = " + id + " successfully deleted");
			}
			try {
				JSONObject results = new JSONObject();
				results.put("result", "OK");
				writeBackToClient( new JSONSuccess( results ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(this.getActionName(), "Impossible to write back the responce to the client", e);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(this.getActionName(), "Cannot serialize objects into a JSON object", e);
			}

		} finally {
			logger.debug("OUT");
		}
	}

}
