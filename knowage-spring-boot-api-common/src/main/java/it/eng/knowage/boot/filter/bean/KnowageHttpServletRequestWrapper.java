package it.eng.knowage.boot.filter.bean;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import it.eng.spagobi.services.security.SpagoBIUserProfile;
import org.apache.logging.log4j.ThreadContext;

public class KnowageHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private final Principal principal;

	public KnowageHttpServletRequestWrapper(HttpServletRequest request, SpagoBIUserProfile userProfile) {
		super(request);

		this.principal = new KnowageUserPrincipal(userProfile);
        ThreadContext.put("tenant", userProfile.getOrganization());
	}

	@Override
	public Principal getUserPrincipal() {
		return principal;
	}

}
