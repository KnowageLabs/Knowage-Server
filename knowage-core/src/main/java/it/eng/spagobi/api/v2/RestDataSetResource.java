/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */
@Path("/2.0/datasetsorion")
@ManageAuthorization
public class RestDataSetResource extends AbstractDataSetResource {

	static protected Logger logger = Logger.getLogger(RestDataSetResource.class);

	@GET
	@Path("/test/orion")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getSbiRestDataSet() {
		try {
			URL resource = Thread.currentThread().getContextClassLoader().getResource("../test/dataset/orion.json");
			byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()));
			String datasetDescription = new String(encoded, "UTF-8");
			return datasetDescription;
		} catch (Exception e) {
			String error = "Error while reading file SbiRESTDataSet.json";
			logger.error(error);
			throw new SpagoBIRuntimeException(error, e);
		}
	}
}