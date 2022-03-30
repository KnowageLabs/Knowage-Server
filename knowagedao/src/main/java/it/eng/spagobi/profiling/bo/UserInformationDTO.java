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

package it.eng.spagobi.profiling.bo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import it.eng.knowage.commons.multitenant.OrganizationImageManager;
import org.apache.commons.lang3.StringUtils;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tenant.TenantManager;

/**
 * Business Object used to retrieve basic informations about current the user
 *
 * @since 2021/03/05
 * @author albnale
 */

public class UserInformationDTO {

	private String userId;
	private String fullName;
	private Boolean isSuperadmin;
	private String defaultRole = null;
	private Map<String, Object> attributes;
	private String organization;
	private String organizationImageb64;
	private String uniqueIdentifier;
	private String email;
	private Locale locale = null;
	private Object userUniqueIdentifier = null;
	private Collection roles = null;
	private Collection functionalities;
	private boolean enterprise;

	public UserInformationDTO(UserProfile user) throws EMFInternalError {
		this.userId = String.valueOf(user.getUserId());
		this.fullName = String.valueOf(user.getUserName());
		this.isSuperadmin = user.getIsSuperadmin();
		this.attributes = user.getUserAttributes();
		this.organization = TenantManager.getTenant().getName();
		this.organizationImageb64 = OrganizationImageManager.getOrganizationB64Image(organization);
		// TODO: Change when there will be user email address
		this.email = null;
		if (user.getUserAttribute("email") != null && StringUtils.isNotBlank(user.getUserAttribute("email").toString()))
			this.email = user.getUserAttribute("email").toString();
		this.userUniqueIdentifier = user.getUserUniqueIdentifier();

		this.locale = GeneralUtilities.getDefaultLocale();
		this.roles = user.getRoles();

		Collection rolesOrDefaultRole = user.getRolesForUse();
		ArrayList<String> newList = new ArrayList<>(rolesOrDefaultRole);
		this.defaultRole = newList.size() == 1 ? newList.get(0) : null;

		this.functionalities = user.getFunctionalities();

		this.enterprise = isEnterpriseEdition();

	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getIsSuperadmin() {
		return isSuperadmin;
	}

	public void setIsSuperadmin(Boolean isSuperadmin) {
		this.isSuperadmin = isSuperadmin;
	}

	public String getDefaultRole() {
		return defaultRole;
	}

	public void setDefaultRole(String defaultRole) {
		this.defaultRole = defaultRole;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getOrganizationImageb64() {
		return organizationImageb64;
	}

	public void setOrganizationImageb64(String organizationImageb64) {
		this.organizationImageb64 = organizationImageb64;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(String object) {
		this.uniqueIdentifier = object;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Object getUserUniqueIdentifier() {
		return userUniqueIdentifier;
	}

	public void setUserUniqueIdentifier(Object userUniqueIdentifier) {
		this.userUniqueIdentifier = userUniqueIdentifier;
	}

	public Collection getRoles() {
		return roles;
	}

	public void setRoles(Collection roles) {
		this.roles = roles;
	}

	public Collection getFunctionalities() {
		return functionalities;
	}

	public void setFunctionalities(Collection functionalities) {
		this.functionalities = functionalities;
	}

	public boolean isEnterprise() {
		return enterprise;
	}

	public void setEnterprise(boolean enterprise) {
		this.enterprise = enterprise;
	}

	private boolean isEnterpriseEdition() {
		try {
			Class.forName("it.eng.knowage.tools.servermanager.utils.LicenseManager");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}