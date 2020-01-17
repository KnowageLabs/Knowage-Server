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

package it.eng.spagobi.mapcatalogue.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.mapcatalogue.serializer.GeoLayerJSONDeserializer;
import it.eng.spagobi.mapcatalogue.serializer.GeoLayerJSONSerializer;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@ManageAuthorization
@Path("/layers")
public class LayerCRUD {

	static private Logger logger = Logger.getLogger(LayerCRUD.class);
	public static final String LAYER_ID = "id";
	public static final String LAYER_LABEL = "label";
	public static final String LAYER_URL = "layerUrl";

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String loadLayers(@Context HttpServletRequest req) throws JSONException, UnsupportedEncodingException {
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		List<GeoLayer> layers = null;
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		try {
			layers = dao.loadAllLayers(null, profile);
		} catch (EMFUserError e) {
			logger.error("Error loading the layers", e);
			throw new SpagoBIRuntimeException("Error loading the layers", e);
		}
		if (layers == null) {
			logger.debug("No layer found");
			return "{root:[]}";
		}

		logger.debug("Serializing the layers");
		ObjectMapper mapper = new ObjectMapper();
		String s = "[]";
		try {
			s = mapper.writeValueAsString(layers);
		} catch (Exception e) {
			logger.error("Error serializing the layers", e);
			throw new SpagoBIRuntimeException("Error serializing the layers", e);
		}

		logger.debug("Layers serialized");
		return "{\"root\":" + s + "}";
	}

	@GET
	@Path("/getFilter")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getFilter(@Context HttpServletRequest req) throws JSONException {
		Object id = null;
		Integer layerId = null;

		try {

			id = req.getParameter("id");
			if (id == null || id.equals("")) {
				throw new SpagoBIRuntimeException("The layer id passed in the request is null or empty");
			}
			layerId = new Integer(id.toString());
		} catch (Exception e) {
			logger.error("error loading filter", e);
			throw new SpagoBIRuntimeException("error request", e);
		}

		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		ArrayList<String> properties = null;
		try {
			properties = dao.getProperties(layerId);
		} catch (Exception e2) {
			logger.error("Error loading the properties for the layer with id " + id, e2);
			throw new SpagoBIRuntimeException(
					"Error loading the properties for the layer with id [" + id + "]. Please check the layer configuration or the file content.", e2);
		}
		ArrayList<JSONObject> prop = new ArrayList<>();
		for (int i = 0; i < properties.size(); i++) {
			JSONObject obj = new JSONObject();
			obj.put("property", properties.get(i));
			prop.add(obj);
		}

		return prop.toString();
	}

	/**
	 * @deprecated Replaced by {@link #getDownload(int, String)}; this method contains a wrong management of parameters.
	 */
	@GET
	@Path("/getDownload")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@Deprecated
	public String getDownload(@Context HttpServletRequest req) throws JSONException {
		Object id = null;
		Integer layerId = null;
		String typeWFS = null;
		String request;
		String[] requestSplit;
		try {

			request = req.getParameter("id");
			requestSplit = request.split(",");
			id = requestSplit[0];
			requestSplit[1] = requestSplit[1].replaceAll("typeWFS=", "");
			typeWFS = requestSplit[1];
			if (id == null || id.equals("")) {
				throw new SpagoBIRuntimeException("The layer id passed in the request is null or empty");
			}
			layerId = new Integer(id.toString());
		} catch (Exception e) {
			logger.error("error loading filter", e);
			throw new SpagoBIRuntimeException("error request", e);
		}

		logger.debug("Deleting the layer");
		return getData(layerId, typeWFS);
	}

	@GET
	@Path("/{layerId}/download/{typeWFS}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDownload(@PathParam("layerId") int layerId, @PathParam("typeWFS") String typeWFS) throws JSONException {
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		JSONObject content = dao.getContentforDownload(layerId, typeWFS);
		if (content == null) {
			return Response.status(404).build();
		} else {
			return Response.ok(content.toString()).build();
		}
	}

