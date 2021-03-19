/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.knowage.security.oauth2;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.security.ISecurityInfoProvider;

/**
 * @author Jeremy Branham (jeremy@savantly.net)
 *
 */
public class OAuth2SecurityInfoProvider implements ISecurityInfoProvider {

	static private Logger logger = Logger.getLogger(OAuth2SecurityInfoProvider.class);

	/**
	 * TODO: Is there an oauth2 standard for getting all roles?
	 */
	@Override
	public List getRoles() {
		logger.debug("IN");

		List<SbiTenant> tenants = DAOFactory.getTenantsDAO().loadAllTenants();

		List<Role> roles = new ArrayList<Role>();
		List<String> roleStringArray = new ArrayList<>();
		roleStringArray.add("admin");
		roleStringArray.add("dev");
		roleStringArray.add("modeladmin");
		roleStringArray.add("user");

		for (String name : roleStringArray) {

			for (SbiTenant tenant : tenants) {
				Role role = new Role(name, name);
				role.setOrganization(tenant.getName());
				roles.add(role);
			}
		}
		logger.debug("OUT");
		return roles;
	}

	/**
	 * TODO: should this be configurable?
	 */
	@Override
	public List getAllProfileAttributesNames() {
		List<String> attributes = new ArrayList<String>();
		attributes.add("displayName");
		attributes.add("email");
		attributes.addAll(OAuth2Config.getInstance().getProfileAttributes());
		return attributes;
	}

}
