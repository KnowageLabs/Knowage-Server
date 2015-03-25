/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.mapcatalogue.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.mapcatalogue.serializer.GeoLayerJSONDeserializer;
import it.eng.spagobi.mapcatalogue.serializer.GeoLayerJSONSerializer;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;


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
			logger.error("Error loading the layers",e);
			throw new SpagoBIRuntimeException("Error loading the layers",e);
		}
		if(layers==null){
			logger.debug("No layer found");
			return "{root:[]}";
		}
		
		logger.debug("Serializing the layers");
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));
		simpleModule.addSerializer(GeoLayer.class, new GeoLayerJSONSerializer());
		mapper.registerModule(simpleModule);
		String s="[]";
		try {
			s = mapper.writeValueAsString(layers);
		} catch (Exception e) {
			logger.error("Error serializing the layers",e);
			throw new SpagoBIRuntimeException("Error serializing the layers",e);
		}
		logger.debug("Layers serialized");
		return  "{root:"+s+"}";
	}
	
	@DELETE
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteLayer(@Context HttpServletRequest req) {
		Object id=null;
		Integer layerId = null;
		JSONObject requestBodyJSON;
		try {
			requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			id = requestBodyJSON.opt("id");
			if(id==null || id.equals("")){
				throw new SpagoBIRuntimeException("The layer id passed in the request is null or empty");
			}
			layerId = new Integer(id.toString());
		} catch (Exception e) {
			logger.error("error loading the layer to delete from the request",e);
			throw new SpagoBIRuntimeException("error loading the layer to delete from the request",e);
		} 
		
		logger.debug("Deleting the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		try {
			
			dao.eraseLayer(layerId);
		} catch (EMFUserError e) {
			logger.error("Error delationg the ayer with id "+id,e);
			throw new SpagoBIRuntimeException("Error delationg the ayer with id "+id,e);
		}
		return "{}";
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveLayer(@Context HttpServletRequest req) {
		JSONObject requestBodyJSON=null;
		Integer id;
		try {
			requestBodyJSON= RestUtilities.readBodyAsJSONObject(req);
		} catch (Exception e) {
			logger.error("Error reading the body from the request",e);
			throw new SpagoBIRuntimeException("Error reading the body from the request",e);
		}
		
		GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);
	
		String validation = validateLayer(aLayer, requestBodyJSON);
		if(validation!=null){
			return validation;
		}
		
		Assert.assertNotNull(aLayer, "The layer is null");
		logger.debug("Layer deserialized correctly");
		
		logger.debug("Saving the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		try {
			id = dao.insertLayer(aLayer);
		} catch (EMFUserError e) {
			logger.error("Error saving the layer",e);
			throw new SpagoBIRuntimeException("Error saving the layer",e);
		}
		logger.debug("Layer saved: layer label "+aLayer.getLabel());
		return "{id:"+id+"}";
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String modifyLayer(@Context HttpServletRequest req) {
		JSONObject requestBodyJSON=null;
		try {
			requestBodyJSON= RestUtilities.readBodyAsJSONObject(req);
		} catch (Exception e) {
			logger.error("Error reading the body from the request",e);
			throw new SpagoBIRuntimeException("Error reading the body from the request",e);
		}
		
		GeoLayer aLayer = GeoLayerJSONDeserializer.deserialize(requestBodyJSON);
		
		String validation = validateLayer(aLayer, requestBodyJSON);
		if(validation!=null){
			return validation;
		}
		
		Assert.assertNotNull(aLayer, "The layer is null");
		logger.debug("Layer deserialized correctly");
		
		logger.debug("Updating the layer");
		ISbiGeoLayersDAO dao = DAOFactory.getSbiGeoLayerDao();
		try {
			dao.modifyLayer(aLayer);
		} catch (EMFUserError e) {
			logger.error("Error updating the layer",e);
			throw new SpagoBIRuntimeException("Error updating the layer",e);
		}
		logger.debug("Layer updated: layer label "+aLayer.getLabel());
		return "{}";
	}
	
	@POST
	@Path("/getLayerProperties")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getLayerProperties(@Context HttpServletRequest req, MultivaluedMap<String, String> form){
		logger.debug("IN");
		
		String s="[]";

		
		List<String> labels = form.get("labels");
		List<GeoLayer> layers = new ArrayList<GeoLayer>();

		for(int i=0; i<labels.size(); i++){
			try {
				ISbiGeoLayersDAO geoLayersDAO = DAOFactory.getSbiGeoLayerDao();
				GeoLayer geoLayer = geoLayersDAO.loadLayerByLabel(labels.get(i));

				if (geoLayer != null){
					layers.add(geoLayer);

				}


			} catch (EMFUserError e) {
				logger.error("Error getting layer properties",e);
				throw new SpagoBIRuntimeException("Error getting layer properties",e);
			}
		}

		logger.debug("Serializing the layers");
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));
		simpleModule.addSerializer(GeoLayer.class, new GeoLayerJSONSerializer());
		mapper.registerModule(simpleModule);
		try {
			s = mapper.writeValueAsString(layers);
		} catch (Exception e) {
			logger.error("Error serializing the layers",e);
			throw new SpagoBIRuntimeException("Error serializing the layers",e);
		}
		logger.debug("Layers serialized");	

		logger.debug("OUT");

		return  "{\"root\":"+s+"}";

	}
	
	
	private String validateLayer (GeoLayer aLayer, JSONObject requestBodyJSON){
		if(aLayer.getType().equals(FILE)){
			String file = requestBodyJSON.optString(PROPS_FILE);
			if(file==null || file.contains("/") || file.contains("\\")){
				return ( ExceptionUtilities.serializeException(fileValidationError,null));
			}
		}
		return null;
	}
	
	
	
	
}
