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
