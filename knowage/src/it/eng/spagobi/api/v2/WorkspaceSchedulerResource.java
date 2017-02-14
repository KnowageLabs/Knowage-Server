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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * @author Stefan Petrovic (Stefan.Petrovic@mht.net)
 */
@Path("/2.0/pdf")
@ManageAuthorization
public class WorkspaceSchedulerResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(WorkspaceSchedulerResource.class);

	@GET
	@Path("/{scheduler}")
	@UserConstraint(functionalities = { SpagoBIConstants.SCHEDULER_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public String getSchedulations(@PathParam("scheduler") String name) throws IOException, EMFUserError, EMFInternalError, JSONException {
		logger.debug("IN");
		JSONArray toreturn = new JSONArray();

		ISnapshotDAO snapDao = null;
		Map<String, Map<Integer, List<Snapshot>>> list = null;
		List<Snapshot> snapshotList = null;
		try {
			snapDao = DAOFactory.getSnapshotDAO();
			list = snapDao.getSnapshotsBySchedulation(name);
		} catch (EMFUserError e) {
			throw new SpagoBIRestServiceException("Error with getting snapshpots", buildLocaleFromSession(), e);

		}
		for (Entry<String, Map<Integer, List<Snapshot>>> entry : list.entrySet()) {
			for (Entry<Integer, List<Snapshot>> deep : entry.getValue().entrySet()) {
				snapshotList = deep.getValue();
				JSONObject obj = new JSONObject();
				JSONArray ids = new JSONArray();
				for (int i = 0; i < snapshotList.size(); i++) {
					obj.put("time", snapshotList.get(i).getDateCreation());
					ids.put(snapshotList.get(i).getId());
				}

				obj.put("name", entry.getKey());
				obj.put("ids", ids);
				toreturn.put(obj);

			}

		}
		return toreturn.toString();

	}

	@POST
	@Path("/merge")
	@Produces("application/pdf")
	@UserConstraint(functionalities = { SpagoBIConstants.SCHEDULER_MANAGEMENT })
	public void  merge(@Context HttpServletRequest req) {
		logger.debug("IN");
		ISnapshotDAO snapDao = null;
		PDFMergerUtility mergePdf = new PDFMergerUtility();
		
		try {
			snapDao = DAOFactory.getSnapshotDAO();
			JSONArray snapshotIds = RestUtilities.readBodyAsJSONArray(req);
			for (int i = 0; i < snapshotIds.length(); i++) {
				Integer id = Integer.valueOf(snapshotIds.getString(i));
				Snapshot snap = snapDao.loadSnapshot(id);
				InputStream is = new ByteArrayInputStream(snap.getContent());
				mergePdf.addSource(is);
			}
			// download merged file
			 ByteArrayOutputStream pdfDownload = new ByteArrayOutputStream();
			 //mergePdf.setDestinationFileName(SpagoBIUtilities.getResourcePath()+"/"+"Merge.pdf");
			 mergePdf.setDestinationStream(pdfDownload);
			 mergePdf.mergeDocuments(null);
			 
			 response.setContentLength(pdfDownload.size());
			  response.setContentType("application/pdf");
			  response.setHeader("Content-Disposition", "filename=mergedDocument.pdf");
			  response.setHeader("Pragma", "public");
			  response.setHeader("Cache-Control", "max-age=0");
			  response.addDateHeader("Expires", 0);
			  response.getOutputStream().write(pdfDownload.toByteArray());

		} catch (EMFUserError e) {
			throw new SpagoBIRestServiceException("Error with getting snapshpots", buildLocaleFromSession(), e);
		} catch (IOException e) {
			throw new SpagoBIRestServiceException("I/O Error with getting snapshpot ids from request", buildLocaleFromSession(), e);
		} catch (JSONException e) {
			throw new SpagoBIRestServiceException("JSON Error with getting snapshpot ids from request", buildLocaleFromSession(), e);
		} catch (EMFInternalError e) {
			throw new SpagoBIRestServiceException(" Error while crating input stream for the content of a snapshot", buildLocaleFromSession(), e);
		}

		



	}

}
