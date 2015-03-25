/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;


import java.util.Map;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.container.strategy.IContextRetrieverStrategy;


import org.apache.log4j.Logger;

/**
 * This class provides useful methods to manage context on a ISessionContainer
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class CoreContextManager extends ContextManager {




	static private Logger logger = Logger.getLogger(CoreContextManager.class);

	public CoreContextManager(IBeanContainer beanContainer, IContextRetrieverStrategy strategy) {
		super(beanContainer,strategy);
		logger.debug("IN");

	}
	/**
	 * <b>TO BE USED ONLY INSIDE SPAGOBI CORE, NOT INSIDE EXTERNAL ENGINES</b>.
	 * Return the BIObject associated with the input key.
	 * If the key is associated to an object that is not a BIObject instance, a ClassCastException is thrown.
	 * 
	 * @param key The input key
	 * @return the BIObject associated with the input key.
	 */

	public BIObject getBIObject(String key) {
		logger.debug("IN");
		BIObject toReturn = null;
		try {
			Object object = get(key);
			toReturn = (BIObject) object;
			return toReturn; 
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * <b>TO BE USED ONLY INSIDE SPAGOBI CORE, NOT INSIDE EXTERNAL ENGINES</b>.
	 * Return the ExecutionInstance associated with the input key.
	 * If the key is associated to an object that is not a ExecutionInstance instance, a ClassCastException is thrown.
	 * 
	 * @param key The input key
	 * @return the ExecutionInstance associated with the input key.
	 */
	public ExecutionInstance getExecutionInstance(String key) {
		logger.debug("IN");
		ExecutionInstance toReturn = null;
		try {
			Object object = get(key);
			toReturn = (ExecutionInstance) object;
			return toReturn; 
		} finally {
			logger.debug("OUT");
		}
	}

	/** In case of massive Export a map of execution instances is returned, each associated with the biObjId referring
	 * 
	 * @param key
	 * @return
	 */

	public Map<Integer, ExecutionInstance> getExecutionInstancesAsMap(String key) {
		logger.debug("IN");
		Map<Integer, ExecutionInstance> toReturn = null;
		try {
			Object object = get(key);
			toReturn = (Map<Integer, ExecutionInstance>) object;
			return toReturn; 
		} finally {
			logger.debug("OUT");
		}
	}

	
	/** used to know which function to be used to retrieve execution instance;
	 *  could not change the original function because it is used everywhere
	 * @param key
	 * @return
	 */
	public Boolean isExecutionInstanceAMap(String key) {
		logger.debug("IN");
		boolean toReturn = false;
		try {
			Object object = get(key);
			if(object == null){
				logger.warn("No object found with key "+key);
				return null;
			}
			toReturn = object instanceof Map;
			return toReturn; 
		} finally {
			logger.debug("OUT");

		}
	}

}