package it.eng.spagobi.api;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

@Path("/1.0/documentsnapshot")
@ManageAuthorization
public class DocumentExecutionSnapshot extends AbstractSpagoBIResource {

	public static final String SERVICE_NAME = "SNAPSHOT_SERVICE";

	@GET
	@Path("/getSnapshots")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getSnapshots(@QueryParam("id") Integer biobjectId, @Context HttpServletRequest req) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List snapshotsList = null;
		try {
			snapshotsList = DAOFactory.getSnapshotDAO().getSnapshots(biobjectId);
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("Cannot load scheduled executions", e);
		}
		resultAsMap.put("schedulers", snapshotsList);
		resultAsMap.put("urlPath", GeneralUtilities.getSpagoBIProfileBaseUrl(this.getUserProfile().getUserUniqueIdentifier().toString()));
		return Response.ok(resultAsMap).build();
	}

	@GET
	@Path("/getSnapshotContent")
	@Produces("application/octet-stream")
	public String getSnapshotContent(@QueryParam("biobjectId") Integer objectId, @QueryParam("idSnap") Integer idSnap, @Context HttpServletRequest req) {

		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		IEngUserProfile profile = this.getUserProfile();
		Snapshot snap = null;
		BIObject obj;
		try {
			obj = DAOFactory.getBIObjectDAO().loadBIObjectById(objectId);
			byte[] content = null;
			String contentType = "text/html";
			try {
				if (ObjectsAccessVerifier.canSee(obj, profile)) {
					logger.debug("Current user [" + ((UserProfile) profile).getUserId().toString() + "] can see snapshot with id = " + idSnap
							+ " of document with id = " + objectId);
					ISnapshotDAO snapdao = DAOFactory.getSnapshotDAO();
					snap = snapdao.loadSnapshot(idSnap);
					content = snap.getContent();
					if (snap.getContentType() != null) {
						contentType = snap.getContentType();
					}
				} else {
					logger.error("Current user [" + ((UserProfile) profile).getUserId().toString() + "] CANNOT see snapshot with id = " + idSnap
							+ " of document with id = " + objectId);
					// content = "You cannot see required snapshot.".getBytes();
					content = "You cannot see required snapshot.".getBytes("UTF-8");
				}
			} catch (EMFInternalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resultAsMap.put("snapshot", snap);
		JSONObject object = new JSONObject();

		try {
			object.put("snapshot", snap.getContent());
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFInternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return object.toString();
		// return Response.ok(resultAsMap).build();
	}

	@POST
	@Path("/deleteSnapshot")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response deleteSnapshot(@Context HttpServletRequest req) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		IEngUserProfile userProfile;
		ISnapshotDAO snapdao;
		Snapshot snapshot;
		String snapshotIds;
		String[] ids;
		userProfile = this.getUserProfile();
		Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
		JSONObject requestVal;
		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			if (requestVal.opt("SNAPSHOT") == null || ((String) requestVal.opt("SNAPSHOT")).trim().isEmpty()) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Snapshot's Ids cannot be null or empty");
			}
			snapshotIds = (String) requestVal.opt("SNAPSHOT");
			ids = snapshotIds.split(",");
			for (int i = 0; i < ids.length; i++) {
				try {
					snapdao = DAOFactory.getSnapshotDAO();
					snapshot = snapdao.loadSnapshot(Integer.valueOf(ids[i]));
					Assert.assertNotNull(snapshot, "Snapshot [" + ids[i] + "] does not exist on the database");
					snapdao.deleteSnapshot(snapshot.getId());
				} catch (EMFUserError e) {
					logger.error("Impossible to delete snapshot with name [" + ids[i] + "] already exists", e);
					throw new SpagoBIServiceException("Impossible to delete snapshot with name [" + ids[i] + "] already exists", e);
				}
			}

		} catch (IOException e1) {
			throw new SpagoBIServiceException(SERVICE_NAME, e1.getMessage());
		} catch (JSONException e1) {
			throw new SpagoBIServiceException(SERVICE_NAME, e1.getMessage());
		}

		return Response.ok(resultAsMap).build();
	}

}
