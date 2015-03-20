/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.behaviouralmodel.lov.bo;

import it.eng.spago.security.IEngUserProfile;

import java.util.List;


public interface IJavaClassLov {

	/**
	 * Gets the values formatted into an xml structure.
	 * 
	 * @param profile a user profile used to fill attributes required by the query
	 * 
	 * @return the xml string of the values
	 */
	public String getValues(IEngUserProfile profile);
	
	
	/**
	 * Gets the list of profile attribute names required by the class.
	 * 
	 * @return the list of profile attribute names
	 */
	public List getNamesOfProfileAttributeRequired();
}
