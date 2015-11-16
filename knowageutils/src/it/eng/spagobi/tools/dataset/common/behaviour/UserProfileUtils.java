/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.behaviour;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class UserProfileUtils {
	
	private static transient Logger logger = Logger.getLogger(UserProfileUtils.class);

	
	/**
	 * Gets the all profile attributes (Also present in GeneralUtilities).
	 *
	 * TODO: centralization of two methods
	 * 
	 * @param profile the profile
	 * 
	 * @return the all profile attributes
	 */
	public static Map getProfileAttributes(IEngUserProfile profile) {		
		Map profileAttributes;
		
		Assert.assertNotNull(profile, "Parameter [profile] cannot be null");
		
		logger.debug("IN");
		try {
			profileAttributes = new HashMap();
			Collection attributeNames = profile.getUserAttributeNames();
			if (attributeNames == null || attributeNames.size() == 0) {
				return profileAttributes;
			}
			
			Iterator it = attributeNames.iterator();
			while (it.hasNext()) {
				Object attributeName = it.next();
				Object attributeValue = profile.getUserAttribute(attributeName.toString());
				profileAttributes.put(attributeName, attributeValue);
			}
		
			return profileAttributes;
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to read attributes from the profile of user [" + profile.getUserUniqueIdentifier() +"]");
		} finally {
			logger.debug("OUT");
		}
	}
}
