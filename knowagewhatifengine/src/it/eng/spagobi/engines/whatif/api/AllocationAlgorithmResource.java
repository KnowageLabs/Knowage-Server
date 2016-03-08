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
