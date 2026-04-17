/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2024 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.security.ldap.exceptions;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Thrown when the user's credentials are rejected by the LDAP server (invalid password)
 * or when the user does not belong to an authorized group.
 */
public class LdapAuthenticationException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = 1L;

	public LdapAuthenticationException(String message) {
		super(message);
	}

	public LdapAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

}
