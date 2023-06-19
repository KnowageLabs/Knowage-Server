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
package it.eng.spagobi.engines.whatif.model.transform.algorithm;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;

public class AllocationAlgorithmSingleton {

	private static final Logger LOGGER = Logger.getLogger(AllocationAlgorithmSingleton.class);
	private static AllocationAlgorithmSingleton INSTANCE = null;

	Map<String, AllocationAlgorithmDefinition> allocationAlgorithms;

	public static synchronized AllocationAlgorithmSingleton getInstance() {
		try {
			if (INSTANCE == null) {
				INSTANCE = new AllocationAlgorithmSingleton();
			}
		} catch (Exception e) {
			LOGGER.debug("Impossible to load configuration", e);
		}
		return INSTANCE;
	}

	private AllocationAlgorithmSingleton() {
		LOGGER.debug("Loading the algorithms");
		allocationAlgorithms = WhatIfEngineConfig.getInstance().getAllocationAlgorithms();
		LOGGER.debug("OUT");
	}

	/**
	 * Gets the list of allocation algorithms
	 *
	 * @return
	 */
	public Map<String, AllocationAlgorithmDefinition> getAllocationAlgorithms() {
		return allocationAlgorithms;
	}

	/**
	 * Loads the default propagation algorithm
	 *
	 * @return
	 * @throws NoAllocationAlgorithmFoundException
	 */
	public AllocationAlgorithmDefinition getDefaultAllocationAlgorithm() throws NoAllocationAlgorithmFoundException {
		if (allocationAlgorithms != null) {
			Iterator<AllocationAlgorithmDefinition> algIter = allocationAlgorithms.values().iterator();
			while (algIter.hasNext()) {
				AllocationAlgorithmDefinition aAllocationAlgorithmDefinition = algIter.next();
				if (aAllocationAlgorithmDefinition.isDefaultAlgorithm()) {
					return aAllocationAlgorithmDefinition;
				}
			}
		}
		throw new NoAllocationAlgorithmFoundException("No default algorithm found");
	}

}
