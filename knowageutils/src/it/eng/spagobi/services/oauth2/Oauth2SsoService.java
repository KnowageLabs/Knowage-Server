/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
