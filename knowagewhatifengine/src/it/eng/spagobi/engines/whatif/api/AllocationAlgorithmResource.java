/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 * 
 * @class AllocationAlgorithmResource
 * 
 * Provides services to manage the allocation algorithms.

 * 
 */
package it.eng.spagobi.engines.whatif.api;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.AllocationAlgorithmDefinition;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.AllocationAlgorithmSingleton;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;

@Path("/1.0/allocationalgorithm")
public class AllocationAlgorithmResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(AllocationAlgorithmResource.class);

	private Collection<AllocationAlgorithmDefinition> allocationAlgorithmDefinitions = null;

	/**
	 * Loads the lost of available allocation algorithms
	 * 
	 * @return
	 */
	@GET
	@Produces("text/html; charset=UTF-8")
	public String getAvailabeAllocationAlgorithms() {
		logger.debug("IN");

		if (allocationAlgorithmDefinitions == null) {
			allocationAlgorithmDefinitions = new ArrayList<AllocationAlgorithmDefinition>();

			Map<String, AllocationAlgorithmDefinition> allocationAlgorithms = AllocationAlgorithmSingleton.getInstance().getAllocationAlgorithms();

			if (allocationAlgorithms != null) {
				logger.debug("Successfully loaded " + allocationAlgorithms.size() + " algorithms from configuration");
				allocationAlgorithmDefinitions = allocationAlgorithms.values();
			} else {
				logger.error("No allocation algorithm defined");
			}
		}

		try {
			String toReturn = serialize(allocationAlgorithmDefinitions);
			logger.debug("OUT");
			return toReturn;
		} catch (Exception e) {
			logger.error("Error serializing the list of available allocation algorithms", e);
			throw new SpagoBIEngineRestServiceRuntimeException("sbi.olap.writeback.algorithm.definition.list.error", getLocale(),
					"Error serializing the list of available allocation algorithms", e);
		}
	}

	@POST
	@Path("/{algorithmClassName}")
	@Produces("text/html; charset=UTF-8")
	public String setUsedAlgorithm(@PathParam("algorithmClassName") String algorithmClassName) {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		ei.setAlgorithmInUse(algorithmClassName);
		logger.debug("OUT");
		return getJsonSuccess();
	}
}
