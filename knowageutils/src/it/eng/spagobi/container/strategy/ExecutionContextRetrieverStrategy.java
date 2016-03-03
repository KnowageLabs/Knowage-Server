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
package it.eng.spagobi.container.strategy;

import it.eng.spagobi.container.Context;
import it.eng.spagobi.container.IBeanContainer;
import it.eng.spagobi.container.IContainer;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * <b>TO BE USED ONLY INSIDE EXTERNAL ENGINES BASED ON SPAGO FRAMEWORK, NOT INSIDE SPAGOBI CORE</b>.
 * This strategy create/retrieve/destroy the context using the "SBI_EXECUTION_ID" attribute contained into the Spago request 
 * SourceBean object.
 * The context is put on ISessionContainer object with a key that has a fix part "SPAGOBI_SESSION_ATTRIBUTE" and a dynamic part, the 
 * "SBI_EXECUTION_ID" request attribute; if this attribute is missing, the key used to put context on session is 
 * the static string "SPAGOBI_SESSION_ATTRIBUTE".
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class ExecutionContextRetrieverStrategy implements IContextRetrieverStrategy {

	static private Logger logger = Logger.getLogger(ExecutionContextRetrieverStrategy.class);
	
	private static final String SPAGOBI_SESSION_ATTRIBUTE = "SPAGOBI_SESSION_ATTRIBUTE";
	public static final String EXECUTION_ID = "SBI_EXECUTION_ID";
	
	private String contextId;
	
	/**
	 * Look for the "SBI_EXECUTION_ID" attribute on request to get the key for context storage on session.
	 * @param requestContainer The Spago SourceBean service request object
	 */
	public ExecutionContextRetrieverStrategy(IContainer requestContainer) {
		this( requestContainer.getString(EXECUTION_ID) );
	}
	
	public ExecutionContextRetrieverStrategy(String contextId) {
		if (contextId == null || contextId.trim().equals("")) {
			logger.debug("Request container does not [" + EXECUTION_ID + "] parameter. Using fix base attribute key...");
			this.contextId = SPAGOBI_SESSION_ATTRIBUTE;
		} else {
			logger.debug("[" + EXECUTION_ID + "] parameter found on request: [" + contextId + "]");
			this.contextId = SPAGOBI_SESSION_ATTRIBUTE + "_" + contextId;
		}
	}
	
	/**
	 * Retrieves the context from the input IBeanContainer instance
	 */
	public Context getContext(IBeanContainer contextsContainer) {
		Context context;
		
		logger.debug("IN");
		
		context = null;
		try {
			logger.debug("Looking for context [" + contextId + "]");
			context = (Context) contextsContainer.get( contextId );
		} finally {
			logger.debug("OUT");
		}
		
		return context;
	}

	/**
	 * Creates a new context and puts it on the input IBeanContainer instance
	 */
	public Context createContext(IBeanContainer sessionContainer) {
		Context context;
		
		logger.debug("IN");
		
		context = null;
		try {
			logger.debug("Creating a new context and putting on session with key = [" + contextId + "]");
			context = new Context();
			sessionContainer.set(contextId, context);
		} finally {
			logger.debug("OUT");
		}
		
		return context;
	}

	/**
	 * Destroys the current context on the input IBeanContainer instance
	 */
	public void destroyCurrentContext(IBeanContainer sessionContainer) {
		Context context;
		
		logger.debug("IN");
		
		try {
			context = (Context) sessionContainer.get(contextId);
			if (context != null) {
				sessionContainer.remove(contextId);
			} else {
				logger.warn("Context [" + contextId + "] not found");
			}
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Destroys all the contexts on the input IBeanContainer instance older than the number of minutes specified at input.
	 */
	public void destroyContextsOlderThan(IBeanContainer session, int minutes) {
		logger.debug("IN");
		try {
			synchronized (session) {
				List attributeNames = session.getKeys();
				Iterator it = attributeNames.iterator();
				while (it.hasNext()) {
					String attributeName = (String) it.next();
					if (!attributeName.startsWith(SPAGOBI_SESSION_ATTRIBUTE)) {
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
