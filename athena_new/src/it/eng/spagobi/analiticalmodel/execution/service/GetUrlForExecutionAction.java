/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * @author Zerbetto Davide
 */
public class GetUrlForExecutionAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "GET_URL_FOR_EXECUTION_ACTION";

	public static final String SBI_SUBOBJECT_ID = "SBI_SUBOBJECT_ID";
	public static final String SBI_SNAPSHOT_ID = "SBI_SNAPSHOT_ID";
	public static final String IS_FROM_CROSS = "isFromCross";
	
	public static final String PARAMETERS = "PARAMETERS";

	// logger component
	private static Logger logger = Logger.getLogger(GetUrlForExecutionAction.class);

	public void doService() {
		Integer subObjectId;
		Integer snapshotId;
		boolean isFromCross;
		JSONObject response;
		
		logger.debug("IN");
		try {
			subObjectId = this.getAttributeAsInteger( SBI_SUBOBJECT_ID );
			logger.debug("Parameter [" + SBI_SUBOBJECT_ID + "] is equals to [" + subObjectId + "]");
			
			snapshotId = this.getAttributeAsInteger( SBI_SNAPSHOT_ID );
			logger.debug("Parameter [" + SBI_SNAPSHOT_ID + "] is equals to [" + snapshotId + "]");
			
			isFromCross = this.getAttributeAsBoolean( IS_FROM_CROSS );
			logger.debug("Parameter [" + IS_FROM_CROSS + "] is equals to [" + isFromCross + "]");
						
			response = null;
			if (snapshotId != null) {
				response = handleSnapshotExecution(snapshotId);
			} else if (subObjectId != null) {
				response = handleSubObjectExecution(subObjectId, isFromCross);
			} else {
				response = handleNormalExecution(isFromCross);
			}
			
			Assert.assertNotNull(response, "An internal error occurred while generating service response. Service response cannot be null");
						
			try {
				writeBackToClient( new JSONSuccess( response ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private JSONObject handleSnapshotExecution(Integer snapshotId) {
		
		JSONObject response= null;
		
		ISnapshotDAO dao;
		Snapshot snapshot;
		BIObject obj;
		String url;
		ExecutionInstance executionInstance;
		
		logger.debug("IN");
		snapshot = null;
		UserProfile profile = (UserProfile) this.getUserProfile();
		executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
		obj = executionInstance.getBIObject();
		dao = null;
		HashMap<String, String> logParam = new HashMap();
		logParam.put("DOCUMENT NAME", obj.getName());
		logParam.put("PARAMS", this.getAttributeAsString( PARAMETERS ));
		try {
			dao = DAOFactory.getSnapshotDAO();
		} catch (EMFUserError e) {				
			logger.error("Error while istantiating DAO", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot access database", e);
		}
		
		try {
			snapshot = dao.loadSnapshot(snapshotId);
		} catch (EMFUserError e) {
			try {
				
				logParam.put("SNAPSHOT ID", snapshotId.toString());				
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.GET_URL_FOR_SNAPSHOT",logParam , "KO");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		 
			logger.error("Snapshot with id = " + snapshotId + " not found", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Scheduled execution not found", e);
		}	
		logParam.put("SNAPSHOT NAME", snapshot.getName());
		try {
			Assert.assertNotNull(executionInstance, "Execution instance cannot be null in order to properly generate execution url");
			
			
			// we are not executing a subobject, so delete subobject if existing
			executionInstance.setSubObject(null);
			
			Assert.assertNotNull(dao, "An internal error occurred while istantiating DAO. DAO cannot be null.");

			
			Assert.assertNotNull(dao, "An internal error occurred while loading snapshot [" + snapshotId + "]. Snapshot cannot be null.");
			
			if (obj.getId().equals(snapshot.getBiobjId())) {
				executionInstance.setSnapshot(snapshot);
				url = executionInstance.getSnapshotUrl();
				response = new JSONObject();
				try {
					response.put("url", url);
				} catch (JSONException e) {
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.GET_URL_FOR_SNAPSHOT",logParam , "KO");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					throw new SpagoBIServiceException("Cannot serialize the url [" + url + "] to the client", e);
				}
			} else {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.GET_URL_FOR_SNAPSHOT",logParam , "KO");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				throw new SpagoBIServiceException(SERVICE_NAME, "Required scheduled execution is not relevant to current document");
			}
		} finally {
			logger.debug("OUT");
		}
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.GET_URL_FOR_SNAPSHOT",logParam , "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	protected JSONObject handleSubObjectExecution(Integer subObjectId, boolean isFromCross) {
		logger.debug("IN");
		
		ExecutionInstance executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
		UserProfile userProfile = (UserProfile) this.getUserProfile();
		BIObject obj = executionInstance.getBIObject();
		HashMap<String, String> logParam = new HashMap();
		logParam.put("DOCUMENT NAME", obj.getName());	
		logParam.put("PARAMS", this.getAttributeAsString( PARAMETERS ));
		
		JSONObject response = new JSONObject();
		ISubObjectDAO dao = null;
		try {
			dao = DAOFactory.getSubObjectDAO();
		} catch (EMFUserError e) {				
			logger.error("Error while istantiating DAO", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot access database", e);
		}
		SubObject subObject = null;
		try {
			subObject = dao.getSubObject(subObjectId);
			
		} catch (EMFUserError e) {
			try {
				logParam.put("SUBOBJECT ID", subObjectId.toString());				
				AuditLogUtilities.updateAudit(getHttpRequest(),  userProfile, "DOCUMENT.GET_URL_FOR_SUBOBJ", logParam, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		 
			logger.error("SubObject with id = " + subObjectId + " not found", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Customized view not found", e);
		}
		logParam.put("SUBOBJECT NAME", subObject.getName());
		try {
			executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			Assert.assertNotNull(executionInstance, "Execution instance cannot be null in order to properly generate execution url");
			
			// we are not executing a snapshot, so delete snapshot if existing
			executionInstance.setSnapshot(null);
			
			Locale locale = this.getLocale();
			
			List errors = null;
			//if (executionInstance.getBIObject().getBiObjectTypeCode().equalsIgnoreCase("DATAMART")) {
				// parameters are applied to datamarts' subobjects, so you must validate them
				JSONObject executionInstanceJSON = this.getAttributeAsJSONObject( PARAMETERS );
				executionInstance.refreshParametersValues(executionInstanceJSON, false);
				try {
					errors = executionInstance.getParametersErrors();
				} catch (Exception e) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Cannot evaluate errors on parameters validation", e);
				}
			//}
				

			if ( errors != null && errors.size() > 0) {
				// there are errors on parameters validation, send errors' descriptions to the client
				JSONArray errorsArray = new JSONArray();
				Iterator errorsIt = errors.iterator();
				while (errorsIt.hasNext()) {
					EMFUserError error = (EMFUserError) errorsIt.next();
					errorsArray.put(error.getDescription());
				}
				try {
					response.put("errors", errorsArray);
				} catch (JSONException e) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize errors to the client", e);
				}
			} else {				
				if (obj.getId().equals(subObject.getBiobjId())) {
					boolean canExecuteSubObject = false;
					if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
						canExecuteSubObject = true;
					} else {
						if (subObject.getIsPublic() || subObject.getOwner().equals(userProfile.getUserId().toString())) {
							canExecuteSubObject = true;
						}
					}
					if (canExecuteSubObject) {
						executionInstance.setSubObject(subObject);
						String url = executionInstance.getSubObjectUrl(locale);
						url += "&isFromCross=" + (isFromCross == true ? "true" : "false");
						try {
							response.put("url", url);
						} catch (JSONException e) {
							try {
								AuditLogUtilities.updateAudit(getHttpRequest(),  userProfile, "DOCUMENT.GET_URL_FOR_SUBOBJ",logParam , "ERR");
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}	
							throw new SpagoBIServiceException("Cannot serialize the url [" + url + "] to the client", e);
						}
					} else {
						try {
							AuditLogUtilities.updateAudit(getHttpRequest(),  userProfile, "DOCUMENT.GET_URL_FOR_SUBOBJ",logParam , "KO");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}	
						throw new SpagoBIServiceException(SERVICE_NAME, "User cannot execute required customized view");
					}
				} else {
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  userProfile, "DOCUMENT.GET_URL_FOR_SUBOBJ",logParam , "KO");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					throw new SpagoBIServiceException(SERVICE_NAME, "Required subobject is not relevant to current document");
				}
			}
		} catch (EMFInternalError e) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  userProfile, "DOCUMENT.GET_URL_FOR_SUBOBJ",logParam , "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			throw new SpagoBIServiceException(SERVICE_NAME, "An internal error has occured", e);
		} finally {
			logger.debug("OUT");
		}
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  userProfile, "DOCUMENT.GET_URL_FOR_SUBOBJ",logParam , "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	protected JSONObject handleNormalExecution(boolean isFromCross) {
		ExecutionInstance executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
		
		logger.debug("IN");
		UserProfile profile = (UserProfile) this.getUserProfile();
		JSONObject response = new JSONObject();
		HashMap<String, String> logParam = new HashMap();
		logParam.put("NAME", executionInstance.getBIObject().getName());
		logParam.put("ENGINE", executionInstance.getBIObject().getEngine().getName());
		logParam.put("PARAMS", this.getAttributeAsString( PARAMETERS ));
		try {
			Assert.assertNotNull(executionInstance, "Execution instance cannot be null in order to properly generate execution url");
			
			// we are not executing a subobject or a snapshot, so delete subobject/snapshot if existing
			executionInstance.setSubObject(null);
			executionInstance.setSnapshot(null);
			JSONObject executionInstanceJSON = this.getAttributeAsJSONObject( PARAMETERS );
			executionInstance.refreshParametersValues(executionInstanceJSON, false);

			Locale locale=this.getLocale();

			List errors = null;
			try {
				errors = executionInstance.getParametersErrors();
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot evaluate errors on parameters validation", e);
			}
			if ( errors != null && errors.size() > 0) {
				// there are errors on parameters validation, send errors' descriptions to the client
				JSONArray errorsArray = new JSONArray();
				Iterator errorsIt = errors.iterator();
				while (errorsIt.hasNext()) {
					EMFUserError error = (EMFUserError) errorsIt.next();
					errorsArray.put(error.getDescription());
				}
				try {
					response.put("errors", errorsArray);
				} catch (JSONException e) {
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.GET_URL",logParam , "ERR");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize errors to the client", e);
				}
			} else {
				
				// there are no errors, we can proceed, so calculate the execution url and send it back to the client
				String url = executionInstance.getExecutionUrl(locale);
				url += "&isFromCross=" + (isFromCross == true ? "true" : "false");
				//adds information about the environment
				String env= (String)this.getAttributeAsString("SBI_ENVIRONMENT");
				if (env==null){
					env = "DOCBROWSER";
				}				
				url += "&SBI_ENVIRONMENT=" + env;
				try {
					response.put("url", url);
				} catch (JSONException e) {
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.GET_URL",logParam , "KO");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.GET_URL",logParam , "ERR");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize the url [" + url + "] to the client", e);
				}
			}
		} finally {
			logger.debug("OUT");
		}
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.GET_URL",logParam , "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

}
