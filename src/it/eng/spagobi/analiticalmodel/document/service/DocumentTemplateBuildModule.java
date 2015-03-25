/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Permits the dynamic creation or modification of a document template.
 */
public class DocumentTemplateBuildModule extends AbstractModule {
	private static transient Logger logger = Logger.getLogger(DocumentTemplateBuildModule.class);
	protected EMFErrorHandler errorHandler = null;
	protected RequestContainer requestContainer = null;
	protected SessionContainer session = null;
	protected SessionContainer permanentSession = null;
	private IEngUserProfile profile;
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {}
	
	
	/**
	 * Manage all the request in order to exec all the different BIObject template creation phases.
	 * 
	 * @param request The request source bean
	 * @param response 	The response Source bean
	 * 
	 * @throws Exception If an Exception occurred
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		debug("service", "start service method");
		String messageExec = (String) request.getAttribute(SpagoBIConstants.MESSAGEDET);
		debug("service", "using message" + messageExec);
		errorHandler = getErrorHandler();
		requestContainer = this.getRequestContainer();
		session = requestContainer.getSessionContainer();
		permanentSession = session.getPermanentContainer();
		debug("service", "errorHanlder, requestContainer, session, permanentSession retrived ");
        profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
        
		try {
			if(messageExec == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				logger.fatal("The execution-message parameter is null");
				throw userError;
			}
			
			if (messageExec.equalsIgnoreCase(SpagoBIConstants.NEW_DOCUMENT_TEMPLATE))  {
				newDocumentTemplateHandler(request, response);
			} else if(messageExec.equalsIgnoreCase(SpagoBIConstants.EDIT_DOCUMENT_TEMPLATE)) {
				editDocumentTemplateHandler(request, response);
			} else {	
		   	    logger.error("Illegal request of service");
		   		errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 102)); 
		   	}
	    } catch (EMFUserError e) { 
	    	errorHandler.addError(e); 
	    }
    }

	private void newDocumentTemplateHandler(SourceBean request, SourceBean response) throws Exception {
		debug("newDocumentTemplateHandler", "start method");
		// get the id of the object
		String idStr = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_ID);
		Integer biObjectID = new Integer(idStr);
		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(biObjectID);
		Engine engine = obj.getEngine();
		
		// GET THE TYPE OF ENGINE (INTERNAL / EXTERNAL) AND THE SUITABLE BIOBJECT TYPES
		Domain engineType = null;
		Domain compatibleBiobjType = null;
		engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
		compatibleBiobjType = DAOFactory.getDomainDAO().loadDomainById(engine.getBiobjTypeId());
		String compatibleBiobjTypeCd = compatibleBiobjType.getValueCd();
		String biobjTypeCd = obj.getBiObjectTypeCode();
		
		// CHECK IF THE BIOBJECT IS COMPATIBLE WITH THE TYPES SUITABLE FOR THE ENGINE
		if (!compatibleBiobjTypeCd.equalsIgnoreCase(biobjTypeCd)) {
			// the engine document type and the biobject type are not compatible
			 logger.fatal("Engine cannot execute input document type: " +
		 				  "the engine " + engine.getName() + " can execute '" + compatibleBiobjTypeCd + "' type documents " +
		 				  "while the input document is a '" + biobjTypeCd + "'.");
			Vector params = new Vector();
			params.add(engine.getName());
			params.add(compatibleBiobjTypeCd);
			params.add(biobjTypeCd);
			errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 2002, params));
			return;
		}
				
		// IF THE ENGINE IS EXTERNAL
		if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
			try {
				// instance the driver class
				String driverClassName = obj.getEngine().getDriverName();
				IEngineDriver aEngineDriver = (IEngineDriver)Class.forName(driverClassName).newInstance();
				EngineURL templateBuildUrl = null;
				try {
					templateBuildUrl = aEngineDriver.getNewDocumentTemplateBuildUrl(obj, profile);
				} catch (InvalidOperationRequest ior) {
					logger.info("Engine " + engine.getName() + " cannot build document template");
					Vector params = new Vector();
					params.add(engine.getName());
					errorHandler.addError(new EMFUserError(EMFErrorSeverity.INFORMATION, "1076", params));
					response.setAttribute(ObjectsTreeConstants.OBJECT_ID, idStr);
					response.setAttribute("biobject", obj);
					return;
				}
			    // set into the reponse the url to be invoked	
				response.setAttribute(ObjectsTreeConstants.CALL_URL, templateBuildUrl);
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "TemplateBuildPublisher");
				response.setAttribute("biobject", obj);				
				response.setAttribute("operation", "newDocumentTemplate");
				
			} catch (Exception e) {
				 logger.fatal("Error retrieving template build url", e);
			   	 errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 100)); 
			}	
			
		// IF THE ENGINE IS INTERNAL	
		} else {
			
			String className = engine.getClassName();
			debug("execute", "Try instantiating class " + className + " for internal engine " + engine.getName() + "...");
			InternalEngineIFace internalEngine = null;
			// tries to instantiate the class for the internal engine
			try {
				if (className == null && className.trim().equals("")) throw new ClassNotFoundException();
				internalEngine = (InternalEngineIFace) Class.forName(className).newInstance();
			} catch (ClassNotFoundException cnfe) {
				logger.fatal("The class ['" + className + "'] for internal engine " + engine.getName() + " was not found.", cnfe);
				Vector params = new Vector();
				params.add(className);
				params.add(engine.getName());
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 2001, params));
				return;
			} catch (Exception e) {
				logger.fatal("Error while instantiating class " + className, e);
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 100));
				return;
			}
			
			debug("execute", "Class " + className + " instantiated successfully. Now engine's execution starts.");
			
			// starts engine's execution
			try {
				internalEngine.handleNewDocumentTemplateCreation(requestContainer, obj, response);
			} catch (InvalidOperationRequest ior) {
				logger.info("Engine " + engine.getName() + " cannot build document template");
				Vector params = new Vector();
				params.add(engine.getName());
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.INFORMATION, "1076", params));
				response.setAttribute(ObjectsTreeConstants.OBJECT_ID, idStr);
				return;
			} catch (Exception e) {
				logger.fatal("Error while engine execution", e);
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 100));
			}
		}
		debug("newDocumentTemplateHandler", "end method");
	}
	
	private void editDocumentTemplateHandler(SourceBean request, SourceBean response) throws Exception {
		debug("editDocumentTemplateHandler", "start method");
		// get the id of the object
		String idStr = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_ID);
		Integer biObjectID = new Integer(idStr);
		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectForDetail(biObjectID);
		Engine engine = obj.getEngine();
		
		// GET THE TYPE OF ENGINE (INTERNAL / EXTERNAL) AND THE SUITABLE BIOBJECT TYPES
		Domain engineType = null;
		Domain compatibleBiobjType = null;
		engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
		compatibleBiobjType = DAOFactory.getDomainDAO().loadDomainById(engine.getBiobjTypeId());
		String compatibleBiobjTypeCd = compatibleBiobjType.getValueCd();
		String biobjTypeCd = obj.getBiObjectTypeCode();
		
		// CHECK IF THE BIOBJECT IS COMPATIBLE WITH THE TYPES SUITABLE FOR THE ENGINE
		if (!compatibleBiobjTypeCd.equalsIgnoreCase(biobjTypeCd)) {
			// the engine document type and the biobject type are not compatible
			 logger.fatal("Engine cannot execute input document type: " +
		 				  "the engine " + engine.getName() + " can execute '" + compatibleBiobjTypeCd + "' type documents " +
		 				  "while the input document is a '" + biobjTypeCd + "'.");
			Vector params = new Vector();
			params.add(engine.getName());
			params.add(compatibleBiobjTypeCd);
			params.add(biobjTypeCd);
			errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 2002, params));
			return;
		}
		
		// IF THE ENGINE IS EXTERNAL
		if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
			try {
				// instance the driver class
				String driverClassName = obj.getEngine().getDriverName();
				IEngineDriver aEngineDriver = (IEngineDriver)Class.forName(driverClassName).newInstance();
				EngineURL templateBuildUrl = null;
				try {
					templateBuildUrl = aEngineDriver.getEditDocumentTemplateBuildUrl(obj, profile);
				} catch (InvalidOperationRequest ior) {
					logger.info("Engine " + engine.getName() + " cannot build document template");
					Vector params = new Vector();
					params.add(engine.getName());
					errorHandler.addError(new EMFUserError(EMFErrorSeverity.INFORMATION, "1076", params));
					response.setAttribute(ObjectsTreeConstants.OBJECT_ID, idStr);
					return;
				}
			    // set into the reponse the url to be invoked	
				response.setAttribute(ObjectsTreeConstants.CALL_URL, templateBuildUrl);
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "TemplateBuildPublisher");
				response.setAttribute("biobject", obj);
				response.setAttribute("operation", "newDocumentTemplate");
				
			} catch (Exception e) {
				 logger.fatal("Error retrieving template build url", e);
			   	 errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 100)); 
			}	
			
		// IF THE ENGINE IS INTERNAL	
		} else {
			
			String className = engine.getClassName();
			debug("execute", "Try instantiating class " + className + " for internal engine " + engine.getName() + "...");
			InternalEngineIFace internalEngine = null;
			// tries to instantiate the class for the internal engine
			try {
				if (className == null && className.trim().equals("")) throw new ClassNotFoundException();
				internalEngine = (InternalEngineIFace) Class.forName(className).newInstance();
			} catch (ClassNotFoundException cnfe) {
				logger.fatal("The class ['" + className + "'] for internal engine " + engine.getName() + " was not found.", cnfe);
				Vector params = new Vector();
				params.add(className);
				params.add(engine.getName());
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 2001, params));
				return;
			} catch (Exception e) {
				logger.fatal("Error while instantiating class " + className, e);
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 100));
				return;
			}
			
			debug("execute", "Class " + className + " instantiated successfully. Now engine's execution starts.");
			
			// starts engine's execution
			try {
				internalEngine.handleDocumentTemplateEdit(requestContainer, obj, response);
			} catch (InvalidOperationRequest ior) {
				logger.info("Engine " + engine.getName() + " cannot build document template");
				Vector params = new Vector();
				params.add(engine.getName());
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.INFORMATION, "1076", params));
				response.setAttribute(ObjectsTreeConstants.OBJECT_ID, idStr);
				return;
			} catch (Exception e) {
				logger.fatal("Error while engine execution", e);
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR, 100));
			}
		}
		debug("editDocumentTemplateHandler", "end method");
	}

	/**
	 * Trace a debug message into the log
	 * @param method Name of the method to store into the log
	 * @param message Message to store into the log
	 */
	private void debug(String method, String message) {
		logger.debug(message);
	}
	
}
