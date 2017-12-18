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
