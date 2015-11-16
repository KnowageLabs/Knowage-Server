/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.bo;

import java.security.Principal;

public class SpagoBIPrincipal implements Principal {

	String userName = "";
	
	/**
	 * Instantiates a new spago bi principal.
	 * 
	 * @param userName the user name
	 */
	public SpagoBIPrincipal(String userName) {
		this.userName = userName;
	}
	
	/* (non-Javadoc)
	 * @see java.security.Principal#getName()
	 */
	public String getName() {
		return this.userName;
	}
	
}