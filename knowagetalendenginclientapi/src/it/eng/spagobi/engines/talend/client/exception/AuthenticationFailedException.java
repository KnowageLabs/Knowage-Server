/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.engines.talend.client.exception;


/**
 * @author Andrea Gioia
 *
 */
public class AuthenticationFailedException extends TalendEngineClientException {
	
	/**
	 * Instantiates a new authentication failed exception.
	 */
	public AuthenticationFailedException() {}
	
	/**
	 * Instantiates a new authentication failed exception.
	 * 
	 * @param msg the msg
	 */
	public AuthenticationFailedException(String msg) {
		super(msg);
	}
}
