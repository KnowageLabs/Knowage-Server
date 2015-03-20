/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;



import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIHttpSessionContainer  
extends AbstractContainer implements IBeanContainer {
	
	private HttpSession session;
	
	static private Logger logger = Logger.getLogger(SpagoBIHttpSessionContainer.class);
	
	
	public SpagoBIHttpSessionContainer(HttpSession session) {
		if (session == null) {
			logger.error("Session object is null." +
					" Cannot initialize " + this.getClass().getName() + " instance");
			throw new ExceptionInInitializerError("HttpSession session in input is null");
		}
		
		setSession( session );
		
	}
	
	public HttpSession getSession() {
		return session;
	}

	private void setSession(HttpSession session) {
		this.session = session;
	}

	public Object get(String key) {
		logger.debug("IN: input key = [" + key + "]");
		if (key == null) {
			logger.warn("Input key is null!! Returning null");
			return null;
		}
		Object toReturn = null;
		try {
			logger.debug("SpagoBISessionAttribute retrieved");
			toReturn = getSession().getAttribute(key);
			if (toReturn == null) {
				logger.debug("Object not found.");
			} else {
				logger.debug("Found object.");
			}
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	public List getKeys() {		
		return Collections.list( getSession().getAttributeNames() );
	}

	public void remove(String key) {
		logger.debug("IN: input key = [" + key + "]");
		if (key == null) {
			logger.warn("Input key is null!! Object will not be removed from session");
			return;
		}
		try {
			Object object = getSession().getAttribute( key );
			if (object == null) {
				logger.warn("Object not found!!");
			} else {
				logger.debug("Found an existing object in session with key = [" + key + "]: it will be removed.");
				getSession().removeAttribute(key);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	public void set(String key, Object value) {
		logger.debug("IN: input key = [" + key + "], object = [" + value + "]");
		if (key == null || value == null) {
			logger.warn("Input key or object is null!! Object will not be put on session");
			return;
		}
		try {
			Object previous = getSession().getAttribute(key);
			if (previous == null) {
				getSession().setAttribute(key, value);
			} else {
				logger.debug("Found an existing object in session with key = [" + key + "]: it will be overwritten.");
				getSession().setAttribute(key, value);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	

}
