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

import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineAnalysisMetadata;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

@Path("/1.0/subobject")
public class SubObjectResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(AnalysisResource.class);

	@POST
	@Path("/{name}/{description}/{scope}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public String save(@PathParam("name") String name, @PathParam("description") String description, @PathParam("scope") String scope) {
		EngineAnalysisMetadata analysisMetadata = null;

		logger.debug("IN");

		logger.debug("Subobject Name: " + name);
		logger.debug("Subobject description: " + description);
		logger.debug("Subobject scope: " + scope);

		analysisMetadata = getWhatIfEngineInstance().getAnalysisMetadata();
		analysisMetadata.setName(name);
		analysisMetadata.setDescription(description);

		if (EngineAnalysisMetadata.PUBLIC_SCOPE.equalsIgnoreCase(scope)) {
			analysisMetadata.setScope(EngineAnalysisMetadata.PUBLIC_SCOPE);
		} else if (EngineAnalysisMetadata.PRIVATE_SCOPE.equalsIgnoreCase(scope)) {
			analysisMetadata.setScope(EngineAnalysisMetadata.PRIVATE_SCOPE);
		} else {
			Assert.assertUnreachable("Value [" + scope + "] is not valid for the input parameter scope");
		}

		String result = null;
		try {
			result = saveAnalysisState();
		} catch (SpagoBIEngineException e) {
			logger.error("Error saving the subobject", e);
			throw new SpagoBIRestServiceException("sbi.olap.save.analysis.error", getLocale(), "Error saving the subobject", e);
		}
		if (!result.trim().toLowerCase().startsWith("ok")) {
			logger.error("Error saving the subobject " + result);
			throw new SpagoBIRestServiceException("sbi.olap.save.analysis.error", getLocale(), "Error saving the subobject");
		}

		return getJsonSuccess();
	}

}
