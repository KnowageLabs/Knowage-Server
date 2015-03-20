/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

import it.eng.spagobi.container.strategy.IContextRetrieverStrategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class provides useful methods to manage context on a ISessionContainer
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class ContextManager extends AbstractContainer implements IBeanContainer {
	
	private static final String _sessionAttributeBaseKey = "SPAGOBI_SESSION_ATTRIBUTE";
	protected IBeanContainer contextsContainer;
	protected IContextRetrieverStrategy contextRetrieverStrategy;
	
	static private Logger logger = Logger.getLogger(ContextManager.class);
	
	public ContextManager(IBeanContainer beanContainer, IContextRetrieverStrategy strategy) {
		logger.debug("IN");
		try {
			if (beanContainer == null)
				throw new ExceptionInInitializerError("Session in input is null");
			if (strategy == null)
				throw new ExceptionInInitializerError("Strategy in input is null");
			contextsContainer = beanContainer;
			contextRetrieverStrategy = strategy;
		} finally {
			logger.debug("OUT");
		}
	}
	
	/**
	 * Retrieves a generic object from context using the given key.
	 * @param key The key of the object in context
	 * @return The object in context with the given key
	 */
	public Object get(String key) {
		logger.debug("IN: input key = [" + key + "]");
		if (key == null) {
			logger.warn("Input key is null!! Returning null");
			return null;
		}
		Object toReturn = null;
		try {
			Context context = contextRetrieverStrategy.getContext(contextsContainer);
			if (context != null) {
				logger.debug("Context retrieved");
				toReturn = context.get(key);
				if (toReturn == null) {
					logger.debug("Object not found.");
				} else {
					logger.debug("Found object.");
				}
			} else {
				logger.debug("SpagoBISessionAttribute not retrieved");
			}
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}
	
	/**
	 * Sets a generic object into context using the given key.
	 * @param key The key to be used to store object in context
	 */
	public void set(String key, Object object) {
		logger.debug("IN: input key = [" + key + "], object = [" + object + "]");
		if (key == null || object == null) {
			logger.warn("Input key or object is null!! Object will not be put on session");
			return;
		}
		try {
			Context context = contextRetrieverStrategy.getContext(contextsContainer);
			if (context == null) {
				context = contextRetrieverStrategy.createContext(contextsContainer);
			}
			Object previous = context.get(key);
			if (previous == null) {
				context.set(key, object);
			} else {
				logger.debug("Found an existing object in session with key = [" + key + "]: it will be overwritten.");
				context.set(key, object);
			}
		} finally {
			logger.debug("OUT");
		}
	}
	
	/**
	 * Removes an object (given its key at input) from context.
	 * @param key The key of the object on context.
	 */
	public void remove(String key) {
		logger.debug("IN: input key = [" + key + "]");
		if (key == null) {
			logger.warn("Input key is null!! Object will not be removed from session");
			return;
		}
		try {
			Context context = contextRetrieverStrategy.getContext(contextsContainer);
			if (context != null) {
				Object object = context.get(key);
				if (object == null) {
					logger.warn("Object not found!!");
				} else {
					logger.debug("Found an existing object in context with key = [" + key + "]: it will be removed.");
					context.remove(key);
				}
			} else {
				logger.warn("Map not found!!");
			}
		} finally {
			logger.debug("OUT");
		}
	}
	
	/**
	 * Destroys current context.
	 */
	public void destroyCurrentContext() {
		logger.debug("IN");
		try {
			contextRetrieverStrategy.destroyCurrentContext(contextsContainer);
		} finally {
			logger.debug("OUT");
		}
	}
	
	/**
	 * Destroys all contexts older than the number of minutes specified at input.
	 */
	public void cleanOldContexts(int minutes) {
		logger.debug("IN");
		try {
			contextRetrieverStrategy.destroyContextsOlderThan(contextsContainer, minutes);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Returns a List of all the String keys of the objects stored on context.
	 */
	public List getKeys() {
		logger.debug("IN");
		List toReturn = new ArrayList();
		try {
			Context context = contextRetrieverStrategy.getContext(contextsContainer);
			if (context != null)
				toReturn = context.getKeys();
			return toReturn;
		} finally {
			logger.debug("OUT");
		}
	}
	/**
	 * Print all the contexts with all personal objects
	 */
	public void print() {
		List contextObjects = this.getKeys();
		for (int i=0; i<contextObjects.size(); i++){
			String attributeName = (String)contextObjects.get(i);
			Object attributeObject = contextsContainer.get(attributeName);
			//logger.debug("*** Context Object_ "+i + "  : "+ attributeName + " value: " +((attributeObject==null)?"":attributeObject.toString()));
			System.out.println("*** Context Object_ "+i + "  : "+ attributeName + " value: " +((attributeObject==null)?"":attributeObject.toString()));
		}
			
	}
}
