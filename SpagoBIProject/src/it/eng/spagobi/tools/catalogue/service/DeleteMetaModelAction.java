/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.catalogue.service;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class DeleteMetaModelAction extends AbstractSpagoBIAction {

	// logger component
	public static Logger logger = Logger.getLogger(DeleteMetaModelAction.class);

	public static String ID = "id";
	
	@Override
	public void doService() {
		
		logger.debug("IN");
		
		try {
		
			IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
			dao.setUserProfile(this.getUserProfile());
			
			Integer id = this.getAttributeAsInteger( ID );
			logger.debug("Id = " + id);
			Assert.assertNotNull(id, "Input id parameter cannot be null");
			
			MetaModel model = dao.loadMetaModelById(id);
			
			if (model == null) {
				logger.warn("Meta model with id " + id + " not found...");
			} else {
				
				HashMap logParameters = new HashMap<String, String>();
				logParameters.put("MODEL", model.toString());
				String logOperation = "META_MODEL_CATALOGUE.DELETE";
				
				try {
					dao.eraseMetaModel(id);
					logger.debug("Model [" + model + "] deleted");
				} catch (Throwable t) {
					AuditLogUtilities.updateAudit(getHttpRequest(), this.getUserProfile(), logOperation, logParameters , "KO");
					throw new SpagoBIServiceException(SERVICE_NAME, "Error while erasing meta model [" + model + "]", t);
				}
				
				AuditLogUtilities.updateAudit(getHttpRequest(), this.getUserProfile(), logOperation, logParameters , "OK");
				
			}
			
			try {
				writeBackToClient( new JSONAcknowledge() );
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the response to the client", e);
			}
			
		} finally {
			logger.debug("OUT");
		}
		
	}
	
}
