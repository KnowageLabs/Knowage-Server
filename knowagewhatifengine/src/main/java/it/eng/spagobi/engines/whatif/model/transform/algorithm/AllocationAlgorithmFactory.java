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
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

public class AllocationAlgorithmFactory {

	public static transient Logger logger = Logger.getLogger(AllocationAlgorithmFactory.class);

	/**
	 * Creating an instance of IAllocationAlgorithm
	 *
	 * @param algorithmClassName the name of the algorithm
	 * @param ei                 the engine istance
	 * @param properties         the properties of the algorithm
	 * @return
	 * @throws SpagoBIEngineException
	 */
	public static IAllocationAlgorithm getAllocationAlgorithm(String algorithmClassName, WhatIfEngineInstance ei, Map<String, Object> properties)
			throws SpagoBIEngineException {
		Map<String, AllocationAlgorithmDefinition> allocationAlgorithms = AllocationAlgorithmSingleton.getInstance().getAllocationAlgorithms();
		IAllocationAlgorithm algorithm;

		logger.debug("Creating the IAllocationAlgorithm with name " + algorithmClassName);
		AllocationAlgorithmDefinition definition = getAllocationAlgorithByClassName(allocationAlgorithms, algorithmClassName);

		if (definition != null) {
			try {
				algorithm = (IAllocationAlgorithm) Class.forName(definition.getClassName()).newInstance();
				logger.debug("Creating the IAllocationAlgorithm with class " + definition.getClassName());
			} catch (Exception e) {
				logger.error("Error creating the IAllocationAlgorithm. The class of the algorithm is [" + definition.getClassName() + "]");
				throw new SpagoBIEngineException("Error creating the IAllocationAlgorithm. The class of the algorithm is [" + definition.getClassName() + "]",
						e);
			}

			algorithm.setProperties(properties);
		} else {
			logger.debug("No algorithm found with class " + algorithmClassName + ". Using the proportional.");
			algorithm = new DefaultWeightedAllocationAlgorithm(ei);
		}

		return algorithm;
	}

	private static AllocationAlgorithmDefinition getAllocationAlgorithByClassName(Map<String, AllocationAlgorithmDefinition> allocationAlgorithms,
			String algorithmClassName) {
		Iterator<Entry<String, AllocationAlgorithmDefinition>> it = allocationAlgorithms.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, AllocationAlgorithmDefinition> entry = it.next();
			AllocationAlgorithmDefinition value = entry.getValue();
			if (value.getClassName().equals(algorithmClassName)) {
				return value;
			}
		}
		return null;
	}
}
