package it.eng.spagobi.profiling;

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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PublicProfile {

	static Logger logger = Logger.getLogger(PublicProfile.class);

	static public final String PUBLIC_USER_PREFIX = "public-";
	static public final String PUBLIC_USER_NAME = "sbi.execution.publicProfileName";
	static final String PUBLIC_PATTERN_MATCH = "public/servlet";

	public static UserProfile evaluatePublicCase(ServletRequest request, HttpSession session, SessionContainer permanentSession) {

		UserProfile toReturn = null;

		if (((HttpServletRequest) request).getServletPath().contains(PUBLIC_PATTERN_MATCH)) {

			String organization = null;
			if (request.getParameter("ORGANIZATION") != null) {
				organization = request.getParameter("ORGANIZATION").toString();
			}
			if (organization == null) {
				throw new SpagoBIRuntimeException("Public request must specify ORGANIZATION");
			}

			String userId = PUBLIC_USER_PREFIX + organization;
			logger.debug("Public User Id is " + userId);

			SpagoBIUserProfile spagoBIProfile = createPublicUserProfile(userId);

			toReturn = new UserProfile(spagoBIProfile);

			if (permanentSession != null)
				permanentSession.setAttribute(IEngUserProfile.ENG_USER_PROFILE, toReturn);
			if (session != null)
				session.setAttribute(IEngUserProfile.ENG_USER_PROFILE, toReturn);
		}
		return toReturn;
	}

	/**
	 * create public user profile
	 */
	public static SpagoBIUserProfile createPublicUserProfile(String userId) {
		logger.debug("IN");

		int indexOfTreat = userId.indexOf("-");
		String organization = userId.substring(indexOfTreat + 1);

		String jwtToken = JWTSsoService.userId2jwtToken(userId);

		logger.debug("JWT-TOKEN " + jwtToken);

		SpagoBIUserProfile profile = null;

		try {

			profile = new SpagoBIUserProfile();
			profile.setUniqueIdentifier(jwtToken);
			profile.setUserId(userId);

			profile.setUserName("Anonymous user");
			profile.setOrganization(organization);
			profile.setIsSuperadmin(false);

			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			roleDAO.setTenant(organization);
			Role publicRole = roleDAO.loadPublicRole();

			List roles = new ArrayList();
			if (publicRole == null) {
				logger.error("No public role is defined");
				return null;
			} else {
				roles.add(publicRole.getName());
			}

			// end load profile attributes

			String[] roleStr = new String[roles.size()];
			for (int i = 0; i < roles.size(); i++) {
				roleStr[i] = (String) roles.get(i);
			}

			profile.setRoles(roleStr);

			profile.setFunctions(UserUtilities.readFunctionality(profile));

		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		logger.debug("OUT");
		return profile;

	}

	public static boolean isPublicUser(String jwtToken) {
		if (jwtToken == null) {
			return false;
		}
		try {
			String userId = JWTSsoService.jwtToken2userId(jwtToken);
			return userId.startsWith("public-");
		} catch (Exception e) {
			logger.debug("Error reading jwttoken for schedulatio. Are you using a sso?", e);
			return false;
		}

	}

}
