/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.analiticalmodel.execution.service;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;

@Path("/1.0/qbe-execution-id")
public class UuidGeneratorResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(UuidGeneratorResource.class);

	@GET
	@Path("/")
	public String createNewExecutionId() {
		String executionId;

		LOGGER.debug("IN");

		executionId = null;
		try {
			UUID uuidObj = UUID.randomUUID();
			executionId = uuidObj.toString();
			executionId = executionId.replaceAll("-", "");
		} finally {
			LOGGER.debug("OUT");
		}

		return executionId;
	}
}
