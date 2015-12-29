/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Giulio Gavardi (giulio.gavardi@eng.it) 
 * 
 * @class DBWriteResource
 * 
 * Provides services to manage the axis resource
 * 
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

import com.eyeq.pivot4j.PivotModel;

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
