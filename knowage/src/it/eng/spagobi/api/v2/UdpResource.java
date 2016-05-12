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

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.deserializer.JSONDeserializer;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.udp.dao.IUdpDAO;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/2.0/udp")
@ManageAuthorization
public class UdpResource extends AbstractSpagoBIResource {

	// logger component-
	private static Logger logger = Logger.getLogger(UdpResource.class);

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.USER_DATA_PROPERTIES_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSbiUdps(@Context HttpServletRequest request) {
		logger.debug("IN");
		IUdpDAO sbiUdpDao = null;
		List<SbiUdp> allObjects = null;
		Locale locale = request.getLocale();

		try {
			sbiUdpDao = DAOFactory.getUdpDAO();
			sbiUdpDao.setUserProfile(getUserProfile());
			allObjects = sbiUdpDao.findAll();
			Integer totalNum = sbiUdpDao.countUdp();
			JSONArray udpJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(allObjects, locale);

			if (udpJSON != null && udpJSON.length() > 0) {
				String toBeReturned = udpJSON.toString(); // JsonConverter.objectToJson(udpJSON, udpJSON.getClass());
				return Response.ok(toBeReturned).build();
			}

			JSONObject response = new JSONObject();
			response.put("Error", "List UDP is empty");
			return Response.status(Status.NOT_FOUND).entity(response.toString()).build();

		} catch (Exception e) {
			logger.error("Error while getting the list of SbiUdps", e);
			throw new SpagoBIRuntimeException("Error while getting the list of Udps", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.USER_DATA_PROPERTIES_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSingleSbiUdp(@Context HttpServletRequest request, @PathParam("id") Integer id) {
		logger.debug("IN");
		IUdpDAO sbiUdpsDao = null;
		List<SbiUdp> allObjects = null;
		Locale locale = request.getLocale();

		try {
			sbiUdpsDao = DAOFactory.getUdpDAO();
			sbiUdpsDao.setUserProfile(getUserProfile());
			allObjects = sbiUdpsDao.findAll();

			if (allObjects != null && !allObjects.isEmpty()) {
				for (SbiUdp udp : allObjects) {
					if (udp.getUdpId() == id) {
						JSONObject udpJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(udp, locale);
						String toBeReturned = udpJSON.toString();
						return Response.ok(toBeReturned).build();
					}
				}

				JSONObject response = new JSONObject();
				response.put("Error", "SbiUdp " + id + " not found");
				return Response.status(Status.NOT_FOUND).entity(response.toString()).build();
			}
		} catch (Exception e) {
			logger.error("Error while getting SbiUdp " + id, e);
			throw new SpagoBIRuntimeException("Error while getting SbiUdp " + id, e);
		} finally {
			logger.debug("OUT");
		}
		return Response.noContent().build();
	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.USER_DATA_PROPERTIES_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertSbiUdp(String body) {

		IUdpDAO sbiUdpsDao = null;
		JSONDeserializer deserializer = new JSONDeserializer();
		SbiUdp sbiUdp;
		try {
			sbiUdp = (SbiUdp) deserializer.deserialize(body, SbiUdp.class);
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while parsing JSON", e);
			throw new SpagoBIRuntimeException("Error while parsing JSON", e);
		}

		if (sbiUdp == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (sbiUdp.getUdpId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error paramters. New User Data Property should not have ID value").build();
		}

		try {
			sbiUdpsDao = DAOFactory.getUdpDAO();
			sbiUdpsDao.setUserProfile(getUserProfile());
			sbiUdpsDao.insert(sbiUdp);
			String encodedSbiUdp = URLEncoder.encode("" + sbiUdp.getUdpId(), "UTF-8");
			return Response.created(new URI("1.0/userdataproperties/" + encodedSbiUdp)).entity(encodedSbiUdp).build();
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while creating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while creating url of the new resource", e);
		}
	}

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.USER_DATA_PROPERTIES_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateSbiUdp(@PathParam("id") Integer id, String body) {

		IUdpDAO sbiUdpsDao = null;

		JSONDeserializer deserializer = new JSONDeserializer();
		SbiUdp sbiUdp;

		try {
			sbiUdp = (SbiUdp) deserializer.deserialize(body, SbiUdp.class);
		} catch (Exception e) {
			Response.notModified().build();
			logger.error("Error while parsing JSON", e);
			throw new SpagoBIRuntimeException("Error while parsing JSON", e);
		}

		if (sbiUdp == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (sbiUdp.getUdpId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The Udp with ID " + id + " doesn't exist").build();
		}

		try {
			sbiUdpsDao = DAOFactory.getUdpDAO();
			sbiUdpsDao.setUserProfile(getUserProfile());
			sbiUdpsDao.update(sbiUdp);
			String encodedSbiUdp = URLEncoder.encode("" + sbiUdp.getUdpId(), "UTF-8");
			return Response.created(new URI("1.0/userdataproperties/" + encodedSbiUdp)).entity(encodedSbiUdp).build();
		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);
		}
	}

	@DELETE
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.USER_DATA_PROPERTIES_MANAGEMENT })
	public Response deleteUdp(@PathParam("id") Integer id) {
		IUdpDAO sbiUdpsDao = null;
		try {
			sbiUdpsDao = DAOFactory.getUdpDAO();
			sbiUdpsDao.setUserProfile(getUserProfile());
			sbiUdpsDao.delete(id);
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}
	}
}
