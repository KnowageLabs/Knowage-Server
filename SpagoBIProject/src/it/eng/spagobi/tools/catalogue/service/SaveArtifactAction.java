/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.catalogue.service;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIServiceExceptionHandler;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.IServiceResponse;
import it.eng.spagobi.utilities.service.JSONResponse;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class SaveArtifactAction extends AbstractSpagoBIAction {

	// logger component
	public static Logger logger = Logger.getLogger(SaveArtifactAction.class);

	public static String ID = "id";
	public static String NAME = "name";
	public static String DESCRIPTION = "description";
	public static String ACTIVE_CONTENT_ID = "active_content_id";
	public static String TYPE = "type";
	
	@Override
	public void doService() {
		
		logger.debug("IN");
		
		try {
		
			IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
			dao.setUserProfile(this.getUserProfile());
			
			Artifact artifact = getArtifactFromRequest();
			LogMF.debug(logger, "Artifact read from request : [{0}]", artifact);
			
			Content content = getContentFromRequest();
			LogMF.debug(logger, "Content read from request : [{0}]", content);
			
			HashMap logParameters = new HashMap<String, String>();
			logParameters.put("ARTIFACT", artifact.toString());
			String logOperation = null;
			
			try {
				if (isNew(artifact)) {
					Artifact existing = dao.loadArtifactByNameAndType(artifact.getName(), artifact.getType());
					if (existing != null) {
						logger.debug("An artifact with the same name and type already exists");
						throw new SpagoBIServiceException(SERVICE_NAME, "An artifact with the same name already exists");
					}
					logOperation = "ARTIFACT_CATALOGUE.ADD";
					dao.insertArtifact(artifact);
					logger.debug("Artifact [" + artifact + "] inserted");
				} else {
					Artifact existing = dao.loadArtifactByNameAndType(artifact.getName(), artifact.getType());
					if (existing != null && !existing.getId().equals(artifact.getId())) {
						logger.debug("An artifact with the same name and type already exists");
						throw new SpagoBIServiceException(SERVICE_NAME, "An artifact with the same name already exists");
					}
					logOperation = "ARTIFACT_CATALOGUE.MODIFY";
					dao.modifyArtifact(artifact);
					logger.debug("Artifact [" + artifact + "] updated");
				}
				
				if (content != null) {
					dao.insertArtifactContent(artifact.getId(), content);
					logger.debug("Content [" + content + "] inserted");
				} else {
					if (this.requestContainsAttribute(ACTIVE_CONTENT_ID)) {
						Integer activeContentId = this.getAttributeAsInteger(ACTIVE_CONTENT_ID);
						logger.debug("Active content id [" + activeContentId + "]");
						dao.setActiveVersion(artifact.getId(), activeContentId);
					}
				}
				
			} catch (SpagoBIServiceException e) {
				throw e;
			} catch (Throwable t) {
				AuditLogUtilities.updateAudit(getHttpRequest(), this.getUserProfile(), logOperation, logParameters , "KO");
				throw new SpagoBIServiceException(this.getActionName(), "Error while saving artifact", t);
			}
			
			AuditLogUtilities.updateAudit(getHttpRequest(), this.getUserProfile(), logOperation, logParameters , "OK");
			
			try {
				JSONObject result = new JSONObject();
				result.put("id", artifact.getId());
				replayToClient( result.toString() , null );
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the response to the client", e);
			}
			
		} catch (Throwable t) {
			SpagoBIServiceException e = SpagoBIServiceExceptionHandler.getInstance().getWrappedException(SERVICE_NAME, t);
			replayToClient( null, e );
		} finally {
			logger.debug("OUT");
		}
		
	}
	
    /*
     * see Ext.form.BasicForm for file upload
     */
	private void replayToClient(final String msg, final SpagoBIServiceException e) {
		
		try {
			
			writeBackToClient(  new IServiceResponse() {
				
				public boolean isInline() {
					return false;
				}
				
				public int getStatusCode() {
					if ( e != null) {
						return JSONResponse.FAILURE;
					}
					return JSONResponse.SUCCESS;
				}
				
				public String getFileName() {
					return null;
				}
				
				public String getContentType() {
					return "text/html";
				}
				
				public String getContent() throws IOException {
					try {
						JSONObject toReturn = new JSONObject();
						toReturn.put("success", e == null);
						toReturn.put("msg", e == null ? msg : e.getMessage());
						return toReturn.toString();
					} catch (JSONException jSONException) {
						logger.error(jSONException);
						return "{success : false, msg : 'Error serializing response object'}";
					}
				}
			});
			
		} catch (IOException ioException) {
			logger.error("Impossible to write back the responce to the client", ioException);
		}
	}

	private void checkUploadedFile(FileItem uploaded) {
		logger.debug("IN");
		try {
			// check if the uploaded file exceeds the maximum dimension
			int maxSize = GeneralUtilities.getTemplateMaxSize();
			if (uploaded.getSize() > maxSize) {
				throw new SpagoBIEngineServiceException(getActionName(), "The uploaded file exceeds the maximum size, that is " + maxSize);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	
	private boolean isNew(Artifact artifact) {
		return artifact.getId() == 0;
	}

	private Artifact getArtifactFromRequest() {
		Integer id = getAttributeAsInteger( ID );
		String name = getAttributeAsString( NAME );
		String description = getAttributeAsString( DESCRIPTION );
		String type = this.getAttributeAsString(TYPE);
		Artifact artifact = new Artifact();
		artifact.setId(id);
		artifact.setName(name);
		artifact.setDescription(description);
		artifact.setType(type);
		return artifact;
	}
	
	private Content getContentFromRequest() {
		Content content = null;
		FileItem uploaded = (FileItem) getAttribute("UPLOADED_FILE");
		if (uploaded != null && uploaded.getSize() > 0) {
			checkUploadedFile(uploaded);
			String fileName = GeneralUtilities.getRelativeFileNames(uploaded.getName());
			content = new Content();
			content.setActive(new Boolean(true));
			UserProfile userProfile = (UserProfile) this.getUserProfile();
			content.setCreationUser(userProfile.getUserId().toString());
			content.setCreationDate(new Date());
			content.setDimension(Long.toString(uploaded.getSize()/1000)+" KByte");
			content.setFileName(fileName);
	        byte[] uplCont = uploaded.get();
	        content.setContent(uplCont);
		} else {
			logger.debug("Uploaded file missing or it is empty");
		}
		return content;
	}
	
}
