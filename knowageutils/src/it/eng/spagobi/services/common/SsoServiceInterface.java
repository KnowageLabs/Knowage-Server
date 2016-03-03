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
