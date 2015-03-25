/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.api;

import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
@Path("/1.0/documents")
public class DocumentResource extends AbstractSpagoBIResource {
	
	static private Logger logger = Logger.getLogger(DataSetResource.class);
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDocuments() {
		return null;
	}
	
	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDocumentDetails(@PathParam("label") String label) {
		return null;
	}
	
	@POST
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String insertDocument(@PathParam("label") String label) {
		return null;
	}
	
	@PUT
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String updateDocument(@PathParam("label") String label) {
		return null;
	}
	
	@GET
	@Path("/{label}/meta")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDocumentMeta(@PathParam("label") String label) {
		return null;
	}
	
	@GET
	@Path("/{label}/parameters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDocumentParameters(@PathParam("label") String label) {
		logger.debug("IN");		
		try {		
			List<JSONObject> parameters = getDocumentManagementAPI().getDocumentParameters(label);
			JSONArray paramsJSON = writeParameters(parameters);
			JSONObject resultsJSON = new JSONObject();
			resultsJSON.put("results", paramsJSON);			
			return resultsJSON.toString();	
		} catch(Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {			
			logger.debug("OUT");
		}	
	}
	
	public JSONArray writeParameters(List<JSONObject> params) throws Exception {		
		JSONArray paramsJSON = new JSONArray();
		
		for (Iterator iterator = params.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = (JSONObject) iterator.next();
			paramsJSON.put(jsonObject);
		}


		return paramsJSON;
	}
	
	
	// ===================================================================
	// UTILITY METHODS
	// ===================================================================
	private UserProfile getUserProfile() {
		UserProfile profile = this.getIOManager().getUserProfile();
		return profile;
	}
	
	private AnalyticalModelDocumentManagementAPI getDocumentManagementAPI() {
		AnalyticalModelDocumentManagementAPI managementAPI = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		return managementAPI;
	}
}
