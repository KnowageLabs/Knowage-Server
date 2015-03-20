/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.common;

import it.eng.spagobi.services.security.exceptions.SecurityException;

import java.io.IOException;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 
 * Interface for read and validate a proxy ticket
 * 
 */
public interface SsoServiceInterface {

	public static final String USER_ID = "user_id";
	public static final String USER_NAME_REQUEST_PARAMETER = "USERNAME";
	public static final String PASSWORD_REQUEST_PARAMETER = "PASSWORD";
	public static final String PASSWORD_MODE_REQUEST_PARAMETER = "PASSWORD_MODE";
	public static final String PASSWORD_MODE_ENCRYPTED = "ENC";
	public static final String SILENT_LOGIN = "SILENT_LOGIN";

	/**
	 * 
	 * @param ticket
	 *            String
	 * @param userId
	 *            String
	 * @throws SecurityException
	 *             String
	 */
	void validateTicket(String ticket, String userId) throws SecurityException;

	/**
	 * 
	 * @param session
	 *            Http Session
	 * @return String
	 * @throws IOException
	 */
	String readTicket(HttpSession session) throws IOException;

	/**
	 * 
	 * @param request
	 *            Http request
	 * @return
	 */
	String readUserIdentifier(HttpServletRequest request);

	/**
	 * 
	 * @param session
	 *            Portlet Session
	 * @return
	 */
	String readUserIdentifier(PortletSession session);
}
