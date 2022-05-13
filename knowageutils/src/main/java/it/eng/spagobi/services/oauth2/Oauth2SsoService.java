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
package it.eng.spagobi.services.oauth2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.common.SsoServiceInterface;

public class Oauth2SsoService extends JWTSsoService {

	public static String ACCESS_TOKEN = "access_token";

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String userId = (String) session.getAttribute(ACCESS_TOKEN);
		if (userId == null)
			userId = request.getParameter(SsoServiceInterface.USER_ID);
		return userId;
	}

}
