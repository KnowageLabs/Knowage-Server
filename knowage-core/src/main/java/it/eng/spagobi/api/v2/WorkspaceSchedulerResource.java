/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.analiticalmodel.document.dao.SnapshotDAOHibImpl;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

/**
 * @author Stefan Petrovic (Stefan.Petrovic@mht.net)
 */
@Path("/2.0/pdf")
@ManageAuthorization
public class WorkspaceSchedulerResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(WorkspaceSchedulerResource.class);

	@GET
	@Path("/{scheduler}")
	@UserConstraint(functionalities = { SpagoBIConstants.SEE_SNAPSHOTS_FUNCTIONALITY })
	@Produces(MediaType.APPLICATION_JSON)
	public String getSchedulations(@PathParam("scheduler") String name, @QueryParam("collate") boolean collate)
			throws IOException, EMFUserError, EMFInternalError, JSONException {
		logger.debug("IN");
		JSONArray toreturn = new JSONArray();

		ISnapshotDAO snapDao = null;
		Map<String, Map<Integer, List<Snapshot>>> list = null;
		List<Snapshot> snapshotList = null;
		try {
			snapDao = DAOFactory.getSnapshotDAO();
			list = snapDao.getSnapshotsBySchedulation(name, collate, false);
		} catch (EMFUserError e) {
			throw new SpagoBIRestServiceException("Error with getting snapshpots", buildLocaleFromSession(), e);

		}
		boolean isAllPDf = true;
		for (Entry<String, Map<Integer, List<Snapshot>>> entry : list.entrySet()) {
			for (Entry<Integer, List<Snapshot>> deep : entry.getValue().entrySet()) {
				snapshotList = deep.getValue();
				JSONObject obj = new JSONObject();
				JSONArray ids = new JSONArray();
				for (int i = 0; i < snapshotList.size(); i++) {
					// obj.put("time", snapshotList.get(i).getDateCreation());
					obj.put("time", SnapshotDAOHibImpl.DATE_FORMATTER.format(snapshotList.get(i).getDateCreation()));
					ids.put(snapshotList.get(i).getId());
					if (snapshotList.get(i).getContentType() == null || !snapshotList.get(i).getContentType().equalsIgnoreCase("application/pdf")) {
						isAllPDf = false;
					}
				}

				obj.put("name", entry.getKey());
				obj.put("ids", ids);
				toreturn.put(obj);

			}

		}
		JSONObject resultAsMap = new JSONObject();
		resultAsMap.put("urlPath", GeneralUtilities.getSpagoBIProfileBaseUrl(this.getUserProfile().getUserUniqueIdentifier().toString()));
		resultAsMap.put("schedulations", toreturn);
		resultAsMap.put("mergeAndNotPDF", !isAllPDf);
		return resultAsMap.toString();

	}

}
