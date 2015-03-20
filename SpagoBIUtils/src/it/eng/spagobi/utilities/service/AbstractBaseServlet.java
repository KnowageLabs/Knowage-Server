/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.service;

import java.io.IOException;

import it.eng.spagobi.utilities.container.HttpServletRequestContainer;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.BaseServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractBaseServlet extends HttpServlet {
	
	
	 /**
     * Logger component
     */
    private static transient Logger logger = Logger.getLogger(AbstractBaseServlet.class);
    
	

    public void init(ServletConfig config) throws ServletException {
    	super.init(config);	
    }
    
    public void service(HttpServletRequest request, HttpServletResponse response) {
    	BaseServletIOManager servletIOManager;
    	
    	servletIOManager = new BaseServletIOManager(request, response);
    	
    	try {
			this.doService( servletIOManager );
		} catch (Throwable t) {
			handleException(servletIOManager, t);
		}
    }
    
    public abstract void doService(BaseServletIOManager servletIOManager) throws SpagoBIEngineException;
    
    public abstract void handleException(BaseServletIOManager servletIOManager, Throwable t);
	
	
    
	
	
	
	
	
	
	
	
	
	
	
	
}
