/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.util.JavaScript;
import it.eng.spago.util.StringUtils;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.SpagoBIRequestContainer;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Retrieves information about the subObject identified on request with attribute SpagoBIConstants.SUBOBJECT_ID.
 * The response is something like this (suitable for javascript evaluation):
 * {id: suobject_id, name: 'suobject_name', description: 'suobject_description' .....}
 * If the subObject does not exist, an empty string is returned.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class GetSubobjectInfo extends AbstractHttpAction {

	static Logger logger = Logger.getLogger(GetSubobjectInfo.class);
	
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) throws Exception {
		logger.debug("IN");
		freezeHttpResponse();
		HttpServletResponse httResponse = getHttpResponse();
		StringBuffer output = new StringBuffer();
		try {
			SpagoBIRequestContainer request = new SpagoBIRequestContainer(serviceRequest);
			if (request.isBlankOrNull(SpagoBIConstants.SUBOBJECT_ID)) {
				output.append("");
			} else {
				Integer subObjId = request.getInteger(SpagoBIConstants.SUBOBJECT_ID);
				SubObject subobject = DAOFactory.getSubObjectDAO().getSubObject(subObjId);
				String dateFormat = request.getString("DATE_FORMAT");
				
				String creationDate = null;
				String lastModificationDate = null;
				try {
					creationDate = StringUtils.dateToString(subobject.getCreationDate(), dateFormat);
				} catch (Exception e) {
					logger.error(e);
					creationDate = subobject.getCreationDate().toString();
				}
				try {
					lastModificationDate = StringUtils.dateToString(subobject.getLastChangeDate(), dateFormat);
				} catch (Exception e) {
					logger.error(e);
					lastModificationDate = subobject.getLastChangeDate().toString();
				}
				
				output.append("{id: " + subobject.getId().toString() + ", " +
						"name: \"" + JavaScript.escapeText(subobject.getName()) + "\", " +
						"description: \"" + JavaScript.escapeText(subobject.getDescription()) + "\", " +
						"owner: \"" + JavaScript.escapeText(subobject.getOwner()) + "\", " +
						"creationDate: \"" + JavaScript.escapeText(creationDate) + "\", " +
						"lastModificationDate: \"" + JavaScript.escapeText(lastModificationDate) + "\", " +
						"isPublic: " + JavaScript.escapeText(subobject.getIsPublic().toString()) + "}");
			}
		} catch (Exception e) {
			logger.error("Error while recovering subobjects list", e);
			output.append("");
		} finally {
			httResponse.getOutputStream().write(output.toString().getBytes());
			httResponse.getOutputStream().flush();
			logger.debug("OUT");
		}
	}
	
}
