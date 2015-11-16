/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
