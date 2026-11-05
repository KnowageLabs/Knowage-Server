package it.eng.spagobi.commons.services;

import javax.servlet.http.HttpServletRequest;

import it.eng.knowage.privacymanager.LoginEventBuilder;
import it.eng.knowage.privacymanager.PrivacyManagerClient;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.bo.UserProfileUtility;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;

public final class LoginCompletionService {
	private LoginCompletionService() {
	}

	public static void store(UserProfile profile, HttpServletRequest request) {
		UserProfileUtility.enrichProfile(profile, request, request.getSession());
		request.getSession().setAttribute(IEngUserProfile.ENG_USER_PROFILE, profile);
	}

	public static void record(UserProfile profile) {
		LoginEventBuilder event = new LoginEventBuilder();
		event.appendSession("knowage", profile.getSourceIpAddress(), profile.getSessionId(), profile.getSessionStart(),
				profile.getUserId().toString());
		event.appendUserAgent(profile.getOs(), profile.getSourceIpAddress(), profile.getSourceSocketEnabled(), profile.getUserAgent());
		PrivacyManagerClient.getInstance().sendMessage(event.getDTO());
	}

	public static void completeFederated(UserProfile profile, HttpServletRequest request) {
		request.getSession();
		request.changeSessionId();
		TenantManager.setTenant(new Tenant(profile.getOrganization()));
		try {
			store(profile, request);
			record(profile);
			AuditLogUtilities.updateAudit(request, profile, "SPAGOBI.Login", null, "OK");
		} finally {
			TenantManager.unset();
		}
	}
}
