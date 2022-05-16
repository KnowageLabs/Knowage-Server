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

import java.util.Locale;
import java.util.UUID;

import it.eng.spagobi.services.security.SpagoBIUserProfile;

/**
 * Holds business data from the request.
 *
 * @author Marco Libanori
 */
public class BusinessRequestContext {

	private UUID correlationId = UUID.randomUUID();

	private final String version;

	private String username;

	private String organization;

	private SpagoBIUserProfile userProfile;

	private String userToken;

	private Locale locale;

	public BusinessRequestContext(String version) {
		super();
		this.version = version.replaceAll("SNAPSHOT", "S");
	}

	public UUID getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(UUID uuid) {
		this.correlationId = uuid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getVersion() {
		return version;
	}

	public SpagoBIUserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(SpagoBIUserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
