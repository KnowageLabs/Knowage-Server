package it.eng.spagobi.api;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SnapshotMainInfo;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/documentsnapshot")
@ManageAuthorization
public class DocumentExecutionSnapshot extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(DocumentExecutionSnapshot.class);

	public static final String SERVICE_NAME = "SNAPSHOT_SERVICE";

	@GET
	@Path("/getSnapshots")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getSnapshots(@QueryParam("id") Integer biobjectId, @Context HttpServletRequest req) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List<SnapshotMainInfo> snapshotsList = null;
		try {
			snapshotsList = DAOFactory.getSnapshotDAO().getSnapshotMainInfos(biobjectId);
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("Cannot load scheduled executions", e);
		}
		resultAsMap.put("schedulers", snapshotsList);
		resultAsMap.put("urlPath", GeneralUtilities.getSpagoBIProfileBaseUrl(this.getUserProfile().getUserUniqueIdentifier().toString()));
		return Response.ok(resultAsMap).build();
	}

	@GET
	@Path("/getSnapshootBySchedulation")
	public Map<String, Map<Integer, List<Snapshot>>> getSnapshoots(@QueryParam("schedulation") String schedulation) {
		try {
			ISnapshotDAO snapdao = DAOFactory.getSnapshotDAO();
			return snapdao.getSnapshotsBySchedulation(schedulation, true, false);
		} catch (EMFUserError e) {
			LOGGER.error(e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
	}

	@GET
	@Path("/getSnapshotsForSchedulationAndDocument")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getSnapshotsForSchedulationAndDocument(@QueryParam("id") Integer biobjectId, @QueryParam("scheduler") String scheduler,
			@Context HttpServletRequest req) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		// workaround , should pass an Id or a label cause name cannot contain spaces otherwise
		scheduler = scheduler.replaceAll("%2520", " ");

		List<Snapshot> snapshotsList = null;
		try {
			snapshotsList = DAOFactory.getSnapshotDAO().getSnapshotsForSchedulationAndDocument(biobjectId, scheduler, false);
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
					LOGGER.debug("Current user [{}] can see snapshot with id = {} of document with id = {}", ((UserProfile) profile).getUserId().toString(), idSnap, objectId);
					ISnapshotDAO snapdao = DAOFactory.getSnapshotDAO();
					snap = snapdao.loadSnapshot(idSnap);
					content = snap.getContent();
					if (snap.getContentType() != null) {
						contentType = snap.getContentType();
					}
				} else {
					LOGGER.error("Current user [{}] CANNOT see snapshot with id = {} of document with id = {}", ((UserProfile) profile).getUserId().toString(), idSnap, objectId);
					// content = "You cannot see required snapshot.".getBytes();
					content = "You cannot see required snapshot.".getBytes(UTF_8);
				}
			} catch (EMFInternalError e) {
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
			LOGGER.error(e);
		} catch (EMFInternalError e) {
			LOGGER.error(e);
		} catch (JSONException e) {
			LOGGER.error(e);
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
					LOGGER.error("Impossible to delete snapshot with name [{}] already exists", ids[i], e);
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
