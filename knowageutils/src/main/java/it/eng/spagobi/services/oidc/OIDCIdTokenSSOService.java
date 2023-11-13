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
package it.eng.spagobi.services.oidc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.oauth2.Oauth2SsoService;

public class OIDCIdTokenSSOService extends JWTSsoService {

	private static Logger logger = Logger.getLogger(OIDCIdTokenSSOService.class);

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String idToken = (String) session.getAttribute(Oauth2SsoService.ID_TOKEN);
		if (idToken == null) {
			logger.debug("ID token not found.");
			return super.readUserIdentifier(request);
		}
		LogMF.debug(logger, "ID token found: [{0}]", idToken);
		return idToken;
	}

}
