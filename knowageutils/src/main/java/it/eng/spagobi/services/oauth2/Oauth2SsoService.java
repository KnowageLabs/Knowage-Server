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

import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import java.io.IOException;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Oauth2SsoService implements SsoServiceInterface {

	public void validateTicket(String ticket, String userId) throws SecurityException {
		// TODO Auto-generated method stub
	}

	public String readTicket(HttpSession session) throws IOException {
		return "NA";
	}

	public String readUserIdentifier(HttpServletRequest request) {
		HttpSession session = request.getSession();

		String userId = (String) session.getAttribute("access_token");

		if (userId == null)
			userId = request.getParameter(SsoServiceInterface.USER_ID);

		return userId;
	}

	public String readUserIdentifier(PortletSession session) {
		// TODO Auto-generated method stub
		return null;
	}

}
