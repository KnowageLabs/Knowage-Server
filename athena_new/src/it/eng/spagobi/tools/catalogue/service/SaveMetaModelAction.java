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
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
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

public class SaveMetaModelAction extends AbstractSpagoBIAction {

	// logger component
	public static Logger logger = Logger.getLogger(SaveMetaModelAction.class);

	public static String ID = "id";
	public static String NAME = "name";
	public static String DESCRIPTION = "description";
	public static String CATEGORY = "category";
	public static String DATA_SOURCE_LABEL = "data_source_label";
	public static String ACTIVE_CONTENT_ID = "active_content_id";
	
	@Override
	public void doService() {
		
		logger.debug("IN");
		
		try {
		
			IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
			dao.setUserProfile(this.getUserProfile());
			
			MetaModel model = getMetaModelFromRequest();
			LogMF.debug(logger, "Model read from request : [{0}]", model);
			
			Content content = getContentFromRequest();
			LogMF.debug(logger, "Content read from request : [{0}]", content);
			
			HashMap logParameters = new HashMap<String, String>();
			logParameters.put("MODEL", model.toString());
			String logOperation = null;
			
			try {
				if (isNew(model)) {
					MetaModel existing = dao.loadMetaModelByName(model.getName());
					if (existing != null) {
						logger.debug("A meta model with name already exists");
						throw new SpagoBIServiceException(SERVICE_NAME, "A meta model with name already exists");
					}
					logOperation = "META_MODEL_CATALOGUE.ADD";
					dao.insertMetaModel(model);
					logger.debug("Model [" + model + "] inserted");
				} else {
					MetaModel existing = dao.loadMetaModelByName(model.getName());
					if (existing != null && !existing.getId().equals(model.getId())) {
						logger.debug("A meta model with name already exists");
						throw new SpagoBIServiceException(SERVICE_NAME, "A meta model with name already exists");
					}
					logOperation = "META_MODEL_CATALOGUE.MODIFY";
					dao.modifyMetaModel(model);
					logger.debug("Model [" + model + "] updated");
				}
				
				if (content != null) {
					dao.insertMetaModelContent(model.getId(), content);
					logger.debug("Content [" + content + "] inserted");
				} else {
					if (this.requestContainsAttribute(ACTIVE_CONTENT_ID)) {
						Integer activeContentId = this.getAttributeAsInteger(ACTIVE_CONTENT_ID);
						logger.debug("Active content id [" + activeContentId + "]");
						dao.setActiveVersion(model.getId(), activeContentId);
					}
				}
				
			} catch (SpagoBIServiceException e) {
				throw e;
			} catch (Throwable t) {
				AuditLogUtilities.updateAudit(getHttpRequest(), this.getUserProfile(), logOperation, logParameters , "KO");
				throw new SpagoBIServiceException(this.getActionName(), "Error while saving meta model", t);
			}
			
			AuditLogUtilities.updateAudit(getHttpRequest(), this.getUserProfile(), logOperation, logParameters , "OK");
			
			try {
				JSONObject result = new JSONObject();
				result.put("id", model.getId());
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

	
	private boolean isNew(MetaModel model) {
		return model.getId() == 0;
	}

	private MetaModel getMetaModelFromRequest() {
		Integer id = getAttributeAsInteger( ID );
		String name = getAttributeAsString( NAME );
		String description = getAttributeAsString( DESCRIPTION );
		String category = getAttributeAsString(CATEGORY);
		String dataSourceLabel = getAttributeAsString(DATA_SOURCE_LABEL);
		Integer categoryValue = null;
		if (StringUtilities.isNotEmpty(category)){
			categoryValue = getAttributeAsInteger( CATEGORY );		
		}
		MetaModel model = new MetaModel();
		model.setId(id);
		model.setName(name);
		model.setDescription(description);
		model.setCategory(categoryValue);
		model.setDataSourceLabel(dataSourceLabel);
		return model;
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
