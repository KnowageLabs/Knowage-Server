/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.boot.factory;

import it.eng.knowage.boot.factory.SecurityServiceFactory;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;
import it.eng.spagobi.services.security.SpagoBIUserProfile.Attributes;
import it.eng.spagobi.services.security.SpagoBIUserProfile.Attributes.Entry;

public class SecurityServiceFactoryTest extends SecurityServiceFactory {

	@Override
	public Class<?> getObjectType() {
		return SecurityServiceService.class;
	}

	@Override
	protected SecurityServiceService createInstance() throws Exception {
		return new SecurityServiceService() {

			@Override
			public boolean isAuthorized(String arg0, String arg1, String arg2, String arg3) {
				return true;
			}

			@Override
			public SpagoBIUserProfile getUserProfile(String arg0, String arg1) {
				SpagoBIUserProfile profile = new SpagoBIUserProfile();

				Attributes attributes = new Attributes();

				Entry entry = new Entry();

				entry.setKey("test");
				entry.setValue("test");

				attributes.getEntry().add(entry);

				profile.setAttributes(attributes);
				profile.setIsSuperadmin(true);
				profile.setOrganization("DEFAULT");
				profile.setUniqueIdentifier("biadmin");
				profile.setUserId("biadmin");
				profile.setUserName("biadmin");

				return profile;
			}

			@Override
			public boolean checkAuthorization(String arg0, String arg1, String arg2) {
				return true;
			}
		};
	}

}
