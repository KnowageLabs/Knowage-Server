/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 */
package it.eng.spagobi.engines.whatif.model.transform.algorithm;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.util.Map;

import org.apache.log4j.Logger;

public class AllocationAlgorithmFactory {

	public static transient Logger logger = Logger.getLogger(AllocationAlgorithmFactory.class);

	/**
	 * Creating an instance of IAllocationAlgorithm
	 * 
	 * @param algorithmName
	 *            the name of the algorithm
	 * @param ei
	 *            the engine istance
	 * @param properties
	 *            the properties of the algorithm
	 * @return
	 * @throws SpagoBIEngineException
	 */
	public static IAllocationAlgorithm getAllocationAlgorithm(String algorithmName, WhatIfEngineInstance ei, Map<String, Object> properties) throws SpagoBIEngineException {
		Map<String, AllocationAlgorithmDefinition> allocationAlgorithms = AllocationAlgorithmSingleton.getInstance().getAllocationAlgorithms();
		IAllocationAlgorithm algorithm;

		logger.debug("Creating the IAllocationAlgorithm with name " + algorithmName);
		AllocationAlgorithmDefinition definition = allocationAlgorithms.get(algorithmName);

		if (definition != null) {
			try {
				algorithm = (IAllocationAlgorithm) Class.forName(definition.getClassName()).newInstance();
				logger.debug("Creating the IAllocationAlgorithm with class " + definition.getClassName());
			} catch (Exception e) {
				logger.error("Error creating the IAllocationAlgorithm. The name of the algorithm is [" + algorithmName + "] and the class is [" + definition.getClassName() + "]");
				throw new SpagoBIEngineException("Error creating the IAllocationAlgorithm. The name of the algorithm is [" + algorithmName + "] and the class is ["
						+ definition.getClassName() + "]", e);
			}

			algorithm.setProperties(properties);
		} else {
			logger.debug("No algorithm found with name " + algorithmName + ". Using the proportional.");
			algorithm = new DefaultWeightedAllocationAlgorithm(ei);
		}

		return algorithm;
	}
}
