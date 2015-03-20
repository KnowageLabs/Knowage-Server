/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security;

import java.util.List;

/**
 * This is interface for gathering security information from portal server.
 * A Specific subclass exists for each portal server.
 */
public interface ISecurityInfoProvider {
	
	/**
	 * Gets the roles.
	 * 
	 * @return The Role list. (list of it.eng.spagobi.bo.Role)
	 */
	public List getRoles();
	

	/**
	 * Gets the list of names of all attributes of all profiles defined in the portal server.
	 * 
	 * @return the list of names of all attributes of all profiles defined in the portal server
	 */
	public List getAllProfileAttributesNames ();
	

}
