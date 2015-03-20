/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.security;


import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;


/**
 * This is the SecurityService interfaces
 * @author Bernabei Angelo
 *
 */
public interface SecurityService {

    	/**
    	 * return the user profile informations
    	 * @param token
    	 * @return
    	 */
        SpagoBIUserProfile getUserProfile(String token,String userId);
	
	/**
	 * Check if the user can access to the path
	 * @param token
	 * @param idFolder ( object tree )
	 * @param mode
	 * @return
	 */
	boolean isAuthorized(String token,String userId,String idFolder,String mode);
	
	/**
	 * check if the user can access to this function 
	 * @param token
	 * @param function
	 * @return
	 */
	boolean checkAuthorization(String token,String userId,String function);	
}
