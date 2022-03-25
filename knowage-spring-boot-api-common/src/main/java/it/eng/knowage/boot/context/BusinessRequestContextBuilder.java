/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.boot.context;

import it.eng.spagobi.services.security.SpagoBIUserProfile;

/**
 * @author Marco Libanori
 */
public class BusinessRequestContextBuilder {

	private BusinessRequestContext businessRequestContext;

	private BusinessRequestContextBuilder() {
	}

	public static BusinessRequestContextBuilder newInstance() {
		return new BusinessRequestContextBuilder();
	}

	public BusinessRequestContextBuilder forBrandNew(String version) {

		SpagoBIUserProfile userProfile = new SpagoBIUserProfile();

		businessRequestContext = new BusinessRequestContext(version);

		businessRequestContext.setUserProfile(userProfile);

		return this;
	}

	public BusinessRequestContextBuilder editExistingOne(BusinessRequestContext existing) {

		businessRequestContext = existing;

		if (existing.getUserProfile() == null) {
			SpagoBIUserProfile userProfile = new SpagoBIUserProfile();

			businessRequestContext.setUserProfile(userProfile);
		}

		return this;
	}

	// TODO: remove gallery element
	public BusinessRequestContextBuilder initForInitializer() {

		withUserId("initializer");
		withUsername("initializer");
		withOrganization("DEFAULT_TENANT");
		withFunction("WidgetGalleryManagement");

		return this;
	}

	public BusinessRequestContextBuilder withOrganization(String organization) {

		SpagoBIUserProfile userProfile = businessRequestContext.getUserProfile();

		userProfile.setOrganization(organization);
		businessRequestContext.setOrganization(organization);

		return this;
	}

	public BusinessRequestContextBuilder withUsername(String username) {

		SpagoBIUserProfile userProfile = businessRequestContext.getUserProfile();

		userProfile.setUserName(username);
		businessRequestContext.setUsername(username);

		return this;
	}

	public BusinessRequestContextBuilder withUserId(String userId) {

		SpagoBIUserProfile userProfile = businessRequestContext.getUserProfile();

		userProfile.setUserId(userId);

		return this;
	}

	public BusinessRequestContextBuilder withFunction(String function) {

		SpagoBIUserProfile userProfile = businessRequestContext.getUserProfile();

		userProfile.getFunctions().add(function);

		return this;
	}

	public BusinessRequestContext build() {
		return businessRequestContext;
	}
}
