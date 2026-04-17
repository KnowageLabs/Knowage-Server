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
 * Thrown when an LDAP search operation fails with a NamingException
 * (e.g., invalid filter, inaccessible base DN, server error).
 */
public class LdapSearchException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = 1L;

	public LdapSearchException(String message) {
		super(message);
	}

	public LdapSearchException(String message, Throwable cause) {
		super(message, cause);
	}

}
