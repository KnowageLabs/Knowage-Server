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

package it.eng.spagobi.api;

/**
 * @author Giulio Gavardi (giulio.gavardi@eng.it)
 * 
 * @class LockerArtifactResource
 * 
 * Provides services to lock of the model
 * 
 */

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.whatif.dao.IWhatifWorkflowDAO;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.Logger;
import org.json.JSONObject;

@Path("/1.0/locker")
public class ArtifactResource extends AbstractSpagoBIResource {

	public static transient Logger logger = Logger.getLogger(ArtifactResource.class);


	/**
	 * Service to lock the artifact
	 * @return 
	 * 
	 */
	@POST
	@Path("/{artifactId}")
	public String lockArtifact(@PathParam("artifactId") int artifactId){
		logger.debug("IN");

		try {
			IWhatifWorkflowDAO iwfd = DAOFactory.getWhatifWorkflowDAO();
			String locker = iwfd.goNextUserByModel(artifactId);
			String status = "locked_by_other";
			if(locker==null){
				status = "unlocked";
			}
			JSONObject resultsJSON = new JSONObject();			
			resultsJSON.put("status", status);
			if(locker != null)
				resultsJSON.put("locker", locker);
			return resultsJSON.toString();	

		} catch(Throwable t) {
			logger.error(t);
			throw new SpagoBIRuntimeException(t);
		} 
		finally {			
			logger.debug("OUT");
		}
	}
}



