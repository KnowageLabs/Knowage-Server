/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.commons.bo;

import java.util.Objects;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class UserProfileUtility {

	public static UserProfile enrichProfile(UserProfile profile, ServletRequest req, HttpSession session) {
		if (!(req instanceof HttpServletRequest))
			return profile;

		HttpServletRequest request = (HttpServletRequest) req;

		String browserDetails = request.getHeader("User-Agent");
		String userAgent = browserDetails;

		String os = "";

		// =================OS=======================
		if (userAgent.toLowerCase().indexOf("windows") >= 0) {
			os = "Windows";
		} else if (userAgent.toLowerCase().indexOf("mac") >= 0) {
			os = "Mac";
		} else if (userAgent.toLowerCase().indexOf("x11") >= 0) {
			os = "Unix";
		} else if (userAgent.toLowerCase().indexOf("android") >= 0) {
			os = "Android";
		} else if (userAgent.toLowerCase().indexOf("iphone") >= 0) {
			os = "IPhone";
		} else {
			os = "UnKnown, More-Info: " + userAgent;
		}
		profile.setUserAgent(userAgent);
		profile.setOs(os);
		profile.setSourceIpAddress(request.getRemoteAddr());
		profile.setSourceSocketEnabled(false);

		if (Objects.nonNull(session)) {
			profile.setSessionStart(session.getCreationTime());
			profile.setSessionId(session.getId());
		}

		return profile;
	}

	private UserProfileUtility() {

	}
}
