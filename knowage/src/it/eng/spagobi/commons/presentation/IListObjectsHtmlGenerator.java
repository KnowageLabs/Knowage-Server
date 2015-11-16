/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation;

import it.eng.spago.paginator.basic.ListIFace;

import javax.servlet.http.HttpServletRequest;

/**
 * The interface for classes generating the HTML list objects.
 * 
 * @author sulis
 */
public interface IListObjectsHtmlGenerator {
	
	/**
	 * The interface for the <code>makeList</code> method.
	 * 
	 * @param list the interface object list at input
	 * @param httpRequest The request http
	 * @param listPage String for paging navigation
	 * 
	 * @return the string buffer with HTML code
	 */
	public StringBuffer makeList(ListIFace list, HttpServletRequest httpRequest, String listPage);
	
}
