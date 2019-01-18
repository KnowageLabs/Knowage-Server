/**Knowage,Open Source Business Intelligence suite*Copyright(C)2016 Engineering Ingegneria Informatica S.p.A.**Knowage is free software:you can redistribute it and/or modify*it under the terms of the GNU Affero General Public License as published by*the Free Software Foundation,either version 3 of the License,or*(at your option)any later version.**Knowage is distributed in the hope that it will be useful,*but WITHOUT ANY WARRANTY;without even the implied warranty of*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the*GNU Affero General Public License for more details.**You should have received a copy of the GNU Affero General Public License*along with this program.If not,see<http://www.gnu.org/licenses/>.
*/

package it.eng.spagobi.api;

import java.io.IOException;import java.sql.Timestamp;import java.util.ArrayList;import java.util.HashMap;import java.util.Iterator;import java.util.List;

import javax.servlet.http.HttpServletRequest;import javax.ws.rs.GET;import javax.ws.rs.POST;import javax.ws.rs.Path;import javax.ws.rs.Produces;import javax.ws.rs.QueryParam;import javax.ws.rs.core.Context;import javax.ws.rs.core.MediaType;import javax.ws.rs.core.Response;

import org.json.JSONException;import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;import it.eng.spago.security.IEngUserProfile;import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelViewpointDAO;import it.eng.spagobi.commons.bo.UserProfile;import it.eng.spagobi.commons.dao.DAOFactory;import it.eng.spagobi.commons.utilities.StringUtilities;import it.eng.spagobi.services.rest.annotations.ManageAuthorization;import it.eng.spagobi.tools.catalogue.bo.MetaModel;import it.eng.spagobi.utilities.assertion.Assert;import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/metamodelviewpoint")
@ManageAuthorization
public class BusinessModelExecutionViewpoint extends AbstractSpagoBIResource {

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
		IMetaModelViewpointDAO metaModelViewpointDAO;
		Viewpoint viewpoint = null;
		try {
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			String role = (String) requestVal.opt(ROLE);
			String viewpointName = (String) requestVal.opt(NAME);
			String viewpointDescription = (String) requestVal.opt(DESCRIPTION);
			String name = (String) requestVal.opt(OBJECT_LABEL);
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
			MetaModel metaModel;
			try {
				metaModel = DAOFactory.getMetaModelsDAO().loadMetaModelForExecutionByNameAndRole(name, role);
				logger.debug("User: [" + ((UserProfile) userProfile).getUserId() + "]");
				logger.debug("Document Id: [" + metaModel.getId() + "]");
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
				metaModelViewpointDAO = DAOFactory.getMetaModelViewpointDAO();
				viewpoint = metaModelViewpointDAO.loadViewpointByNameAndMetaModelId(viewpointName, metaModel.getId());
				Assert.assertTrue(viewpoint == null, "A viewpoint with the name [" + viewpointName + "] alredy exist");
				metaModelViewpointDAO = DAOFactory.getMetaModelViewpointDAO();
				metaModelViewpointDAO.setUserProfile(userProfile);
				viewpoint = new Viewpoint();
				viewpoint.setBiobjId(metaModel.getId());
				viewpoint.setVpName(viewpointName);
				viewpoint.setVpOwner(viewpointOwner);
				viewpoint.setVpDesc(viewpointDescription);
				viewpoint.setVpScope(viewpointScope);
				viewpoint.setVpValueParams(viewpointString);
				viewpoint.setVpCreationDate(new Timestamp(System.currentTimeMillis()));
				metaModelViewpointDAO.insertMetaModelViewpoint(viewpoint);
				// reload viewpoint with new ID
				viewpoint = metaModelViewpointDAO.loadViewpointByNameAndMetaModelId(viewpointName, metaModel.getId());
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
	public Response getViewpoints(@QueryParam("label") String name, @QueryParam("role") String role, @Context HttpServletRequest req) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List viewpoints;
		IEngUserProfile userProfile;
		Integer metaModelId;
		IMetaModelViewpointDAO metaModelViewpointDAO;
		userProfile = this.getUserProfile();
		Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
		MetaModel metaModel;
		metaModel = DAOFactory.getMetaModelsDAO().loadMetaModelForExecutionByNameAndRole(name, role);
		metaModelId = metaModel.getId();
		logger.debug("User: [" + ((UserProfile) userProfile).getUserId() + "]");
		logger.debug("Document Id: [" + metaModelId + "]");
		try {
			metaModelViewpointDAO = DAOFactory.getMetaModelViewpointDAO();
			viewpoints = metaModelViewpointDAO.loadAccessibleViewpointsByMetaModelId(metaModelId, getUserProfile());
		} catch (EMFUserError e) {
			logger.error("Cannot load viewpoints for document [" + metaModelId + "]", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load viewpoints for document [" + metaModelId + "]", e);
		}
		logger.debug("Document [" + metaModelId + "] have " + (viewpoints == null ? "0" : "" + viewpoints.size()) + " valid viewpoints for user ["
				+ ((UserProfile) userProfile).getUserId() + "]");
		resultAsMap.put("viewpoints", viewpoints);
		return Response.ok(resultAsMap).build();
	}

	@POST
	@Path("/deleteViewpoint")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response deleteViewpoint(@Context HttpServletRequest req) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		IEngUserProfile userProfile;
		IMetaModelViewpointDAO metaModelViewpointDAO;
		Viewpoint viewpoint;
		String viewpointIds;
		String[] ids;
		userProfile = this.getUserProfile();
		Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
		JSONObject requestVal;
		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			if (requestVal.opt(VIEWPOINT) == null || ((String) requestVal.opt(VIEWPOINT)).trim().isEmpty()) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Viewpoint's Ids cannot be null or empty");
			}
			viewpointIds = (String) requestVal.opt(VIEWPOINT);
			ids = viewpointIds.split(",");
			for (int i = 0; i < ids.length; i++) {
				try {
					metaModelViewpointDAO = DAOFactory.getMetaModelViewpointDAO();
					viewpoint = metaModelViewpointDAO.loadViewpointByID(Integer.valueOf(ids[i]));
					Assert.assertNotNull(viewpoint, "Viewpoint [" + ids[i] + "] does not exist on the database");
					metaModelViewpointDAO.eraseViewpoint(viewpoint.getVpId());
				} catch (EMFUserError e) {
					logger.error("Impossible to delete viewpoint with name [" + ids[i] + "] already exists", e);
					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to delete viewpoint with name [" + ids[i] + "] already exists", e);
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
