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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.JSONTemplateUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
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
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private AnalyticalModelDocumentManagementAPI documentManagementAPI;

	@POST
	@Path("/saveChartTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String saveTemplate(@FormParam("jsonTemplate") String jsonTemplate, @FormParam("docLabel") String docLabel,
			@Context HttpServletResponse servletResponse) {
		String xml = null;
		try {
			JSONObject json = new JSONObject(jsonTemplate);

			xml = JSONTemplateUtilities.convertJsonToXML(json);

		} catch (Exception e) {
			logger.error("Error converting JSON Template to XML...", e);
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);

		}

		saveTemplate(docLabel, xml);

		return xml;
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

	private void saveTemplate(String docLabel, String xml) {
		ObjTemplate template = new ObjTemplate();
		template.setName("Template.xml");
		template.setContent(xml.getBytes());
		template.setDimension(Long.toString(xml.getBytes().length / 1000) + " KByte");

		IBIObjectDAO biObjectDao;
		BIObject document;
		try {
			if (documentManagementAPI == null) {
				documentManagementAPI = new AnalyticalModelDocumentManagementAPI(getUserProfile());
			}
			biObjectDao = DAOFactory.getBIObjectDAO();
			document = biObjectDao.loadBIObjectById(new Integer(docLabel));
			documentManagementAPI.saveDocument(document, template);
		} catch (EMFUserError e) {
			logger.error("Error saving JSON Template to XML...", e);
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		}
	}
}
