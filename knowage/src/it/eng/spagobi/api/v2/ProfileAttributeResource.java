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
package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bo.ProfileAttribute;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

@Path("/2.0/attributes")
@ManageAuthorization
public class ProfileAttributeResource extends AbstractSpagoBIResource {

	private static Logger logger = Logger.getLogger(ProfileAttributeResource.class);

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public List<ProfileAttribute> getProfileAttributes() {
		ISbiAttributeDAO objDao = null;
		List<SbiAttribute> attrList = null;
		List<ProfileAttribute> profileAttrs = new ArrayList<>();
		try {
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			attrList = objDao.loadSbiAttributes();

			if (attrList != null && !attrList.isEmpty()) {
				for (SbiAttribute attr : attrList) {
					ProfileAttribute pa = new ProfileAttribute(attr);
					profileAttrs.add(pa);
				}
			}

			return profileAttrs;

		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			logger.error("Error while loading profile attributes", e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}

	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAttribute(@PathParam("id") Integer id, @Valid ProfileAttribute attr) {
		ISbiAttributeDAO objDao = null;
		ProfileAttribute attribute = attr;

		if (attribute == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (attribute.getAttributeId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The domain with ID " + id + " doesn't exist").build();
		}

		try {
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			SbiAttribute sa = new SbiAttribute(attribute.getAttributeId(), attribute.getAttributeName(), attribute.getAttributeDescription());
			objDao.saveOrUpdateSbiAttribute(sa);

			return Response.ok().build();

		} catch (EMFUserError e) {
			logger.error("Error while updating profile attribute", e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}

	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ProfileAttribute insertAttribute(@Valid ProfileAttribute attr) {
		ISbiAttributeDAO objDao = null;
		ProfileAttribute attribute = attr;

		try {
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			SbiAttribute sa = new SbiAttribute();
			sa.setAttributeName(attribute.getAttributeName());
			sa.setDescription(attribute.getAttributeDescription());
			Integer id = objDao.saveSbiAttribute(sa);
			attribute.setAttributeId(id);

			return attribute;
		} catch (EMFUserError e) {
			logger.error("Error while saving profile attribute", e);
			throw new SpagoBIRestServiceException(getLocale(), e);

		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	public Response removeAttribute(@PathParam("id") Integer id) {
		ISbiAttributeDAO objDao = null;
		try {
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());
			objDao.deleteSbiAttributeById(id);
			return Response.ok().build();
		} catch (EMFUserError e) {
			logger.error("Error while deleting resource", e);
			throw new SpagoBIRestServiceException(getLocale(), e);

		}
	}

	@DELETE
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.PROFILE_MANAGEMENT })
	public Response deleteMultiple(@QueryParam("id") int[] ids) {
		ISbiAttributeDAO objDao = null;

		try {
			objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(getUserProfile());

			for (int i = 0; i < ids.length; i++) {
				objDao.deleteSbiAttributeById(ids[i]);
			}

			return Response.ok().build();
		} catch (EMFUserError e) {
			logger.error("Error while deleting resource", e);
			throw new SpagoBIRestServiceException(getLocale(), e);

		}

	}

}