	private String getData(int layerId, String typeWFS) {
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		JSONObject content = dao.getContentforDownload(layerId, typeWFS);
		return "" + content + "";
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/getroles")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getRoles(@Context HttpServletRequest req) throws JSONException, IOException {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		// get roles from database
		List<Role> roles = null;
		Role aRole = null;
		ArrayList<JSONObject> roles_get = new ArrayList<JSONObject>();
		try {
			roles = DAOFactory.getRoleDAO().loadAllRolesFiltereByTenant();
		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("OUT");

		for (Iterator<Role> it = roles.iterator(); it.hasNext();) {
			aRole = it.next();

			JSONObject jo = new JSONObject();
			try {
				jo.put("id", aRole.getId());
				jo.put("name", aRole.getName());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			roles_get.add(jo);

		}
		return roles_get.toString();

	}

	@GET
	@Path("/GetLayer")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getLayer(@Context HttpServletRequest req) throws EMFUserError, UnsupportedEncodingException, JSONException {
		Object id = null;
		Integer layerId = null;

		try {

			id = req.getParameter("id");
			if (id == null || id.equals("")) {
				throw new SpagoBIRuntimeException("The layer id passed in the request is null or empty");
			}
			layerId = new Integer(id.toString());
		} catch (Exception e) {
			logger.error("error loading filter", e);
			throw new SpagoBIRuntimeException("error request", e);
		}

		logger.debug("Deleting the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		GeoLayer aLayer = dao.loadLayerByID(layerId);
		ObjectMapper mapper = new ObjectMapper();
		String s = "[]";
		try {
			s = mapper.writeValueAsString(aLayer);
		} catch (Exception e) {
			logger.error("Error serializing the layers", e);
			throw new SpagoBIRuntimeException("Error serializing the layers", e);
		}

		return s;
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/postitem")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String postItem(@Context HttpServletRequest req) throws EMFUserError, JSONException {

		JSONObject requestBodyJSON = null;
		ArrayList<JSONObject> roles_get = new ArrayList<JSONObject>();
		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
		} catch (Exception e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}
		// get roles for item selected from database
		List<Role> roles = new ArrayList<>();
		Role aRole = null;
		try {
			roles = DAOFactory.getRoleDAO().loadRolesItem(requestBodyJSON);

		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("OUT");

		for (Iterator<Role> it = roles.iterator(); it.hasNext();) {
			aRole = it.next();
			JSONObject jo = new JSONObject();
			try {
				jo.put("id", aRole.getId());
				jo.put("name", aRole.getName());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			roles_get.add(jo);

		}
		return roles_get.toString();

	}

	@POST
	@Path("/deleteLayer")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteLayer(@Context HttpServletRequest req) throws JSONException {
		Object id = null;
		Integer layerId = null;

		try {
			id = req.getParameter("id");
			if (id == null || id.equals("")) {
				throw new SpagoBIRuntimeException("The layer id passed in the request is null or empty");
			}
			layerId = new Integer(id.toString());
		} catch (Exception e) {
			logger.error("error loading the layer to delete from the request", e);
			throw new SpagoBIRuntimeException("error loading the layer to delete from the request", e);
		}

		logger.debug("Deleting the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		try {
			dao.eraseLayer(layerId);
		} catch (EMFUserError e) {
			logger.error("Error delationg the ayer with id " + id, e);
			throw new SpagoBIRuntimeException("Error delationg the layer with id " + id, e);
		}
		return "{}";
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveLayer(@Context HttpServletRequest req) throws JSONException, EMFUserError, IOException {
		JSONObject requestBodyJSON = null;
		Integer id;
		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
		} catch (Exception e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

		GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);
		Assert.assertNotNull(aLayer, "The layer is null");
		logger.debug("Layer deserialized correctly");
		logger.debug("Saving the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		// TODO check if profile is null
		dao.setUserProfile(profile);

		try {
			id = dao.insertLayer(aLayer);

		} catch (EMFUserError e) {
			logger.error("Error saving the layer", e);
			throw new SpagoBIRuntimeException("Error saving the layer", e);
		}
		logger.debug("Layer saved: layer label " + aLayer.getLabel());
		// String id_return = "{id:" +id + "}";
		return "{\"id\":" + id + "}";
	}

	@POST
	@Path("/addData")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN)
	public String saveLayer2(MultiPartBody input, @Context HttpServletRequest req) {
		JSONObject requestBodyJSON = null;
		Integer id;

		try {

			String dataJSON = input.getTextParameterValues("data")[0];
			requestBodyJSON = new JSONObject(dataJSON);

			GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);
			ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();

			final FormFile file = input.getFormFileParameterValues("layerFile")[0];
			byte[] data = file.getContent();
			aLayer.setPathFile("");
			aLayer.setFilebody(data);

			id = dao.insertLayer(aLayer);

			logger.debug("Layer saved: layer label " + aLayer.getLabel());

			return "{\"id\":" + id + "}";

		} catch (EMFUserError | IOException | JSONException e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

	}

	@POST
	@Path("/deleterole")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteRole(@Context HttpServletRequest req) throws JSONException {
		JSONObject requestBodyJSON = null;
		Integer id_role = null;
		Integer layerid = null;

		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
		} catch (Exception e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

		id_role = requestBodyJSON.getInt("id");
		layerid = requestBodyJSON.getInt("id_l");

		logger.debug("Deleting the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		try {
			dao.eraseRole(id_role, layerid);
		} catch (EMFUserError e) {

		}
		return "{}";
	}

	@PUT
	@Path("/updateData")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN)
	public String modifyLayerwithFile(MultiPartBody input, @Context HttpServletRequest req) {
		JSONObject requestBodyJSON = null;

		try {

			String dataJSON = input.getTextParameterValues("data")[0];
			requestBodyJSON = new JSONObject(dataJSON);

			GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);
			ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();

			final FormFile file = input.getFormFileParameterValues("layerFile")[0];
			byte[] data = file.getContent();

			String path = SpagoBIUtilities.getResourcePath();
			aLayer.setPathFile(path);
			aLayer.setFilebody(data);

			dao.modifyLayer(aLayer, true);

			logger.debug("Layer saved: layer label " + aLayer.getLabel());
			return "{}";

		} catch (EMFUserError | IOException | JSONException e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String modifyLayer(@Context HttpServletRequest req) throws EMFUserError, JSONException, UnsupportedEncodingException {
		JSONObject requestBodyJSON = null;
		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
		} catch (Exception e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

		GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);
		Assert.assertNotNull(aLayer, "The layer is null");
		logger.debug("Layer deserialized correctly");

		logger.debug("Updating the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		try {
			dao.modifyLayer(aLayer, false);
		} catch (EMFUserError e) {
			logger.error("Error updating the layer", e);
			throw new SpagoBIRuntimeException("Error updating the layer", e);
		}
		logger.debug("Layer updated: layer label " + aLayer.getLabel());
		return "{}";
	}

	@POST
	@Path("/getLayerProperties")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getLayerProperties(@Context HttpServletRequest req, MultivaluedMap<String, String> form) {

		String s = "[]";
		List<String> labels = form.get("labels");
		List<GeoLayer> layers = new ArrayList<GeoLayer>();

		for (int i = 0; i < labels.size(); i++) {
			try {
				ISbiGeoLayersDAO geoLayersDAO = DAOFactory.getSbiGeoLayerDao();
				geoLayersDAO.setUserProfile((IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE));
				GeoLayer geoLayer = geoLayersDAO.loadLayerByLabel(labels.get(i));

				if (geoLayer != null) {
					layers.add(geoLayer);

				}
			} catch (EMFUserError e) {
				logger.error("Error getting layer properties", e);
				throw new SpagoBIRuntimeException("Error getting layer properties", e);
			}
		}

		logger.debug("Serializing the layers");
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
		simpleModule.addSerializer(GeoLayer.class, new GeoLayerJSONSerializer());
		mapper.registerModule(simpleModule);
		try {
			s = mapper.writeValueAsString(layers);
		} catch (Exception e) {
			logger.error("Error serializing the layers", e);
			throw new SpagoBIRuntimeException("Error serializing the layers", e);
		}
		logger.debug("Layers serialized");

		logger.debug("OUT");

		return "{\"root\":" + s + "}";

	}

	// get uploaded filename, is there a easy way in RESTEasy?
	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {
				String[] name = filename.split("=");
				String fn = name[1];

				if (fn.contains(File.separator)) {
					int beginIndex = fn.lastIndexOf(File.separator) + 1;
					if (beginIndex < fn.length()) {
						fn = fn.substring(beginIndex);
					}
				}

				String finalFileName = fn.trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	@POST
	@Path("/getLayerFromList")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getLayerFromList(@Context HttpServletRequest req)
			throws JSONException, EMFUserError, JsonGenerationException, JsonMappingException, IOException {
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		// TODO check if profile is null
		dao.setUserProfile(profile);

		JSONObject requestBodyJSON = null;
		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
		} catch (Exception e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

		String[] layList;
		List<GeoLayer> layers = null;
		if (requestBodyJSON.has("items")) {
			layList = new String[requestBodyJSON.getJSONArray("items").length()];
			for (int i = 0; i < requestBodyJSON.getJSONArray("items").length(); i++) {
				layList[i] = requestBodyJSON.getJSONArray("items").getString(i);
			}

			if (layList != null && layList.length > 0) {
				try {
					layers = dao.loadAllLayers(layList, profile);
				} catch (EMFUserError e) {
					logger.error("Error loading the layers", e);
					throw new SpagoBIRuntimeException("Error loading the layers", e);
				}
			}
		}

		if (layers == null) {
			logger.debug("No layer found");
			return "{\"root\":[]}";
		}

		logger.debug("Serializing the layers");
		ObjectMapper mapper = new ObjectMapper();
		String s = "[]";
		try {
			s = mapper.writeValueAsString(layers);
		} catch (Exception e) {
			logger.error("Error serializing the layers", e);
			throw new SpagoBIRuntimeException("Error serializing the layers", e);
		}

		logger.debug("Layers serialized");
		return "{\"root\":" + s + "}";
	}
}
