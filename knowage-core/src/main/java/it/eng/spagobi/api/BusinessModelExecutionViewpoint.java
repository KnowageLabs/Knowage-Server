/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.spagobi.api;

import static java.util.stream.Collectors.joining;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelViewpointDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/1.0/metamodelviewpoint")
@ManageAuthorization
public class BusinessModelExecutionViewpoint extends AbstractSpagoBIResource {

	public static final String SERVICE_NAME = "SAVE_VIEWPOINTS_SERVICE";

	@POST
	@Path("/addViewpoint")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response addViewoint(@Valid AddViewpointRequestDTO request) {
		logger.debug("IN");
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		String viewpointOwner;
		IMetaModelViewpointDAO metaModelViewpointDAO;
		Viewpoint viewpoint = null;

		String role = request.getRole();
		String viewpointName = request.getName();
		String viewpointDescription = request.getDescription();
		String name = request.getObjLabel();
		Map<String, String> viewpointReq = request.getViewpoint();
		String viewpointScope = request.getScope();

		UserProfile userProfile = UserProfileManager.getProfile();
		Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
		MetaModel metaModel;
		try {
			String viewpointString = null;

			metaModel = DAOFactory.getMetaModelsDAO().loadMetaModelForExecutionByNameAndRole(name, role, false);
			logger.debug("User: [" + userProfile.getUserId() + "]");
			logger.debug("Document Id: [" + metaModel.getId() + "]");
			viewpointOwner = (String) userProfile.getUserId();

			viewpointString = viewpointReq.entrySet()
					.stream()
					.map(e -> e.getKey() + "%3D" + e.getValue())
					.collect(joining("%26"));

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

		resultAsMap.put("viewpoint", viewpoint);
		return Response.ok(resultAsMap).build();
	}

	@GET
	@Path("/getViewpoints")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getViewpoints(@Valid @ExtendedAlphanumeric @QueryParam("label") String name, @Valid @ExtendedAlphanumeric @QueryParam("role") String role) {
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List viewpoints;
		IEngUserProfile userProfile;
		Integer metaModelId;
		IMetaModelViewpointDAO metaModelViewpointDAO;
		userProfile = this.getUserProfile();
		Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
		MetaModel metaModel;
		metaModel = DAOFactory.getMetaModelsDAO().loadMetaModelForExecutionByNameAndRole(name, role, false);
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
	public Response deleteViewpoint(@Valid DeleteViewpointRequestDTO request) {
		IMetaModelViewpointDAO metaModelViewpointDAO;
		Viewpoint viewpoint;
		String id = request.getName();

		try {
			metaModelViewpointDAO = DAOFactory.getMetaModelViewpointDAO();
			viewpoint = metaModelViewpointDAO.loadViewpointByID(Integer.valueOf(id));
			Assert.assertNotNull(viewpoint, "Viewpoint [" + id + "] does not exist on the database");
			metaModelViewpointDAO.eraseViewpoint(viewpoint.getVpId());
		} catch (EMFUserError e) {
			logger.error("Impossible to delete viewpoint with name [" + id + "] already exists", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to delete viewpoint with name [" + id + "] already exists", e);
		}

		return Response.ok().build();
	}

}
