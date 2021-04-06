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
package it.eng.spagobi.services.security;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

/**
 * This is the SecurityService interfaces
 *
 * @author Bernabei Angelo
 * @author Marco Libanori
 */
@WebService(
		name = "SecurityServiceService",
		portName = "SecurityServicePort",
		serviceName = "SecurityService",
		targetNamespace = "http://security.services.spagobi.eng.it/"
	)
@SOAPBinding(style = Style.RPC)
public interface SecurityService {

	/**
	 * Return the user profile informations
	 *
	 * @param token
	 * @return
	 */
	SpagoBIUserProfile getUserProfile(String token, String userId);

	/**
	 * Check if the user can access to the path
	 *
	 * @param token
	 * @param idFolder ( object tree )
	 * @param mode
	 * @return
	 */
	boolean isAuthorized(String token, String userId, String idFolder, String mode);

	/**
	 * Check if the user can access to this function
	 *
	 * @param token
	 * @param function
	 * @return
	 */
	boolean checkAuthorization(String token, String userId, String function);
}
