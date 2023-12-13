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
package it.eng.knowage.privacymanager;

import it.eng.knowage.pm.dto.Outcome;
import it.eng.knowage.pm.dto.PrivacyDTO;
import it.eng.knowage.pm.dto.PrivacyEventType;
import it.eng.knowage.pm.dto.SessionDTO;
import it.eng.knowage.pm.dto.UserAgentDTO;

public class LoginEventBuilder {

	protected PrivacyDTO dto;

	public LoginEventBuilder() {
		dto = new PrivacyDTO();
		dto.setEventType(PrivacyEventType.LOGIN);
		dto.setDescription("Login");
		dto.setModule("knowage");
		dto.setOutcome(Outcome.SUCCESS);
		dto.setTimestamp(System.currentTimeMillis());
	}

	public void appendSession(String appId, String ipAddr, String sessionId, Long sessionStart, String userId) {
		SessionDTO session = new SessionDTO();
		session.setApplicationId(appId);
		session.setIpAddress(ipAddr);
		session.setSessionId(sessionId);
		session.setSessionStart(sessionStart);
		session.setUserId(userId);

		dto.setSession(session);
	}

	public void appendUserAgent(String os, String sourceIp, boolean sourceSocketEnabled, String userAgent) {
		UserAgentDTO usrAg = new UserAgentDTO();
		usrAg.setOs(os);
		usrAg.setSourceIpAddress(sourceIp);
		usrAg.setSourceSocketEnabled(sourceSocketEnabled);
		usrAg.setUserAgent(userAgent);

		dto.setUserAgent(usrAg);
	}

	public PrivacyDTO getDTO() {
		return dto;
	}

}
