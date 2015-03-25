/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.behaviouralmodel.lov.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONFailure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class SaveLovAction extends AbstractSpagoBIAction{

	static private Logger logger = Logger.getLogger(DetailModalitiesValueModule.class);
	private EMFErrorHandler errorHandler;
	
	private SessionContainer session;
	
	private IEngUserProfile profile;
	
	@Override
	public void doService() {
		
		String message = getAttributeAsString("MESSAGEDET");
		logger.debug("begin of detail Modalities Value modify/visualization service with message =" +message);
		
		RequestContainer requestContainer = this.getRequestContainer();
		session = requestContainer.getSessionContainer();
		
		errorHandler = getErrorHandler();
		try {
			// recover user profile
			session = requestContainer.getSessionContainer();
			SessionContainer permanentSession = session.getPermanentContainer();
			profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// process message
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				logger.debug("The message parameter is null");
				throw userError;
			} 
			if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_MOD)) {
				modDetailModValue(AdmintoolsConstants.DETAIL_MOD);
			} else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
				modDetailModValue(AdmintoolsConstants.DETAIL_INS);
			} 	
			
			writeBackToClient(new JSONAcknowledge());
			
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			logger.error("Exception occurred writing back to client", internalError);
			try {
				writeBackToClient(new JSONFailure(ex));
			} catch (Exception ex2) {
				logger.error("Exception occurred writing back to client", ex2);
				throw new SpagoBIServiceException("Exception occurred writing back to client", ex2);
			} 
			
		}
	}
	
	
	
	/**
	 * Inserts/Modifies the detail of a value according to the user 
	 * request. When a value in the LOV list is modified, the <code>modifyModalitiesValue</code> 
	 * method is called; when a new parameter use mode is added, the <code>inserModalitiesValue</code>
	 * method is called. These two cases are differentiated by the <code>mod</code> String input value .
	 * 
	 * @param request The request information contained in a SourceBean Object
	 * @param mod A request string used to differentiate insert/modify operations
	 * @param response The response SourceBean 
	 * @throws EMFUserError If an exception occurs
	 * @throws SourceBeanException If a SourceBean exception occurs
	 */
	private void modDetailModValue(String mod) throws EMFUserError, SourceBeanException {
		ModalitiesValue modVal = null;
		HashMap<String, String> logParam = new HashMap();
		boolean responseTestLov;
		boolean responseLoopback;
		
		try {
			modVal = (ModalitiesValue) session.getAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
			
			logParam.put("NAME",modVal.getName());
			logParam.put("TYPE",modVal.getITypeCd());			
			logParam.put("LABEL",modVal.getLabel());	

			// to rember that the lov has been modified 
			// necessary to show a confirm if the user change the lov and then go back without saving
			String lovProviderModified = getAttributeAsString("lovProviderModified");
			if(lovProviderModified != null && !lovProviderModified.trim().equals("")) 
				session.setAttribute(SpagoBIConstants.LOV_MODIFIED, lovProviderModified);
			
			// check if we are coming from the test
			String returnFromTestMsg =  getAttributeAsString("RETURN_FROM_TEST_MSG");
			
				// save after the test
				if ("SAVE".equalsIgnoreCase(returnFromTestMsg)) {		
					
					// validate data
					//ValidationCoordinator.validate("PAGE", "LovTestColumnSelector", this);
					// if there are some validation errors return to test page 
					Collection errors = errorHandler.getErrors();
					if (errors != null && errors.size() > 0) {
						Iterator iterator = errors.iterator();
						while (iterator.hasNext()) {
							Object error = iterator.next();
							if(error instanceof EMFValidationError) {
								responseTestLov = true;
								AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.ADD/MODIFY", logParam, "KO");
								return;
							}
						}
					}
					
					
					List<String> invisCols = null;
					JSONObject lovConfiguration = getAttributeAsJSONObject(AdmintoolsConstants.LOV_CONFIGURATION);
					String valueColumn = lovConfiguration.getString("valueColumnName");
					String descriptionColumn = lovConfiguration.getString("descriptionColumnName");
					String lovType = lovConfiguration.optString("lovType");
					JSONArray treeLevelColumns = lovConfiguration.optJSONArray("treeLevelsColumns");
					JSONArray visibleColumns = lovConfiguration.optJSONArray("visibleColumnNames");
					JSONArray columns = lovConfiguration.optJSONArray("column");
					List<String> visibleColumnsList = new ArrayList<String>();
					if(visibleColumns!=null){
						for(int i=0; i<visibleColumns.length(); i++){
							visibleColumnsList.add(visibleColumns.getString(i));
						}	
					}
					List<String> treeLevelColumnsList = new ArrayList<String>();
					if(treeLevelColumns!=null){
						for(int i=0; i<treeLevelColumns.length(); i++){
							treeLevelColumnsList.add(treeLevelColumns.getString(i));
						}
					}
					if(columns!=null){
						invisCols = getInvisibleColumns(columns, visibleColumnsList);
					}
					 
					
					String lovProvider = modVal.getLovProvider();
					ILovDetail lovDetail = LovDetailFactory.getLovFromXML(lovProvider);
					lovDetail.setDescriptionColumnName(descriptionColumn);
					if(invisCols!=null){
						lovDetail.setInvisibleColumnNames(invisCols);
					}
					lovDetail.setValueColumnName(valueColumn);
					if(visibleColumnsList!=null){
						lovDetail.setVisibleColumnNames(visibleColumnsList);
					}
					if(treeLevelColumnsList!=null){
						lovDetail.setTreeLevelsColumns(treeLevelColumnsList);
					}
					if(lovType!=null){
						lovDetail.setLovType(lovType);
					}
					String newLovProvider = lovDetail.toXML();
					modVal.setLovProvider(newLovProvider);
					
					session.delAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
					session.delAttribute(SpagoBIConstants.MODALITY);
				} 
				
				
				// don't save after the test
				else if ("DO_NOT_SAVE".equalsIgnoreCase(returnFromTestMsg)) {
					modVal = (ModalitiesValue) session.getAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
					//session.delAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
					//session.delAttribute(SpagoBIConstants.MODALITY);
					
					//prepareDetailModalitiesValuePage(modVal, mod, response);
					
					// exits without writing into DB and without loop
					return;
				}

			
			// finally (if there are no error, if there is no request for test or to
			// add or delete a Fix Lov item) writes into DB
			if(mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
				IModalitiesValueDAO dao=DAOFactory.getModalitiesValueDAO();
				dao.setUserProfile(profile);
				dao.insertModalitiesValue(modVal);
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.ADD", logParam, "OK");
			}else{
				IModalitiesValueDAO dao=DAOFactory.getModalitiesValueDAO();
				dao.setUserProfile(profile);
				dao.modifyModalitiesValue(modVal);
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.MOD", logParam, "OK");
			} 
			
		} catch (Exception ex) {			
			logger.error("Cannot fill response container", ex  );
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListLovsModule.MODULE_PAGE);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.ADD/MODIFY", logParam, "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1018, new Vector(), params);
		}
		
		responseLoopback = true;
		session.delAttribute(SpagoBIConstants.LOV_MODIFIED);

	}

	
	private List<String> getInvisibleColumns(JSONArray columns, List<String> visibleColumns)throws JSONException {
		List<String> invisibleCols = new ArrayList<String>();
		for (int i = 0; i < columns.length(); i++) {
			String column = columns.getString(i);
			if(!visibleColumns.contains(column)){
				invisibleCols.add(column);
			}
		}
		return invisibleCols;
	}
	


}