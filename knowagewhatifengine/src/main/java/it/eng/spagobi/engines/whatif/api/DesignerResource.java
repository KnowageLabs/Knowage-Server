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
package it.eng.spagobi.engines.whatif.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.schema.MondrianSchemaManager;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.services.proxy.ArtifactServiceProxy;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.writeback4j.mondrian.MondrianDriver;
import it.eng.spagobi.writeback4j.mondrian.MondrianSchemaRetriver;

/**
 * @author spetrovic
 *
 */
@Path("/1.0/designer")
@ManageAuthorization

public class DesignerResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(DesignerResource.class);
	private MondrianSchemaRetriver retriver = null;
	private String reference;
	private static final String SUCCESS_REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/whatIf2.jsp";
	@Context HttpServletRequest request;
	@Context HttpServletResponse response;

	@GET
	@Path("/allcubes/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllCubes(@PathParam("id") Integer id) throws SpagoBIEngineException {

		logger.debug("IN");
		if (id != -1) {

			WhatIfEngineInstance ei = getWhatIfEngineInstance();
			ArtifactServiceProxy artifactProxy = (ArtifactServiceProxy) ei.getEnv().get(EngineConstants.ENV_ARTIFACT_PROXY);
			MondrianSchemaManager schemaManager = new MondrianSchemaManager(artifactProxy);
			reference = schemaManager.getMondrianSchemaURI(id);
			MondrianDriver driver = new MondrianDriver(reference);
			retriver = new MondrianSchemaRetriver(driver);
			JSONArray array = new JSONArray(retriver.getAllCubes());
			logger.debug("OUT");
			return array.toString();

		}
		return "";
	}

	@GET
	@Path("/cubes/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCubes(@PathParam("id") Integer id) throws SpagoBIEngineException {

		logger.debug("IN");
		if (id != -1) {

			WhatIfEngineInstance ei = getWhatIfEngineInstance();
			ArtifactServiceProxy artifactProxy = (ArtifactServiceProxy) ei.getEnv().get(EngineConstants.ENV_ARTIFACT_PROXY);
			MondrianSchemaManager schemaManager = new MondrianSchemaManager(artifactProxy);
			reference = schemaManager.getMondrianSchemaURI(id);
			MondrianDriver driver = new MondrianDriver(reference);
			retriver = new MondrianSchemaRetriver(driver);
			JSONArray array = new JSONArray(retriver.getCubes());
			logger.debug("OUT");
			return array.toString();

		}
		return "";
	}

	@GET
	@Path("/cubes/getMDX/{id}/{cubeName}")
	@Produces(MediaType.TEXT_HTML)
	public String getMDX(@PathParam("id") Integer id, @PathParam("cubeName") String cubeName) throws SpagoBIEngineException {
		logger.debug("IN");
		String mdx = "";
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		ArtifactServiceProxy artifactProxy = (ArtifactServiceProxy) ei.getEnv().get(EngineConstants.ENV_ARTIFACT_PROXY);
		MondrianSchemaManager schemaManager = new MondrianSchemaManager(artifactProxy);
		String reference = schemaManager.getMondrianSchemaURI(id);
		MondrianDriver driver = new MondrianDriver(reference);
		retriver = new MondrianSchemaRetriver(driver);
		String firstDimension = retriver.getFirstDimension(cubeName);
		String firstMeasure = retriver.getFirstMeasure(cubeName);
		mdx = "select {[Measures].[" + firstMeasure + "]} on columns, {([" + firstDimension + "])} on rows from [" + cubeName + "]";
		logger.debug("OUT");
		return mdx;
	}

	@POST
	@Path("/cubes")
	@Produces("text/html")
	@Consumes(MediaType.APPLICATION_JSON)
	public void saveMdx(String body) throws SpagoBIEngineException, JSONException {
		logger.debug("IN");
		boolean whatif;
		JSONObject obj = new JSONObject(body);
		String currentArtifactId = obj.getString("mondrianSchemaId");
		String artifactId = obj.getString("id");
		List<String> userProfileAttributes = new ArrayList<String>();

		WhatIfTemplate template = new WhatIfTemplate();
		template.setMondrianSchema(obj.getString("mondrianSchema"));
		template.setMdxQuery(obj.getString("mdxQuery"));
		template.setMondrianMdxQuery(obj.getString("mondrianMdxQuery"));
		template.setProfilingUserAttributes(userProfileAttributes);
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		ei.getEnv().put(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID, currentArtifactId);
		ei.getEnv().put(SpagoBIConstants.SBI_ARTIFACT_ID, artifactId);
		if (ei.getEnv().get("ENGINE").equals("knowageolapengine")) {
			whatif = false;
		} else {
			whatif = true;
		}
		ei.updateWhatIfEngineInstance(template, false, ei.getEnv());
		logger.debug("OUT");
	}

	@GET
	@Path("/cubes/start")
	@Produces("text/html")
	public View redirect() throws SpagoBIEngineException, ServletException, IOException {
		logger.debug("IN");
		return new View(SUCCESS_REQUEST_DISPATCHER_URL);
	}

	@GET
	@Path("/measures/{id}/{cubeName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllMeasures(@PathParam("id") Integer id, @PathParam("cubeName") String cubeName) throws SpagoBIEngineException {

		logger.debug("IN");

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		ArtifactServiceProxy artifactProxy = (ArtifactServiceProxy) ei.getEnv().get(EngineConstants.ENV_ARTIFACT_PROXY);
		MondrianSchemaManager schemaManager = new MondrianSchemaManager(artifactProxy);
		reference = schemaManager.getMondrianSchemaURI(id);
		MondrianDriver driver = new MondrianDriver(reference);
		retriver = new MondrianSchemaRetriver(driver);
		JSONArray array = new JSONArray(retriver.getAllMeasures(cubeName));
		logger.debug("OUT");
		return array.toString();
	}

}
