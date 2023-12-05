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
