/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2008 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
package it.eng.spago.navigation;

import it.eng.spago.base.SourceBean;

// TODO: Auto-generated Javadoc
/**
 * The Class MarkedRequest.
 */
public class MarkedRequest {

	/** The request. */
	private SourceBean request;
	
	/** The mark. */
	private String mark;
	
	/**
	 * Instantiates a new marked request.
	 * 
	 * @param request the request
	 * @param mark the mark
	 */
	public MarkedRequest (SourceBean request, String mark) {
		this.request = request;
		this.mark = mark;
	}

	/**
	 * Gets the mark.
	 * 
	 * @return the mark
	 */
	public String getMark() {
		return mark;
	}

	/**
	 * Sets the mark.
	 * 
	 * @param mark the new mark
	 */
	public void setMark(String mark) {
		this.mark = mark;
	}

	/**
	 * Gets the request.
	 * 
	 * @return the request
	 */
	public SourceBean getRequest() {
		return request;
	}

	/**
	 * Sets the request.
	 * 
	 * @param request the new request
	 */
	public void setRequest(SourceBean request) {
		this.request = request;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String toReturn = "";
		toReturn += "Request SourceBean " + (request == null ? "null." : " = " + request.toString());
		toReturn += "Request Mark = '" + (mark == null ? "" : mark) + "'";
		
		return toReturn;
	}
	
}
