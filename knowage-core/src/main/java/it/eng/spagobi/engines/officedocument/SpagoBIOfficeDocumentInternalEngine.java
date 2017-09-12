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

package it.eng.spagobi.engines.officedocument;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.container.ContextManager;
import it.eng.spagobi.container.SpagoBISessionContainer;
import it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.content.service.ContentServiceImplSupplier;
import it.eng.spagobi.utilities.mime.MimeUtils;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;

public class SpagoBIOfficeDocumentInternalEngine implements InternalEngineIFace {

	public static final String messageBundle = "MessageFiles.component_spagobiofficedocIE_messages";
	
	private static transient Logger logger=Logger.getLogger(SpagoBIOfficeDocumentInternalEngine.class);
	
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
		
		SessionContainer session = requestContainer.getSessionContainer();
		ContextManager contextManager = new ContextManager(new SpagoBISessionContainer(session), 
				new LightNavigatorContextRetrieverStrategy(requestContainer.getServiceRequest()));
		
		if (obj == null) {
			logger.error("The input object is null");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		}

		if (!obj.getBiObjectTypeCode().equalsIgnoreCase("OFFICE_DOC")) {
			logger.error("The input object is not a office document");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1001", messageBundle);
		}
		
		try {
			//defines the mime type to imposte correctly the response
			IEngUserProfile profile = (IEngUserProfile) session.getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			ContentServiceImplSupplier c = new ContentServiceImplSupplier();
			Content template = c.readTemplate(profile.getUserUniqueIdentifier().toString(), obj.getId().toString(), null);
			String templateFileName = template.getFileName();
	
			logger.debug("Template Read");
	
			if(templateFileName==null){
				logger.warn("Template has no name");
				templateFileName="";
			}
			Locale locale = GeneralUtilities.getDefaultLocale();
			response.setAttribute("LOCALE", locale);
			
			String mimeType = MimeUtils.getMimeType(templateFileName);
			logger.debug("Mime type is = " + mimeType);
			if (mimeType.startsWith("image")){
				response.setAttribute("isImage", new Boolean(true));
			}
			response.setAttribute(ObjectsTreeConstants.SESSION_OBJ_ATTR, obj);
			// set information for the publisher
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "OFFICE_DOC");
		} catch (Exception e) {
			logger.error("Cannot exec the Office document", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
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
		logger.error("SpagoBIOfficeDocumentInternalEngine cannot build document template");
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
		logger.error("SpagoBIOfficeDocumentInternalEngine cannot build document template");
		throw new InvalidOperationRequest();
	}
}
