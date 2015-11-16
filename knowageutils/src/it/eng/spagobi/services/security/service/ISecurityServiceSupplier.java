/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/*
 * Created on 21-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.services.security.service;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

/**
 * The interface for the User Profile Factory, in order to manage user information.
 */
public interface ISecurityServiceSupplier {
    
    /**
     * 
     * @return SpagoBIUserProfile
     */
	SpagoBIUserProfile createUserProfile(String userId);
	
	/**
     * if SpagoBIUserProfile is NULL the password is incorrect!!!!
     * @param userId
     * @param psw
     * @return
     */
    SpagoBIUserProfile checkAuthentication(String userId, String psw);
	
    /**
     * if SpagoBIUserProfile is NULL the token is incorrect!!!!
     * @param userId
     * @param token
     * @return
     * 
     * @deprecated
     */
     SpagoBIUserProfile checkAuthenticationWithToken(String userId, String token);
        
    /**
     * 
     * @param userId
     * @param function
     * @return
     * 
     * @deprecated
     */
     boolean checkAuthorization(String userId, String function); 
            
}