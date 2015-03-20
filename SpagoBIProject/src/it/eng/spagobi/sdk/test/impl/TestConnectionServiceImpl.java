/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.test.impl;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.test.TestConnectionService;

import org.apache.log4j.Logger;

public class TestConnectionServiceImpl extends AbstractSDKService implements TestConnectionService {

	static private Logger logger = Logger.getLogger(TestConnectionServiceImpl.class);

	public boolean connect() {
		boolean toReturn = false;
        logger.debug("IN");
        try {
        	IEngUserProfile profile = getUserProfile();
        	if (profile != null) {
        		UserProfile userProfile = (UserProfile) profile;
        		logger.info("User recognized: " +
        				"userUniqueIdentifier = [" + userProfile.getUserUniqueIdentifier() + "]; " +
        						"userId = [" + userProfile.getUserId() + "]; " +
        								"userName = [" + userProfile.getUserName() + "]");
        		toReturn = true;
        	} else {
        		logger.error("User not recognized.");
        		toReturn = false;
        	}
        } catch(Exception e) {
            logger.error("Error while creating user profile object", e);
        }
        logger.debug("OUT");
        return toReturn;
	}
	

}
