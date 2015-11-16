/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.security.exceptions;

public class SecurityException extends Exception {

		/**
		 * Instantiates a new security exception.
		 * 
		 * @param msg the msg
		 */
		public SecurityException(String msg) {
			super(msg);
		}
		
		/**
		 * Instantiates a new security exception.
		 */
		public SecurityException(String msg, Throwable e) {
			super(msg, e);
		}
		
		/**
		 * @deprecated add always a descriptive message to the exception
		 */
		private SecurityException() {
			super();
		}
}
