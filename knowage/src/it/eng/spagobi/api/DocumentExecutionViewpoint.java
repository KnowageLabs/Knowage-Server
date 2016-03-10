package it.eng.spagobi.api;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

@Path("/1.0/documentviewpoint")
@ManageAuthorization
public class DocumentExecutionViewpoint extends AbstractSpagoBIResource {

	// request parameters
	private static final String NAME = "NAME";
	private static final String DESCRIPTION = "DESCRIPTION";
	private static final String SCOPE = "SCOPE";
	private static final String VIEWPOINT = "VIEWPOINT";
	private static final String OBJECT_LABEL = "OBJECT_LABEL";
	private static final String ROLE = "ROLE";
	public static final String SERVICE_NAME = "SAVE_VIEWPOINTS_SERVICE";

	@POST
	@Path("/addViewpoint")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response addViewoint(@Context HttpServletRequest req) {
		logger.debug("IN");
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List errorList = new ArrayList<>();
		String viewpointOwner;
		String viewpointString;
		IViewpointDAO viewpointDAO;
		Viewpoint viewpoint = null;
		try {
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			String role = (String) requestVal.opt(ROLE);
			String viewpointName = (String) requestVal.opt(NAME);
			String viewpointDescription = (String) requestVal.opt(DESCRIPTION);
			String label = (String) requestVal.opt(OBJECT_LABEL);
			JSONObject viewpointJSON = (JSONObject) requestVal.opt(VIEWPOINT);
			String viewpointScope = (String) requestVal.opt(SCOPE);
			if (requestVal.opt(SCOPE) == null || ((String) requestVal.opt(SCOPE)).trim().isEmpty()) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Viewpoint's scope cannot be null or empty");
			}
			if (requestVal.opt(NAME) == null || ((String) requestVal.opt(NAME)).trim().isEmpty()) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Viewpoint's name cannot be null or empty");
			}
			if (requestVal.opt(DESCRIPTION) == null || ((String) requestVal.opt(DESCRIPTION)).trim().isEmpty()) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Viewpoint's description cannot be null or empty");
			}
			IEngUserProfile userProfile = this.getUserProfile();
			Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
			BIObject obj;
			try {
				obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByLabelAndRole(label, role);
				logger.debug("User: [" + userProfile.getUserUniqueIdentifier() + "]");
				logger.debug("Document Id:  [" + obj.getId() + "]");
				viewpointOwner = (String) ((UserProfile) userProfile).getUserId();
				Iterator it = viewpointJSON.keys();
				Assert.assertTrue(it.hasNext(), "Viewpoint's content cannot be empty");
				viewpointString = "";
				while (it.hasNext()) {
					String parameterName = (String) it.next();
					String parameterValue;
					parameterValue = viewpointJSON.getString(parameterName);
					// defines the string of parameters to save into db
					if (!StringUtilities.isEmpty(parameterValue)) {
						viewpointString += parameterName + "%3D" + parameterValue + "%26";
					}
				}
				if (viewpointString.endsWith("%26")) {
					viewpointString = viewpointString.substring(0, viewpointString.length() - 3);
				}
				logger.debug("Viewpoint's content will be saved on database as: [" + viewpointString + "]");
				viewpointDAO = DAOFactory.getViewpointDAO();
				viewpoint = viewpointDAO.loadViewpointByNameAndBIObjectId(viewpointName, obj.getId());
				Assert.assertTrue(viewpoint == null, "A viewpoint with the name [" + viewpointName + "] alredy exist");
				viewpointDAO = DAOFactory.getViewpointDAO();
				viewpointDAO.setUserProfile(userProfile);
				viewpoint = new Viewpoint();
				viewpoint.setBiobjId(obj.getId());
				viewpoint.setVpName(viewpointName);
				viewpoint.setVpOwner(viewpointOwner);
				viewpoint.setVpDesc(viewpointDescription);
				viewpoint.setVpScope(viewpointScope);
				viewpoint.setVpValueParams(viewpointString);
				viewpoint.setVpCreationDate(new Timestamp(System.currentTimeMillis()));
				viewpointDAO.insertViewpoint(viewpoint);
				// reload viewpoint with new ID
				viewpoint = viewpointDAO.loadViewpointByNameAndBIObjectId(viewpointName, obj.getId());
			} catch (EMFUserError e1) {
				throw new SpagoBIServiceException(SERVICE_NAME, e1.getMessage());
			}

		} catch (IOException e2) {
			throw new SpagoBIServiceException(SERVICE_NAME, e2.getMessage());
		} catch (JSONException e2) {
			throw new SpagoBIServiceException(SERVICE_NAME, e2.getMessage());
		}
		resultAsMap.put("viewpoint", viewpoint);
		return Response.ok(resultAsMap).build();
	}

	@GET
	@Path("/getViewpoints")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getViewpoints(@QueryParam("label") String label, @QueryParam("role") String role, @Context HttpServletRequest req) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List viewpoints;
		IEngUserProfile userProfile;
		Integer biobjectId;
		IViewpointDAO viewpointDAO;
		userProfile = this.getUserProfile();
		Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
		BIObject obj;
		try {
			obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByLabelAndRole(label, role);
			biobjectId = obj.getId();
			logger.debug("User: [" + userProfile.getUserUniqueIdentifier() + "]");
			logger.debug("Document Id:  [" + biobjectId + "]");
			try {
				viewpointDAO = DAOFactory.getViewpointDAO();
				viewpoints = viewpointDAO.loadAccessibleViewpointsByObjId(biobjectId, getUserProfile());
			} catch (EMFUserError e) {
				logger.error("Cannot load viewpoints for document [" + biobjectId + "]", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load viewpoints for document [" + biobjectId + "]", e);
			}
			logger.debug("Document [" + biobjectId + "] have " + (viewpoints == null ? "0" : "" + viewpoints.size()) + " valid viewpoints for user ["
					+ userProfile.getUserUniqueIdentifier() + "]");
			resultAsMap.put("viewpoints", viewpoints);
		} catch (EMFUserError e1) {
			throw new SpagoBIServiceException(SERVICE_NAME, e1.getMessage());
		}
		return Response.ok(resultAsMap).build();
	}

	@GET
	@Path("/deleteViewpoint")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response deleteViewpoint(@QueryParam("id") Integer id, @Context HttpServletRequest req) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		IEngUserProfile userProfile;
		IViewpointDAO viewpointDAO;
		Viewpoint viewpoint;
		userProfile = this.getUserProfile();
		Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
		try {
			viewpointDAO = DAOFactory.getViewpointDAO();
			viewpoint = viewpointDAO.loadViewpointByID(Integer.valueOf(id));
			Assert.assertNotNull(viewpoint, "Viewpoint [" + id + "] does not exist on the database");
			viewpointDAO.eraseViewpoint(viewpoint.getVpId());
		} catch (EMFUserError e) {
			logger.error("Impossible to delete viewpoint with name [" + id + "] already exists", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to delete viewpoint with name [" + id + "] already exists", e);
		}
		return Response.ok(resultAsMap).build();
	}

}
