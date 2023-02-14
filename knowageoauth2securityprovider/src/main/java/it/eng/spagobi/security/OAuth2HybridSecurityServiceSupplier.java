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
package it.eng.spagobi.security;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;

/**
 * This security provider is conceived when authentication is delegated to an OAuth2 provider, but Knowage is in charge for the authorization, i.e. users' roles
 * and profile attributes are stored within Knowage metadata. It has to be used along with it.eng.spagobi.services.oauth2.Oauth2HybridSsoService, in a way that
 * the user unique identifier is the regular Knowage JWT token (see class it.eng.spagobi.services.common.JWTSsoService).
 *
 * @author Davide Zerbetto
 *
 */
public class OAuth2HybridSecurityServiceSupplier extends InternalSecurityServiceSupplierImpl {

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		throw new UnreachableCodeException("You cannot invoke this method, since authentication must be delegated to the OAuth2 provider");
	}

}
