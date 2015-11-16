/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.datasource.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This class implements a module which  handles data source management. 
 */
public class DetailDataSourceModule extends AbstractHttpModule{
	static private Logger logger = Logger.getLogger(DetailDataSourceModule.class);
	public static final String MOD_SAVE = "SAVE";
	public static final String MOD_SAVEBACK = "SAVEBACK";
	public final static String NAME_ATTR_LIST_DIALECTS = "dialects";

	private String modalita = "";
	private IEngUserProfile profile;
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
	}

	/**
	 * Reads the operation asked by the user and calls the insertion, updation or deletion methods.
	 * 
	 * @param request The Source Bean containing all request parameters
	 * @param response The Source Bean containing all response parameters
	 * 
	 * @throws exception If an exception occurs
	 * @throws Exception the exception
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		String message = (String) request.getAttribute("MESSAGEDET");
		logger.debug("begin of detail Data Source service with message =" +message);
		RequestContainer requestContainer = this.getRequestContainer();	
		ResponseContainer responseContainer = this.getResponseContainer();	
		SessionContainer session = requestContainer.getSessionContainer();
		SessionContainer permanentSession = session.getPermanentContainer();
		profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		
		EMFErrorHandler errorHandler = getErrorHandler();
		try {
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				logger.debug("The message parameter is null");
				throw userError;
			}
			logger.debug("The message parameter is: " + message.trim());
			if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_SELECT)) {
				getDataSource(request, response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_MOD)) {
				modifyDataSource(request, SpagoBIConstants.DETAIL_MOD, response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_NEW)) {
				newDataSource(response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {
				modifyDataSource(request, SpagoBIConstants.DETAIL_INS, response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_DEL)) {
				deleteDataSource(request, SpagoBIConstants.DETAIL_DEL, response);
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
	
	 
	
	/**
	 * Gets the detail of a data source choosed by the user from the 
	 * data sources list. It reaches the key from the request and asks to the DB all detail
	 * data source information, by calling the method <code>loadDataSourceByID</code>.
	 *   
	 * @param key The choosed data source id key
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   
	private void getDataSource(SourceBean request, SourceBean response) throws EMFUserError {		
		DataSource ds = DAOFactory.getDataSourceDAO().loadDataSourceByID(new Integer((String)request.getAttribute("ID")));
		HashMap<String, String> logParam = new HashMap();
		logParam.put("TYPE",ds.getJndi());
		try {		 											
			this.modalita = SpagoBIConstants.DETAIL_MOD;
			if (request.getAttribute("SUBMESSAGEDET") != null &&
				((String)request.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK))
			{
				response.setAttribute("loopback", "true");
				return;
			}
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dialects = domaindao.loadListDomainsByType("DIALECT_HIB");
			response.setAttribute(NAME_ATTR_LIST_DIALECTS, dialects);
			response.setAttribute("modality", modalita);
			response.setAttribute("dsObj", ds);			
		} catch (Exception ex) {		
			if (this.modalita.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.ADD", logParam, "KO");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.MODIFY", logParam, "KO");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			
			logger.error("Cannot fill response container" + ex.getLocalizedMessage());	
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListDataSourceModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 8003, new Vector(), params);
		}
		
	}
	 /**
	 * Inserts/Modifies the detail of a data source according to the user request. 
	 * When a data source is modified, the <code>modifyDataSource</code> method is called; when a new
	 * data source is added, the <code>insertDataSource</code>method is called. These two cases are 
	 * differentiated by the <code>mod</code> String input value .
	 * 
	 * @param request The request information contained in a SourceBean Object
	 * @param mod A request string used to differentiate insert/modify operations
	 * @param response The response SourceBean 
	 * @throws EMFUserError If an exception occurs
	 * @throws SourceBeanException If a SourceBean exception occurs
	 */
	private void modifyDataSource(SourceBean serviceRequest, String mod, SourceBean serviceResponse) throws EMFUserError, SourceBeanException {
		
		
		try {
			IDataSourceDAO dao=DAOFactory.getDataSourceDAO();
			dao.setUserProfile(profile);
			DataSource dsNew = recoverDataSourceDetails(serviceRequest);
			EMFErrorHandler errorHandler = getErrorHandler();
			HashMap<String, String> logParam = new HashMap();
			logParam.put("JNDI",dsNew.getJndi());
			logParam.put("NAME",dsNew.getLabel());
			logParam.put("URL",dsNew.getUrlConnection());

			// if there are some validation errors into the errorHandler does not write into DB
			Collection errors = errorHandler.getErrors();
			if (errors != null && errors.size() > 0) {
				Iterator iterator = errors.iterator();
				while (iterator.hasNext()) {
					Object error = iterator.next();
					if (error instanceof EMFValidationError) {
						serviceResponse.setAttribute("dsObj", dsNew);
						serviceResponse.setAttribute("modality", mod);
						if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
							try {
								AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.ADD", logParam, "KO");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							try {
								AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.MODIFY", logParam, "KO");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}						
						return;
					}
				}
			}
			
			if (mod.equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {			
				//if a ds with the same label not exists on db ok else error
				if (DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dsNew.getLabel()) != null){
					HashMap params = new HashMap();
					params.put(AdmintoolsConstants.PAGE, ListDataSourceModule.MODULE_PAGE);
					EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 8004, new Vector(), params );
					getErrorHandler().addError(error);
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.ADD", logParam, "KO");
					return;
				}	 		

				dao.insertDataSource(dsNew, ((UserProfile)profile).getOrganization());
				
				IDataSource tmpDS = dao.loadDataSourceByLabel(dsNew.getLabel());
				dsNew.setDsId(tmpDS.getDsId());
				mod = SpagoBIConstants.DETAIL_MOD; 
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.ADD", logParam, "OK");
			} else {				
				//update ds
				dao.modifyDataSource(dsNew);
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.MODIFY", logParam, "OK");
			}  
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dialects = domaindao.loadListDomainsByType("DIALECT_HIB");
			serviceResponse.setAttribute(NAME_ATTR_LIST_DIALECTS, dialects);
			
			if (serviceRequest.getAttribute("SUBMESSAGEDET") != null && 
				((String)serviceRequest.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVE)) {	
				serviceResponse.setAttribute("modality", mod);
				serviceResponse.setAttribute("dsObj", dsNew);				
				return;
			}
			else if (serviceRequest.getAttribute("SUBMESSAGEDET") != null && 
					((String)serviceRequest.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK)){
					serviceResponse.setAttribute("loopback", "true");
				    return;
			}
	
		} catch (EMFUserError e){
			logger.error("Cannot fill response container" + e.getLocalizedMessage());
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListDataSourceModule.MODULE_PAGE);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.MODIFY",null, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 8005, new Vector(), params);
			
		}
		
		catch (Exception ex) {		
			logger.error("Cannot fill response container" , ex);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.MODIFY", null, "KO");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}		

	}

	/**
	 * Deletes a data source choosed by user from the data sources list.
	 * 
	 * @param request	The request SourceBean
	 * @param mod	A request string used to differentiate delete operation
	 * @param response	The response SourceBean
	 * @throws EMFUserError	If an Exception occurs
	 * @throws SourceBeanException If a SourceBean Exception occurs
	 */
	private void deleteDataSource(SourceBean request, String mod, SourceBean response)
		throws EMFUserError, SourceBeanException {
		HashMap<String, String> logParam = new HashMap();
		try {
			String id = (String) request.getAttribute("ID");
//			if the ds is associated with any BIEngine or BIObjects, creates an error
			boolean bObjects =  DAOFactory.getDataSourceDAO().hasBIObjAssociated(id);
			//boolean bEngines =  DAOFactory.getDataSourceDAO().hasBIEngineAssociated(id);
			if (bObjects){
				HashMap params = new HashMap();
				params.put(AdmintoolsConstants.PAGE, ListDataSourceModule.MODULE_PAGE);
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 8007, new Vector(), params );
				getErrorHandler().addError(error);
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.DELETE", null, "ERR");
				return;
			}
			
			//delete the ds
			IDataSource ds = DAOFactory.getDataSourceDAO().loadDataSourceByID(new Integer(id));
			DAOFactory.getDataSourceDAO().eraseDataSource(ds);
			
			logParam.put("TYPE",ds.getJndi());
			logParam.put("NAME",ds.getLabel());	
		}
		catch (EMFUserError e){
			  try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.DELETE", null, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			  logger.error("Cannot fill response container" + e.getLocalizedMessage());
			  HashMap params = new HashMap();		  
			  params.put(AdmintoolsConstants.PAGE, ListDataSourceModule.MODULE_PAGE);
			  throw new EMFUserError(EMFErrorSeverity.ERROR, 8006, new Vector(), params);
				
		}
	    catch (Exception ex) {		
		    ex.printStackTrace();
			logger.error("Cannot fill response container" ,ex);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.DELETE", null, "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	    }
	    try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "DATA_SOURCE.DELETE", logParam, "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    response.setAttribute("loopback", "true");			
	}



	/**
	 * Instantiates a new <code>datasource<code> object when a new data source insertion is required, in order
	 * to prepare the page for the insertion.
	 * 
	 * @param response The response SourceBean
	 * @throws EMFUserError If an Exception occurred
	 */

	private void newDataSource(SourceBean response) throws EMFUserError {
		
		try {
			
			DataSource ds = null;
			this.modalita = SpagoBIConstants.DETAIL_INS;
			response.setAttribute("modality", modalita);
			ds = new DataSource();
			ds.setDsId(-1);
			ds.setDescr("");
			ds.setDialectId(new Integer("-1"));
			ds.setLabel("");
			ds.setJndi("");
			ds.setUrlConnection("");
			ds.setUser("");
			ds.setPwd("");
			ds.setDriver("");
			ds.setMultiSchema(false);
			ds.setSchemaAttribute("");
			response.setAttribute("dsObj", ds);
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dialects = domaindao.loadListDomainsByType("DIALECT_HIB");
			response.setAttribute(NAME_ATTR_LIST_DIALECTS, dialects);
		} catch (Exception ex) {
			logger.error("Cannot prepare page for the insertion" , ex);		
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		
	}


	private DataSource recoverDataSourceDetails (SourceBean serviceRequest) throws EMFUserError, SourceBeanException, IOException  {
		DataSource ds  = new DataSource();
		
		String idStr = (String)serviceRequest.getAttribute("ID");
		Integer id = new Integer(idStr);
		Integer dialectId = Integer.valueOf((String)serviceRequest.getAttribute("DIALECT"));	
		String description = (String)serviceRequest.getAttribute("DESCR");	
		String label = (String)serviceRequest.getAttribute("LABEL");
		String jndi = (String)serviceRequest.getAttribute("JNDI");
		String url = (String)serviceRequest.getAttribute("URL_CONNECTION");
		String user = (String)serviceRequest.getAttribute("USER");
		String pwd = (String)serviceRequest.getAttribute("PWD");
		String driver = (String)serviceRequest.getAttribute("DRIVER");
		String schemaAttr = (String)serviceRequest.getAttribute("ATTRSCHEMA");
		String multiSchema = (String)serviceRequest.getAttribute("MULTISCHEMA");
		Boolean isMultiSchema = false;
		if(multiSchema!=null && multiSchema.equals("YES")){
			isMultiSchema = true;
		}
		
		ds.setDsId(id.intValue());
		ds.setDialectId(dialectId);
		ds.setLabel(label);
		ds.setDescr(description);
		ds.setJndi(jndi);
		ds.setUrlConnection(url);
		ds.setUser(user);
		ds.setPwd(pwd);
		ds.setDriver(driver);
		ds.setSchemaAttribute(schemaAttr);
		ds.setMultiSchema(isMultiSchema);
				
		return ds;
	}

}