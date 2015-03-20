/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.utils;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;


/** 
 * TODO : move it in it.eng.spagobi.tools.dataset and rename to DataSetProfiler (or similar)
 */
public class DataSetUtilities {

	/**
	 * Check if the dataset is executable by the user
	 * @param dataset
	 * @param owner
	 * @param isAdminUser
	 * @return
	 */
	public static boolean isExecutableByUser(IDataSet dataset, IEngUserProfile profile){
		if(profile==null){
			return false;
		}
		boolean isAdminUser = isAdministrator(profile);
		if(isAdminUser){
			return true;
		}
		String owner = profile.getUserUniqueIdentifier().toString();
		
		return (dataset.isPublic() || (!owner.equals(null) && owner.equals(dataset.getOwner()) ) );
	} 
	
	
	public static boolean isAdministrator (IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		try {
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)){
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	} 
	
}
