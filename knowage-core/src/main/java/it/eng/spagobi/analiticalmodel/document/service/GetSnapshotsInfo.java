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
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.SpagoBIRequestContainer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Retrieves information about all snapshots of the document identified on request with attribute SpagoBIConstants.OBJECT_ID.
 * The response is something like this (suitable for javascript evaluation):
 * {id: snapshot1_id, name: 'snapshot1_name', description: 'snapshot1_description', historyLength: snapshot1_historyLength};;{id: snapshot2_id, name: 'snapshot2_name', description: 'snapshot2_description', historyLength: snapshot2_historyLength};;...
 * If the document has no public snapshots, an empty string is returned.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class GetSnapshotsInfo extends AbstractHttpAction {

	static Logger logger = Logger.getLogger(GetSnapshotsInfo.class);
	
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
				List snapshotsList = DAOFactory.getSnapshotDAO().getSnapshots(objId);
				List evaluatedSnapshotsName = new ArrayList();
				Iterator it = snapshotsList.iterator();
				while (it.hasNext()) {
					Snapshot snapshot = (Snapshot) it.next();
					if (evaluatedSnapshotsName.contains(snapshot.getName())) {
						continue;
					} else {
						evaluatedSnapshotsName.add(snapshot.getName());
					}
					int historyLength = findHistoryLength(snapshotsList, snapshot.getName());
					output.append("{id: " + snapshot.getId().toString() + ", " +
									"name: \"" + JavaScript.escapeText(snapshot.getName()) + "\", " +
									"description: \"" + JavaScript.escapeText(snapshot.getDescription()) + "\", " +
									"historyLength: " + new Integer(historyLength).toString() + "}");
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

	private int findHistoryLength(List snapshotsList, String name) {
		int count = 0;
		Iterator it = snapshotsList.iterator();
		while (it.hasNext()) {
			Snapshot snapshot = (Snapshot) it.next();
			if (snapshot.getName().equals(name)) {
				count++;
			}
		}
		return count;
	}
	
}
