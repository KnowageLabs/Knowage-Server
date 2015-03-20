/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

public class EncodingFilter implements Filter {

	
	private static transient Logger logger = Logger.getLogger(EncodingFilter.class);
	 private FilterConfig filterConfig = null;
	 
	 public void init(FilterConfig filterConfig) {
		    this.filterConfig = filterConfig;
		  }
	 
	
    public void destroy() {
    	// do nothing
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
        	throws IOException, ServletException {
        	logger.debug("IN");
        	
        	String encoding = filterConfig.getInitParameter("encoding");
        	if(encoding == null) encoding = "UTF-8";        	
        	request.setCharacterEncoding(encoding);
    		
        	chain.doFilter(request, response);
    }

}
