/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.mapcatalogue.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.mapcatalogue.serializer.GeoLayerJSONDeserializer;
import it.eng.spagobi.mapcatalogue.serializer.GeoLayerJSONSerializer;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/layers")
public class LayerCRUD {

	static private Logger logger = Logger.getLogger(LayerCRUD.class);
	private static final String FILE = "File";
	private static final String PROPS_FILE = "propsFile";
	private static final String fileValidationError = "error.mesage.description.layer.validation.file";
	public static final String LAYER_ID = "id";
	public static final String LAYER_LABEL = "label";

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String loadLayers(@Context HttpServletRequest req) {
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		List<GeoLayer> layers = null;
		try {
			layers = dao.loadAllLayers();
		} catch (EMFUserError e) {
			logger.error("Error loading the layers", e);
			throw new SpagoBIRuntimeException("Error loading the layers", e);
		}
		if (layers == null) {
			logger.debug("No layer found");
			return "{root:[]}";
		}
		System.out.println("Contengo " + layers.toString());
		logger.debug("Serializing the layers");
		ObjectMapper mapper = new ObjectMapper();
		// SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));
		// simpleModule.addSerializer(GeoLayer.class, new GeoLayerJSONSerializer());
		// mapper.registerModule(simpleModule);
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
	@Path("/getroles")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getRoles(@Context HttpServletRequest req) {

		// get roles from database
		List roles = null;
		Role aRole = null;
		ArrayList<JSONObject> roles_get = new ArrayList<JSONObject>();
		// gets roles from database
		try {
			roles = DAOFactory.getRoleDAO().loadAllRoles();
			System.out.println(roles.toString());
		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("OUT");

		for (Iterator it = roles.iterator(); it.hasNext();) {
			aRole = (Role) it.next();
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
	public String deleteLayer(@Context HttpServletRequest req) {
		Object id = null;
		Integer layerId = null;

		try {

			id = req.getParameter("id");
			System.out.println(id);
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
			throw new SpagoBIRuntimeException("Error delationg the ayer with id " + id, e);
		}
		return "{}";
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveLayer(@Context HttpServletRequest req) {
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
	public String saveLayer2(MultipartFormDataInput input, @Context HttpServletRequest req) {
		JSONObject requestBodyJSON = null;
		Integer id;

		try {

			Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
			List<InputPart> dataList = formDataMap.get("data");
			for (InputPart inputPart : dataList) {
				requestBodyJSON = new JSONObject(inputPart.getBodyAsString());
			}

			GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);

			ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();

			// prendo i pacchetti del file
			List<InputPart> inputParts = formDataMap.get("layerFile");
			LayerServices layerServices = new LayerServices();
			for (InputPart inputPart : inputParts) {
				byte[] data = inputPart.getBodyAsString().replace("data:;base64,", "").getBytes(Charset.forName("UTF-8"));
				data = layerServices.decode64(data);
				String path = layerServices.getResourcePath(data);
				aLayer.setPathFile(path);
				aLayer.setLayerDef(data);

				id = dao.insertLayer(aLayer);

				logger.debug("Layer saved: layer label " + aLayer.getLabel());
				// return "{id:"+id+"}";
				return "{\"id\":" + id + "}";
			}

		} catch (EMFUserError | IOException | JSONException e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

		return null;
	}

	@PUT
	@Path("/updateData")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN)
	public String modifyLayerwithFile(MultipartFormDataInput input, @Context HttpServletRequest req) {
		JSONObject requestBodyJSON = null;
		Integer id;

		try {

			Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
			List<InputPart> dataList = formDataMap.get("data");
			for (InputPart inputPart : dataList) {
				requestBodyJSON = new JSONObject(inputPart.getBodyAsString());
			}

			GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);

			ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();

			// prendo i pacchetti del file
			List<InputPart> inputParts = formDataMap.get("layerFile");
			LayerServices layerServices = new LayerServices();
			for (InputPart inputPart : inputParts) {

				InputStream inputStream = inputPart.getBody(InputStream.class, null);
				byte[] data = IOUtils.toByteArray(inputStream);

				// decodifico i file dalla base64
				data = layerServices.decode64(data);
				// prendo il contenuto del file
				// data = layerServices.getFile(data);
				// salvo il file sul server e mi ritorno il path
				String path = layerServices.getResourcePath(data);
				// setPath in maniera temporanea per passarlo al dao che crea il vero path
				aLayer.setPathFile(path);
				aLayer.setLayerDef(data);

				dao.modifyLayer(aLayer);

				logger.debug("Layer saved: layer label " + aLayer.getLabel());
				return "{}";

			}

		} catch (EMFUserError | IOException | JSONException e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

		return null;

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String modifyLayer(@Context HttpServletRequest req) {
		JSONObject requestBodyJSON = null;
		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
		} catch (Exception e) {
			logger.error("Error reading the body from the request", e);
			throw new SpagoBIRuntimeException("Error reading the body from the request", e);
		}

		GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);
		aLayer.setPathFile(null);
		Assert.assertNotNull(aLayer, "The layer is null");
		logger.debug("Layer deserialized correctly");

		logger.debug("Updating the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		try {
			dao.modifyLayer(aLayer);
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

}
