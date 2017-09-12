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
package it.eng.spagobi.engines.talend.client.exception;

import org.apache.commons.httpclient.StatusLine;


/**
 * @author Andrea Gioia
 *
 */
public class ServiceInvocationFailedException extends TalendEngineClientException {
	private String statusLine;
	private String responseBody;
	
	/**
	 * Instantiates a new service invocation failed exception.
	 */
	public ServiceInvocationFailedException() {}
	
	/**
	 * Instantiates a new service invocation failed exception.
	 * 
	 * @param msg the msg
	 */
	public ServiceInvocationFailedException(String msg) {
		super(msg);
	}
	
	/**
	 * Instantiates a new service invocation failed exception.
	 * 
	 * @param msg the msg
	 * @param statusLine the status line
	 * @param responseBody the response body
	 */
	public ServiceInvocationFailedException(String msg, String statusLine, String responseBody) {
		super(msg);
		this.statusLine = statusLine;
		this.responseBody = responseBody;
	}

	/**
	 * Gets the response body.
	 * 
	 * @return the response body
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * Gets the status line.
	 * 
	 * @return the status line
	 */
	public String getStatusLine() {
		return statusLine;
	}
}
