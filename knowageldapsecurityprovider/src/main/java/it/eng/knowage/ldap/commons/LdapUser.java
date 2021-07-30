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

package it.eng.knowage.ldap.commons;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class LdapUser {

	String distinguishName;
	String userId;
	String password;
	Attributes attributes;

	public LdapUser(String distinguishName, String userId, String psw, Attributes attributes) {
		super();
		this.distinguishName = distinguishName;
		this.userId = userId;
		this.attributes = attributes;
		this.password = psw;
	}

	public String getDistinguishName() {
		return distinguishName;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}
}
