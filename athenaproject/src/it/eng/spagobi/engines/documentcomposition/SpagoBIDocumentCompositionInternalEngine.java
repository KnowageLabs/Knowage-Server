/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.documentcomposition;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.ContextManager;
import it.eng.spagobi.container.SpagoBISessionContainer;
import it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */

public class SpagoBIDocumentCompositionInternalEngine implements InternalEngineIFace {

	public static final String messageBundle = "MessageFiles.component_spagobidocumentcompositionIE_messages";
	
	private static transient Logger logger=Logger.getLogger(SpagoBIDocumentCompositionInternalEngine.class);
	
	/**
	 * Executes the document and populates the response.
	 * 
	 * @param requestContainer The <code>RequestContainer</code> object (the session can be retrieved from this object)
	 * @param obj The <code>BIObject</code> representing the document to be executed
	 * @param response The response <code>SourceBean</code> to be populated
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void execute(RequestContainer requestContainer, BIObject obj,
			SourceBean response) throws EMFUserError {
		
		logger.debug("IN");
		Monitor monitor =MonitorFactory.start("spagobi.engines.SpagoBIDocumentCompositionInternalEngine.execute");
		if (obj == null) {
			logger.error("The input object is null");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		}

		if (!obj.getBiObjectTypeCode().equalsIgnoreCase("DOCUMENT_COMPOSITE") &&
			!obj.getBiObjectTypeCode().equalsIgnoreCase("COMPOSITE_DOCUMENT")) {
			logger.error("The input object is not a composite document");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1001", messageBundle);
		}
		
		try {
			byte[] contentBytes = null;
			try{
				ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(obj.getId());
	            if(template==null) throw new Exception("Active Template null");
	            contentBytes = template.getContent();
	            if(contentBytes==null) throw new Exception("Content of the Active template null");
			} catch (Exception e) {
				logger.error("Error while recovering template content: \n" + e);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1002", messageBundle);
			}
			// get bytes of template and transform them into a SourceBean
			SourceBean content = null;
			try {
				String contentStr = new String(contentBytes);
				content = SourceBean.fromXMLString(contentStr);
			} catch (Exception e) {
				logger.error("Error while converting the Template bytes into a SourceBean object: ", e);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1011", messageBundle);
			}
			
			// read the configuration and set relative object into session
			DocumentCompositionConfiguration docConf = new DocumentCompositionConfiguration(content);
			SessionContainer session = requestContainer.getSessionContainer();
			ContextManager contextManager = new ContextManager(new SpagoBISessionContainer(session), 
					new LightNavigatorContextRetrieverStrategy(requestContainer.getServiceRequest()));
			contextManager.set("docConfig", docConf);
	       
			// set information into response
			response.setAttribute(ObjectsTreeConstants.SESSION_OBJ_ATTR, obj);
			response.setAttribute(content);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "DOCUMENT_COMPOSITION");
		} catch (EMFUserError ue) {
			logger.error("Cannot exec the composite document", ue);
			throw new EMFUserError(EMFErrorSeverity.ERROR, ue.getErrorCode(), messageBundle);
		} catch (Exception e) {
			logger.error("Cannot exec the composite document", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		}finally{
			monitor.stop();
		}
	}

	/**
	 * The <code>SpagoBIOfficeDocumentInternalEngine</code> cannot manage subobjects so this method must not be invoked.
	 * 
	 * @param requestContainer The <code>RequestContainer</code> object (the session can be retrieved from this object)
	 * @param obj The <code>BIObject</code> representing the document
	 * @param response The response <code>SourceBean</code> to be populated
	 * @param subObjectInfo An object describing the subobject to be executed
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void executeSubObject(RequestContainer requestContainer,
			BIObject obj, SourceBean response, Object subObjectInfo)
			throws EMFUserError {
		// it cannot be invoked
		logger.error("SpagoBIOfficeDocumentInternalEngine cannot exec subobjects");

		throw new EMFUserError(EMFErrorSeverity.ERROR, "101", messageBundle);
	}

	/**
	 * Function not implemented. Thid method should not be called
	 * 
	 * @param requestContainer The <code>RequestContainer</code> object (the session can be retrieved from this object)
	 * @param response The response <code>SourceBean</code> to be populated
	 * @param obj the obj
	 * 
	 * @throws InvalidOperationRequest the invalid operation request
	 * @throws EMFUserError the EMF user error
	 */
	public void handleNewDocumentTemplateCreation(RequestContainer requestContainer, 
			BIObject obj, SourceBean response) throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDocumentCompositionInternalEngine cannot build document template");
		throw new InvalidOperationRequest();
		
	}

	/**
	 * Function not implemented. Thid method should not be called
	 * 
	 * @param requestContainer The <code>RequestContainer</code> object (the session can be retrieved from this object)
	 * @param response The response <code>SourceBean</code> to be populated
	 * @param obj the obj
	 * 
	 * @throws InvalidOperationRequest the invalid operation request
	 * @throws EMFUserError the EMF user error
	 */
	public void handleDocumentTemplateEdit(RequestContainer requestContainer, 
			BIObject obj, SourceBean response) throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDocumentCompositionInternalEngine cannot build document template");
		throw new InvalidOperationRequest();
	}
}
