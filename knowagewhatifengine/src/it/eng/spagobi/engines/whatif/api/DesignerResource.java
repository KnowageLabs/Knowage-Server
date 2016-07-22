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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.schema.MondrianSchemaManager;
import it.eng.spagobi.services.proxy.ArtifactServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.writeback4j.mondrian.MondrianDriver;
import it.eng.spagobi.writeback4j.mondrian.MondrianSchemaRetriver;

/**
 * @author spetrovic
 *
 */
@Path("/1.0/designer")
public class DesignerResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(DesignerResource.class);
	private MondrianSchemaRetriver retriver = null;

	@GET
	@Path("/cubes/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllCubes(@PathParam("id") Integer id) throws SpagoBIEngineException {

		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		ArtifactServiceProxy artifactProxy = (ArtifactServiceProxy) ei.getEnv().get(EngineConstants.ENV_ARTIFACT_PROXY);
		MondrianSchemaManager schemaManager = new MondrianSchemaManager(artifactProxy);
		String reference = schemaManager.getMondrianSchemaURI(id);
		MondrianDriver driver = new MondrianDriver(reference);
		retriver = new MondrianSchemaRetriver(driver);
		JSONArray array = new JSONArray(retriver.getAllCubes());
		logger.debug("OUT");
		return array.toString();
	}

	@GET
	@Path("/cubes/getMDX/{id}/{cubeName}")
	@Produces(MediaType.TEXT_HTML)
	public String getMDX(@PathParam("id") Integer id, @PathParam("cubeName") String cubeName) throws SpagoBIEngineException {
		String mdx = "";
		logger.debug("IN");
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
}
