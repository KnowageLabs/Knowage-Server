/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;

import it.eng.spago.base.SourceBean;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


public class EngineTestServlet extends HttpServlet {
	/**
     * Logger component
     */
    private static transient Logger logger = Logger.getLogger(EngineTestServlet.class);
    
    public void service(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
    	
    	logger.debug("IN");
    	
    	request.getSession(true);
		String message = "sbi.connTestOk";
		
		response.getOutputStream().write(message.getBytes());
    	response.getOutputStream().flush();
    	logger.debug("OUT");  		
    }    
}
