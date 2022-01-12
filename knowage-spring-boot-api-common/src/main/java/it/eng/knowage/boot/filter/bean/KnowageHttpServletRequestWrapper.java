package it.eng.knowage.boot.filter.bean;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import it.eng.spagobi.services.security.SpagoBIUserProfile;

public class KnowageHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private final Principal principal;

	public KnowageHttpServletRequestWrapper(HttpServletRequest request, SpagoBIUserProfile userProfile) {
		super(request);

		this.principal = new KnowageUserPrincipal(userProfile);
	}

	@Override
	public Principal getUserPrincipal() {
		return principal;
	}

}
