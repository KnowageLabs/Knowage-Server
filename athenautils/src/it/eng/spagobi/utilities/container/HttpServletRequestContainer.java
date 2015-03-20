/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.container;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class HttpServletRequestContainer extends AbstractContainer {
	HttpServletRequest request;
	
	public HttpServletRequestContainer(HttpServletRequest request) {
		setRequest(request);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public boolean containsProperty(String propertyName) {
		return getProperty(propertyName) != null;
	}

	public Object getProperty(String propertyName) {
		return getRequest().getParameter( propertyName );
	}

	public void setProperty(String propertyName, Object propertyValue) {
		// TODO rise an unsupported operation exception		
	}
	
	
}
