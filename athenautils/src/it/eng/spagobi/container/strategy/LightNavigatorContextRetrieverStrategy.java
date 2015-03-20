/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container.strategy;

import it.eng.spago.base.SourceBean;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spagobi.container.Context;
import it.eng.spagobi.container.IBeanContainer;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * <b>TO BE USED ONLY INSIDE SPAGOBI CORE, NOT INSIDE EXTERNAL ENGINES</b>.
 * This strategy create/retrieve/destroy the context using the LightNavigationManager.LIGHT_NAVIGATOR_ID attribute contained into 
 * the Spago request SourceBean object.
 * The context is put on ISessionContainer object with a key that has a fix part "SPAGOBI_SESSION_ATTRIBUTE" and a dynamic part, the 
 * LightNavigationManager.LIGHT_NAVIGATOR_ID request attribute; if this attribute is missing, the key used to put context on session is 
 * the static string "SPAGOBI_SESSION_ATTRIBUTE".
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class LightNavigatorContextRetrieverStrategy implements
		IContextRetrieverStrategy {

	static private Logger logger = Logger.getLogger(LightNavigatorContextRetrieverStrategy.class);
	
	private static final String _sessionAttributeBaseKey = "SPAGOBI_SESSION_ATTRIBUTE";
	private String _key;
	
	/**
	 * Look for the LightNavigationManager.LIGHT_NAVIGATOR_ID attribute on request to get the key for context storage on session.
	 * @param request The Spago SourceBean service request object
	 */
	public LightNavigatorContextRetrieverStrategy(SourceBean request) {
		logger.debug("IN");
		try {
			String lightNavigatorId = (String) request.getAttribute(LightNavigationManager.LIGHT_NAVIGATOR_ID);
			if (lightNavigatorId == null || lightNavigatorId.trim().equals("")) {
				logger.debug("Request does not contain light navigator id. Using fix base attribute key...");
				_key = _sessionAttributeBaseKey;
			} else {
				logger.debug("Light navigator id found on request: [" + lightNavigatorId + "]");
				_key = _sessionAttributeBaseKey + "_" + lightNavigatorId;
			}
		} finally {
			logger.debug("OUT");
		}
	}
	
	/**
	 * Retrieves the context from the input ISessionContainer instance
	 */
	public Context getContext(IBeanContainer sessionContainer) {
		logger.debug("IN");
		try {
			logger.debug("Looking at Context on session with key = [" + _key + "]");
			Context context = (Context) sessionContainer.get(_key);
			return context;
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Creates a new context and puts it on the input ISessionContainer instance
	 */
	public Context createContext(IBeanContainer sessionContainer) {
		logger.debug("IN");
		try {
			logger.debug("Creating a new context and putting on session with key = [" + _key + "]");
			Context context = new Context();
			sessionContainer.set(_key, context);
			return context;
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Destroys the current context on the input ISessionContainer instance
	 */
	public void destroyCurrentContext(IBeanContainer sessionContainer) {
		logger.debug("IN");
		try {
			Context context = (Context) sessionContainer.get(_key);
			if (context != null) {
				sessionContainer.remove(_key);
			} else {
				logger.warn("Context not found!!");
			}
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Destroys all the contexts on the input ISessionContainer instance older than the number of minutes specified at input.
	 */
	public void destroyContextsOlderThan(IBeanContainer session,
			int minutes) {
		logger.debug("IN");
		try {
			synchronized (session) {
				List attributeNames = session.getKeys();
				Iterator it = attributeNames.iterator();
				while (it.hasNext()) {
					String attributeName = (String) it.next();
					if (!attributeName.startsWith(_sessionAttributeBaseKey)) {
						Object attributeObject = session.get(attributeName);
						if (attributeObject instanceof Context) {
							Context context = (Context) attributeObject;
							if (context.isOlderThan(minutes)) {
								logger.debug("Deleting context instance with last usage date = [" + context.getLastUsageDate() + "]");
								session.remove(attributeName);
							}
						} else {
							logger.debug("Session attribute with key [" + attributeName + "] is not a Context object; cannot delete it.");
						}
					}
				}
			}
		} finally {
			logger.debug("OUT");
		}
	}

}
