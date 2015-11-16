/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

import it.eng.spago.base.SessionContainer;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * A wrapper of the Spago SessionContainer object. Inherits all it.eng.spagobi.container.AbstractContainer utility methods.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class SpagoBISessionContainer extends AbstractContainer implements
		IBeanContainer {

	static private Logger logger = Logger.getLogger(SpagoBISessionContainer.class);
	
	private SessionContainer _session;
	
	public SpagoBISessionContainer(SessionContainer session) {
		if (session == null) {
			logger.error("Session object is null!! Cannot initialize " + this.getClass().getName() + " instance");
			throw new ExceptionInInitializerError("SessionContainer session in input is null");
		}
		_session = session;
	}
	
	public void remove(String key) {
		logger.debug("IN: input key = [" + key + "]");
		if (key == null) {
			logger.warn("Input key is null!! Object will not be removed from session");
			return;
		}
		try {
			Object object = _session.getAttribute(key);
			if (object == null) {
				logger.warn("Object not found!!");
			} else {
				logger.debug("Found an existing object in session with key = [" + key + "]: it will be removed.");
				_session.delAttribute(key);
			}
		} finally {
			logger.debug("OUT");
		}

	}

	public void set(String key, Object object) {
		logger.debug("IN: input key = [" + key + "], object = [" + object + "]");
		if (key == null || object == null) {
			logger.warn("Input key or object is null!! Object will not be put on session");
			return;
		}
		try {
			Object previous = _session.getAttribute(key);
			if (previous == null) {
				_session.setAttribute(key, object);
			} else {
				logger.debug("Found an existing object in session with key = [" + key + "]: it will be overwritten.");
				_session.setAttribute(key, object);
			}
		} finally {
			logger.debug("OUT");
		}
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
			toReturn = _session.getAttribute(key);
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
		return _session.getAttributeNames();
	}

}
