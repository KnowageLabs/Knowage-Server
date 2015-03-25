/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.service;

import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.kpi.ou.util.OrganizationalUnitSynchronizer;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Zerbetto Davide
 *
 */
public class SynchronizeOUsAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "SYNCHRONIZE_OUS_ACTION";
	
	// logger component
	private static Logger logger = Logger.getLogger(SynchronizeOUsAction.class);
	
	public void doService() {
		logger.debug("IN");
		List<OrganizationalUnitHierarchy> list = null;
		try {
			try {
				OrganizationalUnitSynchronizer sinc = new OrganizationalUnitSynchronizer( getUserProfile() );
				list = sinc.synchronize();
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Error while synchronizating organizational units: " + t.getMessage(), t);
			}
			
			JSONObject response = null;
			try {
				response = createResponseObject(list);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to prepare response message", e);
			}
			
			try {
				writeBackToClient( new JSONSuccess( response ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private JSONObject createResponseObject(
			List<OrganizationalUnitHierarchy> list) throws JSONException {
		JSONObject toReturn = new JSONObject();
		String message = getResponseMessage(list);
		toReturn.put("message", message);
		return toReturn;
	}

	private String getResponseMessage(List<OrganizationalUnitHierarchy> list) {
		StringBuffer buffer = new StringBuffer("");
		if (!list.isEmpty()) {
			buffer.append("The following hierarchies could not be updated since they have grants: ");
			Iterator<OrganizationalUnitHierarchy> it = list.iterator();
			while (it.hasNext()) {
				OrganizationalUnitHierarchy h = it.next();
				buffer.append(h.getLabel());
				if (it.hasNext()) {
					buffer.append(", ");
				}
			}
			buffer.append(". Please remove grants on those hierarchies and re-try.");
		}
		return buffer.toString();
	}

}
