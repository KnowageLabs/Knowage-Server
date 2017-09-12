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
