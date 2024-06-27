package it.eng.spagobi.security.utils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AntiCsrfFilter  implements Filter{

	private ServletContext context;
	
	private static final String DEFAULT_TOKEN_NAME = "X-CSRF-TOKEN";
	private static final String TOKEN_NAME_PROP = "csrfTokenName";
	private String csrfTokenName;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
        // recupero il token name dalle cponfigurazioni, properties, env oppure uso default
		csrfTokenName = System.getProperty(TOKEN_NAME_PROP);
		if(csrfTokenName == null) {
			System.getenv(TOKEN_NAME_PROP);
			if(csrfTokenName == null) {
				csrfTokenName = DEFAULT_TOKEN_NAME;
			}
		}

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
	    // controllo per escludere le login dal check csrf
		String page = request.getParameter("PAGE");
		String actionName = request.getParameter("ACTION_NAME");
		
		if( !("LoginPage".equals(page) 
		   || "LOGIN_ACTION_BY_TOKEN".equals(actionName) 
		   || "LOGIN_ACTION_WEB".equals(actionName)) ) {
		  String cookieVal = "";
		  String paramVal;
		  // getCookie
		  HttpServletRequest req =   ((HttpServletRequest)request);
		  Cookie[] cookies= req.getCookies();
		  boolean cFound = false;
		  if(cookies != null) {
			  for(Cookie cookie : cookies) {
				if(cookie.getName().equals(csrfTokenName)) {
					cFound = true;
					cookieVal = cookie.getValue();
				}
			  }
		  }
		  paramVal = req.getHeader(csrfTokenName);
		
		  if((!(cFound && paramVal != null)) || (!cookieVal.equals(paramVal))){
			 HttpServletResponse resp = (HttpServletResponse)response;
			 // sparo direttamente 403
			 resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
		  }
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
