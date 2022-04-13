package it.eng.knowage.utils.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class LogTenantFilter implements Filter {
	private static final String ENVIRONMENT = "environment";
	private static transient Logger logger = Logger.getLogger(LogTenantFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String envUrlName = httpRequest.getHeader("X-Forwarded-Host");
			if (envUrlName != null) {
				if (envUrlName.contains(",")) {
					int iend = envUrlName.indexOf(".");
					envUrlName = envUrlName.substring(0, iend);
				}
				if (envUrlName.contains(":"))
					envUrlName = envUrlName.substring(0, envUrlName.indexOf(":"));
				if (envUrlName.contains("."))
					envUrlName = envUrlName.substring(0, envUrlName.indexOf("."));
				ThreadContext.put(ENVIRONMENT, envUrlName);

			}
			try {
				chain.doFilter(request, response);
			} finally {
				ThreadContext.remove(ENVIRONMENT);
			}
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
