/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
