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
package it.eng.spagobi.analiticalmodel.document.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.util.JavaScript;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.SpagoBIRequestContainer;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Retrieves information about all public suobjects of the document identified on request with attribute SpagoBIConstants.OBJECT_ID.
 * The response is something like this (suitable for javascript evaluation):
 * {id: suobject1_id, name: 'suobject1_name', description: 'suobject1_description'};;{id: suobject2_id, name: 'suobject2_name', description: 'suobject2_description'};;...
 * If the document has no public subobjects, an empty string is returned.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class GetPublicSubobjectsInfo extends AbstractHttpAction {

	static Logger logger = Logger.getLogger(GetPublicSubobjectsInfo.class);
	
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) throws Exception {
		logger.debug("IN");
		freezeHttpResponse();
		HttpServletResponse httResponse = getHttpResponse();
		StringBuffer output = new StringBuffer();
		try {
			SpagoBIRequestContainer request = new SpagoBIRequestContainer(serviceRequest);
			if (request.isBlankOrNull(SpagoBIConstants.OBJECT_ID)) {
				output.append("");
			} else {
				Integer objId = request.getInteger(SpagoBIConstants.OBJECT_ID);
				List suobjects = DAOFactory.getSubObjectDAO().getPublicSubObjects(objId);
				Iterator it = suobjects.iterator();
				while (it.hasNext()) {
					SubObject subobject = (SubObject) it.next();
					output.append("{id: " + subobject.getId().toString() + ", " +
									"name: \"" + JavaScript.escapeText(subobject.getName()) + "\", " +
									"description: \"" + JavaScript.escapeText(subobject.getDescription()) + "\"}");
					if (it.hasNext()) {
						output.append(";;");
					}
				}
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
