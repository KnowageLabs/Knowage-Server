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
