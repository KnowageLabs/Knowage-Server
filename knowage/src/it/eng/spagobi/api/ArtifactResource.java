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

import java.util.List;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import javax.ws.rs.GET;
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
	@Path("/{artifactId}/lock")
	public String lockArtifact(@PathParam("artifactId") int artifactId){
		logger.debug("IN");

		Object profileO = getAttributeFromHttpSession(IEngUserProfile.ENG_USER_PROFILE);
		if(profileO == null){
			return ExceptionUtilities.serializeException("Profile not found when executing service", null);

		}
		String userId = ((IEngUserProfile)profileO).getUserUniqueIdentifier().toString();		

		logger.debug("User Id is "+userId);
		logger.debug("Artifact Id is "+artifactId);

		IArtifactsDAO artifactsDAO = DAOFactory.getArtifactsDAO();

		Artifact artifact = artifactsDAO.loadArtifactById(artifactId);
		
		if(artifact == null)	{
			logger.error("Artifact referring to id [" + artifactId +"] could not be loaded");
			return ExceptionUtilities.serializeException("Artifact with id [" + artifactId + "] could not be loaded", null);
		}
		
		//Integer artifactId = artifact.getId();
		logger.debug("Artifact id is "+artifactId);
		
		String locker = artifactsDAO.lockArtifact(artifactId, userId);
		String status = null;
		if(locker != null && locker.equals(userId)){
			logger.debug("Artifact with artifact "+artifactId+" was locked by current user "+locker);
			status = SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_USER;
		}
		else if(locker != null){
			logger.debug("Artifact with artifact "+artifactId+" was already locked by user "+locker);
			status = SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_OTHER;
		}
		else{
			logger.debug("Artifact with artifact "+artifactId+" was not locked");
			status = SpagoBIConstants.SBI_ARTIFACT_VALUE_UNLOCKED;
		}
		
		logger.debug("Artifact with artifact "+artifactId+" is in status "+status+" ");
		try {
			JSONObject resultsJSON = new JSONObject();			
			resultsJSON.put("status", status);
			if(locker != null)
				resultsJSON.put("locker", locker);
			return resultsJSON.toString();	
		} catch(Throwable t) {
			return ExceptionUtilities.serializeException("An unexpected error occured while executing service", null);
		} 
		finally {			
			logger.debug("OUT");
		}
	}


		/**
		 * Service to unlock the artifact
		 * @return 
		 * 
		 */
		@POST
		@Path("/{artifactId}/unlock")
		public String unlockArtifact(@PathParam("artifactId") int artifactId){
			logger.debug("IN");

			Object profileO = getAttributeFromHttpSession(IEngUserProfile.ENG_USER_PROFILE);
			if(profileO == null){
				return ExceptionUtilities.serializeException("Profile not found when executing service", null);

			}
			String userId = ((IEngUserProfile)profileO).getUserUniqueIdentifier().toString();		

			logger.debug("User Id is "+userId);
			logger.debug("Artifact Id is "+artifactId);

			IArtifactsDAO artifactsDAO = DAOFactory.getArtifactsDAO();

			Artifact artifact = artifactsDAO.loadArtifactById(artifactId);
			
			if(artifact == null)	{
				logger.error("Artifact referring to id [" + artifactId +"] could not be loaded");
				return ExceptionUtilities.serializeException("Artifact with id [" + artifactId + "] could not be loaded", null);
			}

			//Integer artifactId = artifact.getId();
			logger.debug("Artifact id is "+artifactId);
			
			String locker = artifactsDAO.unlockArtifact(artifactId, userId);
			String status = null;

			if(locker == null){
				logger.debug("Artifact was unlocked");
				status = SpagoBIConstants.SBI_ARTIFACT_VALUE_UNLOCKED;
			}
			else{
				logger.warn("Artifact was not unlocked and is hold by locker "+locker);
				if(locker != null && userId.equals(locker)){
					status = SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_USER;
				}
				else{
					status = SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_OTHER;

				}			
			}
			
			
			logger.debug("Artifact with artifact "+artifactId+" is in status "+status+" ");

			try {
				JSONObject resultsJSON = new JSONObject();			
				resultsJSON.put("status", status);
				if(locker != null)
					resultsJSON.put("locker", locker);
				return resultsJSON.toString();
			} catch(Throwable t) {
				return ExceptionUtilities.serializeException("An unexpected error occured while executing service", null);
			} 
			finally {			
				logger.debug("OUT");
			}

	}
		
		
		/**
		 * Service to return the artifact status
		 * 
		 * @return 
		 *  unlocked 
		 * locked_by_you
		 * locked_by_other
		 * 
		 */
//		@POST
//		@Path("/getStatus/{artifactVersionId}")
//		public String getArtifactStatus(@PathParam("artifactVersionId") int artifactVersionId){
//			logger.debug("IN");
//			String statusToReturn = null;
//			
//			Object profileO = getAttributeFromHttpSession(IEngUserProfile.ENG_USER_PROFILE);
//			if(profileO == null){
//				return ExceptionUtilities.serializeException("Profile not found when executing service", null);
//
//			}
//			String userId = ((IEngUserProfile)profileO).getUserUniqueIdentifier().toString();		
//
//			logger.debug("User Id is "+userId);
//			logger.debug("Artifact Version Id is "+artifactVersionId);
//
//			IArtifactsDAO artifactsDAO = DAOFactory.getArtifactsDAO();
//
//			Artifact artifact = artifactsDAO.loadArtifactByContentId(artifactVersionId);
//			
//			if(artifact == null)	{
//				logger.error("Artifact referring to version id [" + artifactVersionId +"] could not be loaded");
//				return ExceptionUtilities.serializeException("Artifact with versioon id [" + artifactVersionId + "] could not be loaded", null);
//			}
//
//			
//			Integer artifactId = artifact.getId();
//			
//			logger.debug("Artifact id is "+artifactId);
//
//			Boolean locked = artifact.getLocked();
//			String locker = artifact.getLocker();
//
//			if(locked==false){
//				logger.debug("Artifact with id "+artifactId+" is unlocked");
//				statusToReturn="unlocked";
//			}
//			else{
//				if(locker != null && locker.equals(userId)){
//					statusToReturn="locked_by_you";	
//				}
//				else{
//					statusToReturn="locked_by_other";	
//				}
//					
//					
//				}
//			
//			logger.debug("Status of artifact is "+statusToReturn);
//
//			try {
//				JSONObject resultsJSON = new JSONObject();			
//				resultsJSON.put("status", statusToReturn);
//				return resultsJSON.toString();	
//			} catch(Throwable t) {
//				return ExceptionUtilities.serializeException("An unexpected error occured while executing service", null);
//			} 
//			finally {			
//				logger.debug("OUT");
//			}
//
//	}


}
