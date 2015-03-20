/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIServletRequestContainer 
	extends AbstractContainer implements IReadOnlyContainer {

static private Logger logger = Logger.getLogger(SpagoBIRequestContainer.class);
	
	ServletRequest request;
	
	public SpagoBIServletRequestContainer(ServletRequest request) {
		if (request == null) {
			logger.error("ServletRequest is null. " +
					"Cannot initialize " + this.getClass().getName() + "  instance");
			throw new ExceptionInInitializerError("ServletRequest request in input is null");
		}
		setRequest( request );
	}

	private ServletRequest getRequest() {
		return request;
	}

	private void setRequest(ServletRequest request) {
		this.request = request;
	}
	
	public Object get(String key) {
		return getRequest().getParameter(key);
	}

	public List getKeys() {
		return Collections.list( getRequest().getParameterNames() );
	}

	public void remove(String key) {
		throw new UnsupportedOperationException ("Impossible to write in a ReadOnlyContainer");	
	}

	public void set(String key, Object value) {
		throw new UnsupportedOperationException ("Impossible to write in a ReadOnlyContainer");	
	}
}
