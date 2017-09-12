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
package it.eng.spagobi.analiticalmodel.execution.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.JSONStoreFeedTransformer;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.SpagoBIServiceExceptionHandler;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetRolesForExecutionAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "GET_ROLES_SERVICE";
	
	// request parameters
	public static String DOCUMENT_ID = ObjectsTreeConstants.OBJECT_ID;
	public static String DOCUMENT_LABEL = ObjectsTreeConstants.OBJECT_LABEL;
	
	// logger component
	private static Logger logger = Logger.getLogger(GetRolesForExecutionAction.class);
	
	
	public void doService() {
		Integer documentId;
		String documentLabel;
		BIObject obj;
		IEngUserProfile profile;
		List roleNames;
		List roles;
		
		
		logger.debug("IN");
		
		try {
		profile = getUserProfile();
		documentId = requestContainsAttribute( DOCUMENT_ID )? getAttributeAsInteger( DOCUMENT_ID ): null;
		documentLabel = getAttributeAsString( DOCUMENT_LABEL );
		
		logger.debug("Parameter [" + DOCUMENT_ID + "] is equals to [" + documentId + "]");
		logger.debug("Parameter [" + DOCUMENT_LABEL + "] is equals to [" + documentLabel + "]");
		
		Assert.assertTrue(!StringUtilities.isEmpty( documentLabel ) || documentId != null, 
				"At least one between [" + DOCUMENT_ID + "] and [" + DOCUMENT_LABEL + "] parameter must be specified on request");
		
		// load object to check if it exists
		obj = null;
		if ( !StringUtilities.isEmpty( documentLabel ) ) {
			logger.info("Loading document with label = [" + documentLabel + "] ...");
			try {
				obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
			} catch (EMFUserError error) {
				logger.error("Object with label equals to [" + documentLabel + "] not found");
				throw new SpagoBIServiceException(SERVICE_NAME, "Object with label equals to [" + documentLabel + "] not found", error);
			}		
		} else if ( documentId != null ) {
			logger.info("Loading biobject with id = [" + documentId + "] ...");
			try {
				obj = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
			} catch (EMFUserError error) {
				logger.error("Object with id equals to [" + documentId + "] not found");
				throw new SpagoBIServiceException(SERVICE_NAME, "Object with id equals to [" + documentId + "] not found", error);
			}
		} else {
			Assert.assertUnreachable("At least one between [" + DOCUMENT_ID + "] and [" + DOCUMENT_LABEL + "] parameter must be specified on request");
		}
		Assert.assertNotNull(obj, "Impossible to load document [" +  (StringUtilities.isEmpty(documentLabel)? documentId: documentLabel) + "]");
		logger.info("... docuemnt loaded succesfully");
			
		// retrive roles for execution
		try {
			roleNames = ObjectsAccessVerifier.getCorrectRolesForExecution(obj.getId(), profile);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, t);			
		} 
		
		if (roleNames == null || roleNames.size() == 0) {
			logger.warn("Object cannot be executed by any role of the user");
			throw new SpagoBIServiceException(SERVICE_NAME, "Object cannot be executed by any role of the user");
		} else {
			roles = new ArrayList();
			Iterator it = roleNames.iterator();
			while(it.hasNext()) {
				String roleName = (String)it.next();
				try {
					Role role = DAOFactory.getRoleDAO().loadByName(roleName);
					roles.add(role);
				} catch (EMFUserError error) {
					logger.error("Role with name equals to [" + roleName + "] not found");
					throw new SpagoBIServiceException(SERVICE_NAME, "Role with name equals to [" + roleName + "] not found", error);
				}
				
			}
		}
		
		JSONObject rolesJSON = null;
		try {
			JSONArray  dataJSON = (JSONArray)SerializerFactory.getSerializer("application/json").serialize( roles,null );
			rolesJSON = (JSONObject)JSONStoreFeedTransformer.getInstance().transform(dataJSON, 
					"id", "name", "description", new String[]{"id", "name", "description"}, new Integer(roleNames.size()));
		} catch (SerializationException e) {
			e.printStackTrace();
		}
		
		try {
			writeBackToClient( new JSONSuccess( rolesJSON ) );
		} catch (IOException e) {
			throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
		}
		} catch (Throwable t) {
			throw SpagoBIServiceExceptionHandler.getInstance().getWrappedException(SERVICE_NAME, t);
		} finally {
			logger.debug("OUT");
		}
	}

}
