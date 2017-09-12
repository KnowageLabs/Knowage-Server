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
