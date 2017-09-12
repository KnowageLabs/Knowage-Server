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
