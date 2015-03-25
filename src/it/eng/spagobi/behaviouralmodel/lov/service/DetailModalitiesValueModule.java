/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spago.validation.coordinator.ValidationCoordinator;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.DatasetDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListItemDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.security.ISecurityInfoProvider;
import it.eng.spagobi.security.SecurityInfoProviderFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Implements a module which  handles all predefined List of Values (LOV) management: 
 * has methods for LOV load, detail, modify/insertion and deleting operations. 
 * The <code>service</code> method has  a switch for all these operations, differentiated the ones 
 * from the others by a <code>message</code> String.
 */

public class DetailModalitiesValueModule extends AbstractHttpModule {
	static private Logger logger = Logger.getLogger(DetailModalitiesValueModule.class);
	private EMFErrorHandler errorHandler;
	
	private SessionContainer session;
	
	private IEngUserProfile profile;
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
	}

	/**
	 * Reads the operation asked by the user and calls the insertion, modify, detail and
	 * deletion methods.
	 * <p>
	 * When a new value is defined, the user has to use a wizard to build all
	 * the new value definition. There are some methods written for this aim.
	 * 
	 * @param request The Source Bean containing all request parameters
	 * @param response The Source Bean containing all response parameters
	 * 
	 * @throws exception If an exception occurs
	 * @throws Exception the exception
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		
		String message = (String) request.getAttribute("MESSAGEDET");
		logger.debug("begin of detail Modalities Value modify/visualization service with message =" +message);
		
		RequestContainer requestContainer = this.getRequestContainer();
		session = requestContainer.getSessionContainer();
		
		
		errorHandler = getErrorHandler();
		try {
			// recover user profile
			RequestContainer reqCont = getRequestContainer();
			SessionContainer sessCont = reqCont.getSessionContainer();
			ResponseContainer responseContainer = this.getResponseContainer();	
			session = requestContainer.getSessionContainer();
			SessionContainer permanentSession = session.getPermanentContainer();
			profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// process message
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				logger.debug("The message parameter is null");
				throw userError;
			} 
			if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_SELECT)) {
				String id = (String) request.getAttribute("id");
				getDetailModValue(id, response);
			} 	else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_MOD)) {
				modDetailModValue(request, AdmintoolsConstants.DETAIL_MOD, response);
			} 	else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_NEW)) {
				newDetailModValue(response);
			} 	else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
				modDetailModValue(request, AdmintoolsConstants.DETAIL_INS, response);
			} 	else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_DEL)) {
				delDetailModValue(request, AdmintoolsConstants.DETAIL_DEL, response);
			} 	else if (message.trim().equalsIgnoreCase("EXIT_FROM_DETAIL")){
				exitFromDetail(request, response);
			}   else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_TEST_AFTER_ATTRIBUTES_FILLING)) {
				testLovAfterAttributesFilling(request, response);
			}

		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}
	}
	
	private void exitFromDetail (SourceBean request, SourceBean response) throws SourceBeanException {
		response.setAttribute("loopback", "true");
		session.delAttribute(SpagoBIConstants.LOV_MODIFIED);
		session.delAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
	}
	
	/**
	 * Gets the detail of a value choosed by the user from the 
	 * predefined List of Values. It reaches the key from the request and asks 
	 * to the DB all detail parameter use mode information, by calling the 
	 * method <code>loadModalitiesValueByID</code>.
	 *   
	 * @param key The choosed parameter use mode id key
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */
	private void getDetailModValue(String key, SourceBean response) throws EMFUserError {
		try {
			ModalitiesValue modVal = DAOFactory.getModalitiesValueDAO().loadModalitiesValueByID(new Integer(key));
			prepareDetailModalitiesValuePage(modVal, AdmintoolsConstants.DETAIL_MOD, response);
			session.setAttribute(SpagoBIConstants.LOV_MODIFIED, "false");
			session.setAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT, modVal);
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListLovsModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1019, new Vector(),
					params);

		}
	}
	
	private void testLovAfterAttributesFilling(SourceBean request, 	SourceBean response) throws EMFUserError, SourceBeanException  {
		try {
			ModalitiesValue modVal = null;
			modVal = (ModalitiesValue) session.getAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
			String lovProv = modVal.getLovProvider();
		  	ILovDetail lovDet = LovDetailFactory.getLovFromXML(lovProv);
			List profAttrToFill = getProfileAttributesToFill(lovDet);
			if(profAttrToFill.size()!=0) {
				//	create a fake user profile
				UserProfile currentUserProfile = (UserProfile) profile;
			    UserProfile userProfile = new UserProfile((String) currentUserProfile.getUserId(), currentUserProfile.getOrganization());
				// copy all the roles, functionalities of the original profile
			    userProfile.setFunctionalities(profile.getFunctionalities());
			    userProfile.setRoles(((UserProfile)profile).getRolesForUse());
			    userProfile.setDefaultRole(((UserProfile)profile).getDefaultRole());
			    
				// copy attributes and add the missing ones
				Map attributes = new HashMap();
				Collection origAttrNames = profile.getUserAttributeNames();
			    Iterator origAttrNamesIter = origAttrNames.iterator();
			    while(origAttrNamesIter.hasNext()) {
			    	String profileAttrName = (String)origAttrNamesIter.next();
			    	String profileAttrValue = profile.getUserAttribute(profileAttrName).toString();
			    	attributes.put(profileAttrName, profileAttrValue);
			    }
			    Iterator profAttrToFillIter = profAttrToFill.iterator();
			    while(profAttrToFillIter.hasNext()) {
			    	String profileAttrName = (String)profAttrToFillIter.next();
			    	String profileAttrValue = (String)request.getAttribute(profileAttrName);
			    	if(profileAttrValue!=null) {
			    		attributes.put(profileAttrName, profileAttrValue);
			    	}
			    }
			    userProfile.setAttributes(attributes);
			    session.setAttribute(SpagoBIConstants.USER_PROFILE_FOR_TEST, userProfile);
			}
			response.setAttribute("testLov", "true");
			return;
		} catch (Exception e) {
			logger.error("Error while creating user profile for test", e);
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
	private void modDetailModValue(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException {
		ModalitiesValue modVal = null;
		HashMap<String, String> logParam = new HashMap();
		
		try {
			modVal = (ModalitiesValue) session.getAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
			
			logParam.put("NAME",modVal.getName());
			logParam.put("TYPE",modVal.getITypeCd());			
			logParam.put("LABEL",modVal.getLabel());	
			
			
			
			// to rember that the lov has been modified 
			// necessary to show a confirm if the user change the lov and then go back without saving
			String lovProviderModified = (String) request.getAttribute("lovProviderModified");
			if(lovProviderModified != null && !lovProviderModified.trim().equals("")) 
				session.setAttribute(SpagoBIConstants.LOV_MODIFIED, lovProviderModified);
			
			// check if we are coming from the test
			String returnFromTestMsg = (String) request.getAttribute("RETURN_FROM_TEST_MSG");
			if(returnFromTestMsg!=null) {
				// save after the test
				if ("SAVE".equalsIgnoreCase(returnFromTestMsg)) {		
					
					// validate data
					ValidationCoordinator.validate("PAGE", "LovTestColumnSelector", this);
					// if there are some validation errors return to test page 
					Collection errors = errorHandler.getErrors();
					if (errors != null && errors.size() > 0) {
						Iterator iterator = errors.iterator();
						while (iterator.hasNext()) {
							Object error = iterator.next();
							if(error instanceof EMFValidationError) {
								response.setAttribute("testLov", "true");
								AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.ADD/MODIFY", logParam, "KO");
								return;
							}
						}
					}
					String valueColumn = (String)request.getAttribute("valueColumn");
					String descriptionColumn = (String)request.getAttribute("descriptionColumn");
					List visibleColumns = (List)request.getAttributeAsList("visibleColumn");
					List columns = (List)request.getAttributeAsList("column");
					
					String lovProvider = modVal.getLovProvider();
					ILovDetail lovDetail = LovDetailFactory.getLovFromXML(lovProvider);
					lovDetail.setDescriptionColumnName(descriptionColumn);
					List invisCols = getInvisibleColumns(columns, visibleColumns); 
					lovDetail.setInvisibleColumnNames(invisCols);
					lovDetail.setValueColumnName(valueColumn);
					lovDetail.setVisibleColumnNames(visibleColumns);
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
					prepareDetailModalitiesValuePage(modVal, mod, response);
					// exits without writing into DB and without loop
					return;
				}
			} 
			// if we are not coming from the test result page, the Lov objects fields are in request
			else {
				String idStr = (String) request.getAttribute("id");
				Integer id = new Integer(idStr);
				String description = (String) request.getAttribute("description");
				String name = (String) request.getAttribute("name");
				String label = (String) request.getAttribute("label");
				String input_type = (String) request.getAttribute("input_type");
				String input_type_cd = input_type.substring(0, input_type.indexOf(","));
				String input_type_id = input_type.substring(input_type.indexOf(",") + 1);
				if(mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
					modVal.setId(id);
				} 
				// check if lov type has been changed and in that case reset the lovprovider
				String oldTypeId = modVal.getITypeId();
				if((oldTypeId!=null) && (!oldTypeId.trim().equals(""))) {
					if(!oldTypeId.equals(input_type_id)) {
						modVal.setLovProvider("");
					}
				}
				// set the properties of the lov object
				modVal.setDescription(description);
				modVal.setName(name);
				modVal.setLabel(label);
				modVal.setITypeCd(input_type_cd);
				modVal.setITypeId(input_type_id);
                // check label and validation
				labelControl(request, mod);
				ValidationCoordinator.validate("PAGE", "ModalitiesValueValidation", this);
				// based on the type of lov set special properties
				Object objectToTest = null;
				
				
				if(input_type_cd.equalsIgnoreCase("QUERY")) {
					String lovProv = modVal.getLovProvider();
					QueryDetail query = null;
					if( (lovProv==null) || (lovProv.trim().equals("")) ) {
						query = new QueryDetail();
					} else {
						query = (QueryDetail)LovDetailFactory.getLovFromXML(lovProv);
					}
					recoverQueryWizardValues(request, query);
					String lovProvider = query.toXML();
					modVal.setLovProvider(lovProvider);
					ValidationCoordinator.validate("PAGE", "QueryWizardValidation", this);
					objectToTest = query;
					
					if (query!=null) logParam.put("QUERY",query.getQueryDefinition());
					
					
				} 
				
				else if (input_type_cd.equalsIgnoreCase("JAVA_CLASS")) {
					String lovProv = modVal.getLovProvider();
					JavaClassDetail javaClassDet =  null;
					if( (lovProv==null) || (lovProv.trim().equals("")) ) {
						javaClassDet = new JavaClassDetail();
					} else {
						javaClassDet = (JavaClassDetail)LovDetailFactory.getLovFromXML(lovProv);
					}
			        recoverJavaClassWizardValues(request, javaClassDet);
					String lovProvider = javaClassDet.toXML();
					modVal.setLovProvider(lovProvider);
					ValidationCoordinator.validate("PAGE", "JavaClassWizardValidation", this);
					objectToTest = javaClassDet;
					if (javaClassDet!=null) logParam.put("javaClassDet",javaClassDet.getJavaClassName());
				} 
				
				else if (input_type_cd.equalsIgnoreCase("SCRIPT")) {					
					String lovProv = modVal.getLovProvider();
					ScriptDetail scriptDet =  null;
					if( (lovProv==null) || (lovProv.trim().equals("")) ) {
						scriptDet = new ScriptDetail();
					} else {
						scriptDet = (ScriptDetail)LovDetailFactory.getLovFromXML(lovProv);
					}
					recoverScriptWizardValues(request, scriptDet);
					String lovProvider = scriptDet.toXML();
					modVal.setLovProvider(lovProvider);
					ValidationCoordinator.validate("PAGE", "ScriptWizardValidation", this);
					objectToTest = scriptDet;
					if (scriptDet!=null) logParam.put("scriptDet",scriptDet.getScript());
				} 
				
				else  if (input_type_cd.equalsIgnoreCase("FIX_LOV")) {
					String lovProv = modVal.getLovProvider();
					FixedListDetail fixlistDet = null;
					if( (lovProv==null) || (lovProv.trim().equals("")) ) {
						fixlistDet = new FixedListDetail();
					} else {
						fixlistDet = (FixedListDetail)LovDetailFactory.getLovFromXML(lovProv);
					}
					boolean itemTaskDone = doFixListItemTask(modVal, fixlistDet, request);
					if(itemTaskDone) {
						prepareDetailModalitiesValuePage(modVal, mod, response);
						session.setAttribute(SpagoBIConstants.LOV_MODIFIED, "true");
						session.setAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT, modVal);
						// exits without writing into DB and without loop
						return;
					} else {
						List items = fixlistDet.getItems();
						if(items.size()==0) {
							modVal.setLovProvider("<LOV/>");
						}
						objectToTest = fixlistDet;
					}

				}
				else if (input_type_cd.equalsIgnoreCase("DATASET")) {					
					String lovProv = modVal.getLovProvider();
					DatasetDetail datasetDet =  null;
					if( (lovProv==null) || (lovProv.trim().equals("")) ) {
						datasetDet = new DatasetDetail();
					} else {
						datasetDet = (DatasetDetail)LovDetailFactory.getLovFromXML(lovProv);
					}
					recoverDatasetWizardValues(request, datasetDet);
					String lovProvider = datasetDet.toXML();
					modVal.setLovProvider(lovProvider);
					ValidationCoordinator.validate("PAGE", "DatasetWizardValidation", this);
					objectToTest = datasetDet;
					if (datasetDet!=null) logParam.put("datasetDet",datasetDet.getDatasetLabel());
				} 
				
				
				
				// if there are some validation errors into the errorHandler does not write into DB
				Collection errors = errorHandler.getErrors();
				if (errors != null && errors.size() > 0) {
					Iterator iterator = errors.iterator();
					while (iterator.hasNext()) {
						Object error = iterator.next();
						if (error instanceof EMFValidationError) {
							prepareDetailModalitiesValuePage(modVal, mod, response);

							AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DOCUMENT.ADD/MODIFY", logParam, "KO");
							return;
						}
					}
				}
				// check if user wants to test
				Object test = request.getAttribute("testLovBeforeSave");
				if (test != null) {
					session.setAttribute(SpagoBIConstants.MODALITY, mod);
					session.setAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT, modVal);
					boolean needProfAttrFill = checkProfileAttributes(response, (ILovDetail)objectToTest);
					if(!needProfAttrFill) {
						response.setAttribute("testLov", "true");
					}
					// exits without writing into DB 
					return;
				}
			}
		
			
			
			// finally (if there are no error, if there is no request for test or to
			// add or delete a Fix Lov item) writes into DB
			if(mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
				IModalitiesValueDAO dao=DAOFactory.getModalitiesValueDAO();
				dao.setUserProfile(profile);
				dao.insertModalitiesValue(modVal);
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.ADD", logParam, "OK");
				
			} else {
				// looks for dependencies associated to the previous lov
				Integer lovId = modVal.getId();
				ModalitiesValue initialLov = DAOFactory.getModalitiesValueDAO().loadModalitiesValueByID(lovId);
				IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
				IParameterUseDAO paruseDAO = DAOFactory.getParameterUseDAO();
				List paruses = paruseDAO.getParameterUsesAssociatedToLov(lovId);
				Iterator parusesIt = paruses.iterator();
				List documents = new ArrayList();
				List correlations = new ArrayList();
				while (parusesIt.hasNext()) {
					ParameterUse aParuse = (ParameterUse) parusesIt.next();
					documents.addAll(objParuseDAO.getDocumentLabelsListWithAssociatedDependencies(aParuse.getUseID()));
					correlations.addAll(objParuseDAO.getAllDependenciesForParameterUse(aParuse.getUseID()));
				}
				// if the document list is not empty means that the lov is in correlation in some documents
				if (documents.size() > 0) {
					if (!initialLov.getITypeCd().equals(modVal.getITypeCd())) {
						// the lov type was changed
						HashMap errparams = new HashMap();
						errparams.put(AdmintoolsConstants.PAGE, "DetailModalitiesValuePage");
						List params = new ArrayList();
						params.add(documents.toString());
						EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "input_type", "1058", params, errparams);
						errorHandler.addError(error);
						prepareDetailModalitiesValuePage(modVal, mod, response);
						
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.MODIFY", logParam, "KO");
						return;
					} else {
						// the lov type was not changed, must verify that the dependency columns are still present
						// load all the columns returned by the lov
						String queryDetXML = modVal.getLovProvider();
						ILovDetail lovProvDet = LovDetailFactory.getLovFromXML(queryDetXML);
						List visColumns = lovProvDet.getVisibleColumnNames();
						List invisColumns = lovProvDet.getInvisibleColumnNames();
						List columns = new ArrayList();
						if( (visColumns!=null) && (visColumns.size()!=0) )
							columns.addAll(visColumns);
						if( (invisColumns!=null) && (invisColumns.size()!=0) )
							columns.addAll(invisColumns);
						// for each correlation column name chechs if the column is still present 
						Iterator correlationsIt = correlations.iterator();
						boolean columnNoMorePresent = false;
						List columnsNoMorePresent = new ArrayList();
						while (correlationsIt.hasNext()) {
							ObjParuse aObjParuse = (ObjParuse) correlationsIt.next();
							String filterColumn = aObjParuse.getFilterColumn();
							// because spago put all sourcebean attribute to Uppercase
							//filterColumn = filterColumn.toUpperCase();
							if (!columns.contains(filterColumn)) {
								columnNoMorePresent = true;
								columnsNoMorePresent.add(filterColumn);
							}
						}
						// if there are some column no more present then generate an error and return to the detail page
						if (columnNoMorePresent) {
							HashMap errparams = new HashMap();
							errparams.put(AdmintoolsConstants.PAGE, "DetailModalitiesValuePage");
							List params = new ArrayList();
							params.add(documents.toString());
							params.add(columnsNoMorePresent.toString());
							EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, 1059, params, errparams);
							errorHandler.addError(error);
							prepareDetailModalitiesValuePage(modVal, mod, response);
							

							AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.MODIFY", logParam, "KO");
							return;
						}
					}
				}
				IModalitiesValueDAO dao=DAOFactory.getModalitiesValueDAO();
				dao.setUserProfile(profile);
				dao.modifyModalitiesValue(modVal);
				
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.MODIFY", logParam, "OK");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		
		response.setAttribute("loopback", "true");
		session.delAttribute(SpagoBIConstants.LOV_MODIFIED);



		
	}
	
	
	private boolean checkProfileAttributes(SourceBean response, ILovDetail lovDet) {
		boolean needFill = false;
		try{
			List attrsToFill = getProfileAttributesToFill(lovDet);
			if(attrsToFill.size()!=0) {
				response.setAttribute(SpagoBIConstants.PROFILE_ATTRIBUTES_TO_FILL, attrsToFill);
				needFill = true;
			}
		} catch (Exception e) {
			logger.error("Error while checking the profile " + "attributes required for test", e);
		}
		return needFill;
	}
	
	
	private List getProfileAttributesToFill(ILovDetail lovDet) {
		List attrsToFill = new ArrayList();
		try{
			Collection userAttrNames = profile.getUserAttributeNames();
			List attrsRequired = lovDet.getProfileAttributeNames();
			Iterator attrsReqIter = attrsRequired.iterator();
			while(attrsReqIter.hasNext()) {
				String attrName = (String)attrsReqIter.next();
				if(!userAttrNames.contains(attrName)) {
					attrsToFill.add(attrName);
				}
			}
		} catch (Exception e) {
			logger.error("Error while checking the profile " + "attributes required for test", e);
		}
		return attrsToFill;
	}
	
	
	private boolean doFixListItemTask(ModalitiesValue modVal, FixedListDetail fixlistDet, SourceBean request) throws Exception {
		boolean changeItems = false;
		// checks if it is requested to delete a Fix Lov item
		Object indexOfFixedLovItemToDeleteObj = request.getAttribute("indexOfFixedLovItemToDelete");
		if (indexOfFixedLovItemToDeleteObj != null) {
			// it is requested to delete a Fix Lov item
			int indexOfFixedLovItemToDelete = new Integer((String)indexOfFixedLovItemToDeleteObj).intValue();
			fixlistDet = deleteFixLovValue(fixlistDet, indexOfFixedLovItemToDelete);
			changeItems = true;
		}
		// checks if it is requested to change a Fix Lov item
		Object indOfFixLovItemToChangeObj = request.getAttribute("indexOfFixedLovItemToChange");
		if (indOfFixLovItemToChangeObj != null) {
			// it is requested to change a Fix Lov item
			int indexOfFixedLovItemToChange = new Integer((String)indOfFixLovItemToChangeObj).intValue();
			String newValue = (String)request.getAttribute("nameRow"+indexOfFixedLovItemToChange+"InpText");
			String newName = (String)request.getAttribute("descrRow"+indexOfFixedLovItemToChange+"InpText");
			request.setAttribute("newNameRow", newName);
			request.setAttribute("newValueRow", newValue);
			ValidationCoordinator.validate("PAGE", "FixLovChangeValidation", this);
			if(errorHandler.isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
				fixlistDet = changeFixLovValue(fixlistDet, indexOfFixedLovItemToChange, newName, newValue);
				changeItems = true;
			}
		}
		//	checks if it is requested to move down a Fix Lov item
		Object indexOfItemToDown = request.getAttribute("indexOfItemToDown");
		if (indexOfItemToDown != null) {
			// it is requested to move down a Fix Lov item
			int indexOfItemToDownInt = new Integer((String)indexOfItemToDown).intValue();
			fixlistDet = moveDownFixLovItem(fixlistDet, indexOfItemToDownInt);
			changeItems = true;
		}
		// checks if it is requested to move up a Fix Lov item
		Object indexOfItemToUp = request.getAttribute("indexOfItemToUp");
		if (indexOfItemToUp != null) {
			// it is requested to move down a Fix Lov item
			int indexOfItemToUpInt = new Integer((String)indexOfItemToUp).intValue();
			fixlistDet = moveUpFixLovItem(fixlistDet, indexOfItemToUpInt);
			changeItems = true;
		}
		//	checks if it is requested to add a Fix Lov item
		Object insertFixLovItem = request.getAttribute("insertFixLovItem");
		if (insertFixLovItem != null) {
			ValidationCoordinator.validate("PAGE", "FixLovWizardValidation", this);
			if(errorHandler.isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
				fixlistDet = addFixLovItem(request, modVal);
				changeItems = true;
			}
		}
		// if the request was to insert/delete/modify item of the fix list update the lov
		if(changeItems) {
			String lovProvider = fixlistDet.toXML();
			modVal.setLovProvider(lovProvider);
		}
		return changeItems;
	}
	
	
	
	
	
	/**
	 * Sets some attributes into the response SourceBean. Those attributes are required for 
	 * the correct visualization of the ModalitiesValue form page.
	 * 
	 * @param modVal The ModalitiesValue to visualize 
	 * @param mod The modality (insert/modify)
	 * @param response The SourceBean to set
	 * @throws SourceBeanException
	 * @throws EMFUserError
	 * @throws EMFInternalError 
	 */

	private void prepareDetailModalitiesValuePage (ModalitiesValue modVal, String mod, SourceBean response) throws SourceBeanException, EMFUserError, EMFInternalError {
		response.setAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT, modVal);
		response.setAttribute(SpagoBIConstants.MODALITY, mod);	
		loadValuesDomain(response);
		loadAllProfileAttributes(response);
	}
	
	private void loadAllProfileAttributes(SourceBean response) throws SourceBeanException {
		ISecurityInfoProvider portalSecurityProvider = null;
		try {
			portalSecurityProvider = SecurityInfoProviderFactory.getPortalSecurityProvider();
		} catch (Exception e) {
			logger.error(" Error while istantiating portal security class", e);
			return;
		}
		List profileattrs = portalSecurityProvider.getAllProfileAttributesNames();
		response.setAttribute(SpagoBIConstants.PROFILE_ATTRS, profileattrs);
	}
	
	
	/**
	 * Deletes a value choosed by user from the LOV list.
	 * 
	 * @param request	The request SourceBean
	 * @param mod	A request string used to differentiate delete operation
	 * @param response	The response SourceBean
	 * @throws EMFUserError	If an Exception occurs
	 * @throws SourceBeanException If a SourceBean Exception occurs
	 */
	
	private void delDetailModValue(SourceBean request, String mod, SourceBean response)
		throws EMFUserError, SourceBeanException {
		HashMap<String, String> logParam = new HashMap();

		String idStr = (String) request.getAttribute("id");
		//controls if there is any parameter associated
		boolean hasPar = DAOFactory.getModalitiesValueDAO().hasParameters(idStr);
		IModalitiesValueDAO moddao = DAOFactory.getModalitiesValueDAO();
		ModalitiesValue modVal = moddao.loadModalitiesValueByID(new Integer(idStr));
		logParam.put("NAME",modVal.getName());
		logParam.put("TYPE",modVal.getITypeCd());
		try {
			moddao.eraseModalitiesValue(modVal);
			if (hasPar){

				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.DELETE", logParam, "KO");
				EMFUserError error = new EMFUserError (EMFErrorSeverity.ERROR, "1023", new Vector(), null);
				getErrorHandler().addError(error);
				return;
		    }
			
		} catch (Exception ex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.DELETE", logParam, "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.error("Cannot fill response container", ex  );
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListLovsModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1020, new Vector(), params);
			
		} finally {
			session.delAttribute(SpagoBIConstants.LOV_MODIFIED);
			session.delAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
		}
		response.setAttribute("afterDeleteLoop", "true");
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "LOV.DELETE", logParam, "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	/**
	 * Instantiates a new <code>Value<code> object when a new value 
	 * insertion in the LOV list is required, in order to prepare the page for the insertion.
	 * 
	 * @param response The response SourceBean
	 * @throws EMFUserError If an Exception occurred
	 */
	private void newDetailModValue(SourceBean response) throws EMFUserError {
		try {
			ModalitiesValue modVal = new ModalitiesValue();
			modVal.setId(new Integer(0));
			modVal.setName("");
			modVal.setDescription("");
			modVal.setLabel("");
			modVal.setLovProvider("");
			modVal.setITypeCd("QUERY");
			prepareDetailModalitiesValuePage(modVal, AdmintoolsConstants.DETAIL_INS, response);
			session.setAttribute(SpagoBIConstants.LOV_MODIFIED, "false");
			session.setAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT, modVal);
		} catch (Exception ex) {
			logger.error("Cannot prepare page for the insertion", ex);
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListLovsModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1021, new Vector(),
					params);

		}
	}
	
	
	/**
	 * Loads into the Response Source Bean all the Input Type Domain objects
	 * 
	 * @param response The response Source Bean
	 * @throws EMFUserError If any exception occurred
	 */
	private void loadValuesDomain(SourceBean response)  throws EMFUserError {
		try {
			List list = DAOFactory.getDomainDAO().loadListDomainsByType("INPUT_TYPE");
			response.setAttribute (SpagoBIConstants.LIST_INPUT_TYPE, list);
		} catch (Exception ex) {
			logger.error("Cannot prepare page for the insertion", ex  );
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1021);
		}
	}
	
	/**
	 * Recover all Java Class Wizard values when a value is inserted or modified, choosing "Java Class"
	 * as the input type. 
	 * 
	 * @param request The request SourceBean
	 */
	private void recoverJavaClassWizardValues (SourceBean request, JavaClassDetail jcd) {
		String javaClassName = (String) request.getAttribute("javaClassName");
		if (javaClassName == null) {
			javaClassName = "";
		}
		jcd.setJavaClassName(javaClassName);		
	}
	
	/**
	 * Recover all Query Wizard values when a value is inserted or modified, choosing "Query Statement"
	 * as the input type. 
	 * 
	 * @param request The request SourceBean
	 */
	private void recoverQueryWizardValues (SourceBean request, QueryDetail query) {
		//String connName = (String)request.getAttribute("connName");
		String datasource = (String)request.getAttribute("datasource");
		String queryDefinition = (String)request.getAttribute("queryDef");
		queryDefinition = "<![CDATA[" + queryDefinition + "]]>";
		//query.setConnectionName(connName);
		query.setDataSource(datasource);
		query.setQueryDefinition(queryDefinition);
	}
	
	/**
	 * Recover all Script Wizard values when a value is inserted or modified, choosing "Script to Load Values"
	 * as the input type.
	 * 
	 * @param request The request SourceBean
	 */
	
	private void recoverScriptWizardValues (SourceBean request, ScriptDetail sdet) {
			String script = (String) request.getAttribute("script");
			if(script==null) {
				script = "";
			}
			String languageScript = (String) request.getAttribute("LANGUAGESCRIPT");
			if(languageScript==null) {
				languageScript = "";
			}
			// TODO controllare se si possono togliere i caratteri di escape
			//script = script.replaceAll(">", "&gt;");
			//script = script.replaceAll("<", "&lt;");
			//script = script.replaceAll("\"", "&quot;");
			script = "<![CDATA[" + script + "]]>";
			sdet.setScript(script);	   
			sdet.setLanguageScript(languageScript);
	}
	
	/**
	 * Recover all Dataset Wizard values when a value is inserted or modified, choosing "Dataset"
	 * as the input type. 
	 * 
	 * @param request The request SourceBean
	 */
	private void recoverDatasetWizardValues (SourceBean request, DatasetDetail dataset) {
		String datasetId = (String)request.getAttribute("dataset");
		String datasetLabel = (String)request.getAttribute("datasetReadLabel");
		dataset.setDatasetId(datasetId);
		dataset.setDatasetLabel(datasetLabel);
	}
	
	
	/**
	 * Inserts a new Fixed LOV item in the FixedLov Wizard. When this type of Input is selected dring the insertion/
	 * modify of a Value in the LOV list, it is possible to add a series of FixLov Values, showed
	 * at runtime in a table.
	 * 
	 * @param request	The request SourceBean
	 * @param modVal	The ModalitiesValue to modify with the new entry
	 * @throws SourceBeanException	If a SourceBean Exception occurred
	 */
	private FixedListDetail addFixLovItem (SourceBean request, ModalitiesValue modVal) throws SourceBeanException {
		String lovProv = modVal.getLovProvider();
		FixedListDetail lovDetList = null;
		if ((lovProv==null) || (lovProv.trim().equals("")) || (!modVal.getITypeCd().equals("FIX_LOV"))) {
			lovDetList = new FixedListDetail();
		} else {
			lovDetList = FixedListDetail.fromXML(lovProv);
		}
		String lovDesc = (String)request.getAttribute("nameOfFixedLovItemNew");
		String lovValue = (String)request.getAttribute("valueOfFixedLovItemNew");
		lovDetList.add(lovValue, lovDesc);
		return lovDetList;
	}

	
	
	/**
	 * Delete from the list of fix lov items the one at index indexOfFixedListItemToDelete
	 * @param lovDetList	The list of Fix Lov
	 * @param indexOfFixedListItemToDelete	The index of the item to be deleted
    */
	private FixedListDetail deleteFixLovValue (FixedListDetail lovDetList, int indexOfFixedLovItemToDelete)  {
		List lovs = lovDetList.getItems();
		lovs.remove(indexOfFixedLovItemToDelete);
		lovDetList.setLovs(lovs);
		return lovDetList;
	}
	
	/**
	 * Move up an item of the fix lov list
	 * @param lovDetList	The list of Fix Lov
	 * @param indexOfItemToUp	The index of the item to move up
    */
	private FixedListDetail moveUpFixLovItem (FixedListDetail lovDetList, int indexOfItemToUp)  {
		List lovs = lovDetList.getItems();
		Object o = lovs.get(indexOfItemToUp);
		lovs.remove(indexOfItemToUp);
		lovs.add((indexOfItemToUp-1), o);
		lovDetList.setLovs(lovs);
		return lovDetList;
	}
	
	/**
	 * Move down an item of the fix lov list
	 * @param lovDetList	The list of Fix Lov
	 * @param indexOfItemToDown	The index of the item to move down
    */
	private FixedListDetail moveDownFixLovItem (FixedListDetail lovDetList, int indexOfItemToDown)  {
		List lovs = lovDetList.getItems();
		Object o = lovs.get(indexOfItemToDown);
		lovs.remove(indexOfItemToDown);
		lovs.add((indexOfItemToDown+1), o);
		lovDetList.setLovs(lovs);
		return lovDetList;
	}
					
	/**
	 * Chnage from the list of fix lov items the one at index indexOfFixedListItemToDelete
	 * @param lovDetList	The list of Fix Lov
	 * @param itemToChange	The index of the item to be changed
	 * @param newName the new name of the item
	 * @param newValue the new value of the item
    */
	private FixedListDetail changeFixLovValue (FixedListDetail lovDetList, int itemToChange, 
			                                   String newName, String newValue)  {
		List lovs = lovDetList.getItems();
		lovs.remove(itemToChange);
		FixedListItemDetail lovdet = new FixedListItemDetail();
		lovdet.setValue(newValue);
		lovdet.setDescription(newName);
		lovs.add(itemToChange, lovdet);
		lovDetList.setLovs(lovs);
		return lovDetList;
	}		
			
			
	/**
	 * Controls if the label choosed by user is yet in use.
	 * If it is, an error is added to the error handler.
	 * 
	 * @param request The request Source Bean
	 * @param mod The modality
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void labelControl (SourceBean request, String mod) throws EMFUserError {
		String label = (String) request.getAttribute("label");
		List allModVal = DAOFactory.getModalitiesValueDAO()
				.loadAllModalitiesValue();
		if (AdmintoolsConstants.DETAIL_INS.equalsIgnoreCase(mod)) {
			Iterator i = allModVal.iterator();
			while (i.hasNext()) {
				ModalitiesValue value = (ModalitiesValue) i.next();
				String valueLabel = value.getLabel();
				if (valueLabel.equals(label)) {
					HashMap params = new HashMap();
					params.put(AdmintoolsConstants.PAGE,
							ListLovsModule.MODULE_PAGE);
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "label", "1024",
							new Vector(), params);
					errorHandler.addError(error);

				}
			}
		} else {
			String currentId = (String) request.getAttribute("id");
			Iterator i = allModVal.iterator();
			while (i.hasNext()) {
				ModalitiesValue value = (ModalitiesValue) i.next();
				String valueLabel = value.getLabel();
				String valueId = value.getId().toString();
				if (valueLabel.equals(label)
						&& (!currentId.equalsIgnoreCase(valueId))) {
					HashMap params = new HashMap();
					params.put(AdmintoolsConstants.PAGE,
							ListLovsModule.MODULE_PAGE);
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "label", "1024",
							new Vector(), params);
					errorHandler.addError(error);

				}
			}
		}
	}
	
	
	private List getInvisibleColumns(List columns, List visibleColumns) {
		List invisibleCols = new ArrayList();
		Iterator iterCols = columns.iterator();
		while(iterCols.hasNext()){
			String colName = (String)iterCols.next();
			if(!visibleColumns.contains(colName)) {
				invisibleCols.add(colName);
			}
		}
		return invisibleCols;
	}
	
}


