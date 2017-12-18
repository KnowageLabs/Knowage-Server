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
