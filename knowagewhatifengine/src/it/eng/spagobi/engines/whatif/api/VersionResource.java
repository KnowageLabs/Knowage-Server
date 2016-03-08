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

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.serializer.SerializationException;
import it.eng.spagobi.engines.whatif.version.SbiVersion;
import it.eng.spagobi.engines.whatif.version.VersionManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.Logger;
import org.pivot4j.PivotModel;

@Path("/1.0/version")
public class VersionResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(VersionResource.class);

	private VersionManager versionManager;

	private VersionManager getVersionBusiness() {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();

		if (versionManager == null) {
			versionManager = new VersionManager(ei);
		}
		return versionManager;
	}

	/**
	 * Load the list of versions
	 *
	 * @return the serialization of the versions
	 */
	@GET
	public String getAllVersions() {
		logger.debug("IN");

		List<SbiVersion> versions = getVersionBusiness().getAllVersions();

		logger.debug("OUT");
		String serializedVersions;
		try {
			serializedVersions = serialize(versions);
		} catch (SerializationException e) {
			logger.error("Error serializing versions");
			throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e);
		}

		return serializedVersions;

	}

	/**
	 * Delete the versions
	 *
	 * @param versionsToDelete
	 *            its the serialization of a list with the ids of the version to
	 *            remove. Example. "1 , 2, 4"
	 * @return
	 */
	@POST
	@Path("/delete/{versionsToDelete}")
	public String increaseVersion(@PathParam("versionsToDelete") String versionsToDelete) {
		logger.debug("IN");
		getVersionBusiness().deleteVersions(versionsToDelete);
		logger.debug("OUT");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();
		String table = renderModel(model);
		logger.debug("OUT");
		return table;
	}

}
