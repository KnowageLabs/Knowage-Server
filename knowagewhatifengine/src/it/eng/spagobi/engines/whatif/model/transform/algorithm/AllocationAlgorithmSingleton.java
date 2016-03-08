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

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class AllocationAlgorithmSingleton {

	private static AllocationAlgorithmSingleton instance = null;
	private static transient Logger logger = Logger.getLogger(SingletonConfig.class);

	Map<String, AllocationAlgorithmDefinition> allocationAlgorithms;

	public synchronized static AllocationAlgorithmSingleton getInstance() {
		try {
			if (instance == null) {
				instance = new AllocationAlgorithmSingleton();
			}
		} catch (Exception e) {
			logger.debug("Impossible to load configuration", e);
		}
		return instance;
	}

	private AllocationAlgorithmSingleton() {
		logger.debug("Loading the algorithms");
		allocationAlgorithms = WhatIfEngineConfig.getInstance().getAllocationAlgorithms();
		logger.debug("OUT");
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
